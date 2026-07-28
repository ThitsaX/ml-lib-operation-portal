package com.thitsaworks.operation_portal.core.revenue_config.engine;

import com.thitsaworks.operation_portal.component.common.type.RevenueConfigStatus;
import com.thitsaworks.operation_portal.component.common.type.RevenueRemainderRecipient;
import com.thitsaworks.operation_portal.component.common.type.RevenueRoundingMode;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigErrors;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigException;
import com.thitsaworks.operation_portal.core.revenue_config.model.RevenueConfig;
import com.thitsaworks.operation_portal.core.revenue_config.model.RevenueConfigHistory;
import com.thitsaworks.operation_portal.core.revenue_config.model.RevenueRoundingPolicy;
import com.thitsaworks.operation_portal.core.revenue_config.model.repository.RevenueConfigHistoryRepository;
import com.thitsaworks.operation_portal.core.revenue_config.model.repository.RevenueConfigRepository;
import com.thitsaworks.operation_portal.core.revenue_config.model.repository.RevenueRoundingPolicyRepository;
import com.thitsaworks.operation_portal.core.revenue_party.model.RevenueParty;
import com.thitsaworks.operation_portal.core.revenue_party.repository.RevenuePartyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueEngineHandler implements RevenueEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevenueEngineHandler.class);

    private static final int REVENUE_AMOUNT_SCALE = 2;

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final RevenueConfigRepository revenueConfigRepository;

    private final RevenueConfigHistoryRepository revenueConfigHistoryRepository;

    private final RevenuePartyRepository revenuePartyRepository;

    private final RevenueRoundingPolicyRepository revenueRoundingPolicyRepository;

    private static final Comparator<RevenueConfig> LATEST_UPDATED_REVENUE_CONFIG_FIRST = Comparator
                                                                                             .comparing(
                                                                                                 RevenueEngineHandler::updatedAtOrCreatedAt,
                                                                                                 Comparator.nullsFirst(
                                                                                                     Comparator.naturalOrder()))
                                                                                             .thenComparing(
                                                                                                 revenueConfig -> revenueConfig
                                                                                                                      .getRevenueConfigId()
                                                                                                                      .getEntityId(),
                                                                                                 Comparator.nullsLast(
                                                                                                     Comparator.naturalOrder()))
                                                                                             .reversed();

    private final AtomicReference<Snapshot> snapshotRef = new AtomicReference<>(Snapshot.empty());

    @Override
    @CoreWriteTransactional
    public void archiveExpiredRevenueConfigs() {

        List<RevenueConfig> activeRevenueConfigs = this.revenueConfigRepository.findByStatusIn(
            List.of(RevenueConfigStatus.ACTIVE), Sort.by(Sort.Direction.ASC, "taxCodeId"));

        Map<String, List<RevenueConfig>> revenueConfigsByTaxCode = activeRevenueConfigs
                                                                       .stream()
                                                                       .collect(
                                                                           Collectors.groupingBy(
                                                                               RevenueConfig::getTaxCodeId));

        Instant now = Instant.now();
        List<RevenueConfig> expiredRevenueConfigs = new ArrayList<>();
        for (List<RevenueConfig> revenueConfigs : revenueConfigsByTaxCode.values()) {
            if (revenueConfigs.size() <= 1) {
                continue;
            }

            expiredRevenueConfigs.addAll(this.expiredRevenueConfigs(revenueConfigs, now));
        }

        if (expiredRevenueConfigs.isEmpty()) {
            LOGGER.debug("No expired revenue configurations found to archive.");
            return;
        }

        List<RevenueConfigHistory> histories = expiredRevenueConfigs
                                                   .stream()
                                                   .map(revenueConfig -> new RevenueConfigHistory(
                                                       revenueConfig, RevenueConfigStatus.EXPIRED))
                                                   .toList();

        this.revenueConfigHistoryRepository.saveAll(histories);
        this.revenueConfigRepository.deleteAll(expiredRevenueConfigs);
        this.revenueConfigRepository.flush();

        LOGGER.info("Archived [{}] expired revenue configurations.", expiredRevenueConfigs.size());
    }

    @Override
    @CoreReadTransactional
    public void runStatusLifecycleJob() {

        try {
            LOGGER.debug("Start refreshing revenue engine data for calculation");

            Instant now = Instant.now();
            List<RevenueConfig> revenueConfigs = this.revenueConfigRepository.findByStatusIn(
                List.of(RevenueConfigStatus.ACTIVE), Sort.by(Sort.Direction.ASC, "taxCodeId"));
            List<RevenueParty> revenueParties = this.revenuePartyRepository.findAll();
            RevenueRoundingPolicy roundingPolicy = this.revenueRoundingPolicyRepository
                                                       .findFirstByOrderByCreatedAtDescRevenueRoundingPolicyId_IdDesc()
                                                       .orElse(null);

            var withTaxCodeId = revenueConfigs
                                    .stream()
                                    .filter(revenueConfig -> this.isCurrent(revenueConfig, now))
                                    .sorted(LATEST_UPDATED_REVENUE_CONFIG_FIRST)
                                    .collect(Collectors.toUnmodifiableMap(
                                        RevenueConfig::getTaxCodeId, Function.identity(),
                                        (a, b) -> a));

            var withPartyCode = revenueParties
                                    .stream()
                                    .filter(RevenueParty::isActive)
                                    .collect(Collectors.toUnmodifiableMap(
                                        RevenueParty::getPartyCode, Function.identity(),
                                        (a, b) -> a));

            this.snapshotRef.set(new Snapshot(withTaxCodeId, withPartyCode, roundingPolicy));

            LOGGER.debug(
                "Revenue engine data refresh completed. revenueConfigs=[{}], revenueParties=[{}]",
                withTaxCodeId.size(), withPartyCode.size());

        } catch (Exception e) {
            LOGGER.info("Revenue engine refresh failed", e);
            throw new IllegalStateException("Revenue engine refresh failed", e);
        }
    }

    @Override
    public RevenueSplit calculateRevenue(String taxCodeId, BigDecimal amount)
        throws RevenueConfigException {

        this.validateCalculationInput(taxCodeId, amount);

        Snapshot snapshot = this.snapshotRef.get();
        RevenueConfig revenueConfig = Optional
                                          .ofNullable(snapshot.revenueConfigMap().get(taxCodeId))
                                          .orElseThrow(() -> new RevenueConfigException(
                                              RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(
                                                  taxCodeId)));
        RevenueRoundingPolicy roundingPolicy = Optional
                                                   .ofNullable(snapshot.roundingPolicy())
                                                   .orElseThrow(() -> new RevenueConfigException(
                                                       RevenueConfigErrors.REVENUE_ROUNDING_POLICY_NOT_FOUND));

        RevenueRemainderRecipient remainderRecipient = roundingPolicy.getRemainderRecipient();

        BigDecimal normalizedAmount = amount.setScale(REVENUE_AMOUNT_SCALE);
        RevenueRoundingMode roundingMode = roundingPolicy.getRoundingMode();

        BigDecimal golAmount = this.calculateSplitAmount(
            normalizedAmount, revenueConfig.getGolPercentage(), roundingMode);
        BigDecimal ministryAmount = this.calculateSplitAmount(
            normalizedAmount, revenueConfig.getMinistryPercentage(), roundingMode);
        BigDecimal thirdPartyAmount = this.calculateSplitAmount(
            normalizedAmount, revenueConfig.getThirdPartyPercentage(), roundingMode);
        BigDecimal sendingDfspAmount = this.calculateSplitAmount(
            normalizedAmount, revenueConfig.getSendingDfspPercentage(), roundingMode);

        switch (remainderRecipient) {
            case GOL_GRA -> golAmount = normalizedAmount
                                            .subtract(ministryAmount)
                                            .subtract(thirdPartyAmount)
                                            .subtract(sendingDfspAmount);
            case MINISTRY -> ministryAmount = normalizedAmount
                                                  .subtract(golAmount)
                                                  .subtract(thirdPartyAmount)
                                                  .subtract(sendingDfspAmount);
            case THIRD_PARTY -> thirdPartyAmount = normalizedAmount
                                                       .subtract(golAmount)
                                                       .subtract(ministryAmount)
                                                       .subtract(sendingDfspAmount);
            case DFSP -> sendingDfspAmount = normalizedAmount
                                                 .subtract(golAmount)
                                                 .subtract(ministryAmount)
                                                 .subtract(thirdPartyAmount);
        }

        BigDecimal remainderAmount = switch (remainderRecipient) {
            case GOL_GRA -> golAmount;
            case MINISTRY -> ministryAmount;
            case THIRD_PARTY -> thirdPartyAmount;
            case DFSP -> sendingDfspAmount;
        };

        if (remainderAmount.signum() < 0) {
            throw new RevenueConfigException(
                RevenueConfigErrors.INVALID_REVENUE_SPLIT.format(remainderRecipient.name()));
        }

        return new RevenueSplit(
            revenueConfig.getRevenueConfigId(), revenueConfig.getTaxCodeId(),
            revenueConfig.getTaxCodeDescription(), revenueConfig.getCategory(),
            revenueConfig.getResponsibleMinistryCode(), revenueConfig.getThirdPartyProviderCode(),
            normalizedAmount, revenueConfig.getGolPercentage(), golAmount,
            revenueConfig.getMinistryPercentage(), ministryAmount,
            revenueConfig.getThirdPartyPercentage(), thirdPartyAmount,
            revenueConfig.getSendingDfspPercentage(), sendingDfspAmount, roundingMode.name(),
            remainderRecipient.name());
    }

    private void validateCalculationInput(String taxCodeId, BigDecimal amount)
        throws RevenueConfigException {

        if (taxCodeId == null || taxCodeId.isBlank()) {
            throw new RevenueConfigException(
                RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(taxCodeId));
        }

        if (amount == null || amount.signum() < 0 ||
                amount.stripTrailingZeros().scale() > REVENUE_AMOUNT_SCALE) {
            throw new RevenueConfigException(RevenueConfigErrors.INVALID_REVENUE_AMOUNT);
        }
    }

    private BigDecimal calculateSplitAmount(BigDecimal amount,
                                            BigDecimal percentage,
                                            RevenueRoundingMode roundingMode) {

        return amount
                   .multiply(percentage)
                   .divide(ONE_HUNDRED)
                   .setScale(REVENUE_AMOUNT_SCALE, roundingMode.toRoundingMode());
    }

    private record Snapshot(Map<String, RevenueConfig> revenueConfigMap,
                            Map<String, RevenueParty> revenuePartyMap,
                            RevenueRoundingPolicy roundingPolicy) {

        static Snapshot empty() {

            return new Snapshot(Map.of(), Map.of(), null);
        }

    }

    private List<RevenueConfig> expiredRevenueConfigs(List<RevenueConfig> revenueConfigs,
                                                      Instant now) {

        return revenueConfigs
                   .stream()
                   .filter(revenueConfig -> this.isCurrent(revenueConfig, now))
                   .sorted(LATEST_UPDATED_REVENUE_CONFIG_FIRST)
                   .skip(1)
                   .toList();
    }

    private boolean isFuture(RevenueConfig revenueConfig, Instant now) {

        Instant effectiveDate = revenueConfig.getEffectiveDate();
        return effectiveDate != null && effectiveDate.isAfter(now);
    }

    private boolean isCurrent(RevenueConfig revenueConfig, Instant now) {

        return !this.isFuture(revenueConfig, now);
    }

    private static Instant updatedAtOrCreatedAt(RevenueConfig revenueConfig) {

        return revenueConfig.getUpdatedAt() != null ? revenueConfig.getUpdatedAt() :
                   revenueConfig.getCreatedAt();
    }

}
