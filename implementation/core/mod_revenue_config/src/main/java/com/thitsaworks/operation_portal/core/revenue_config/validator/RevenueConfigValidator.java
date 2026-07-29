/*
 * Copyright (c) 2024-2026 ThitsaWorks Pte. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thitsaworks.operation_portal.core.revenue_config.validator;

import com.thitsaworks.operation_portal.component.common.identifier.RevenueConfigId;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigCategory;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigStatus;
import com.thitsaworks.operation_portal.component.misc.util.TimeZoneUtil;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigErrors;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigException;
import com.thitsaworks.operation_portal.core.revenue_config.model.repository.RevenueConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RevenueConfigValidator {

    private static final BigDecimal ZERO_PERCENTAGE = BigDecimal.ZERO;

    private static final BigDecimal TOTAL_PERCENTAGE = new BigDecimal("100.00");

    private static final int PERCENTAGE_SCALE = 2;

    private final PartyRegistryValidator partyRegistryValidator;

    private final RevenueConfigRepository revenueConfigRepository;

    public void validate(RevenueConfigCategory category,
                         String responsibleMinistryCode,
                         String thirdPartyProviderCode,
                         BigDecimal golPercentage,
                         BigDecimal ministryPercentage,
                         BigDecimal thirdPartyPercentage,
                         BigDecimal sendingDfspPercentage,
                         Instant effectiveDate,
                         String effectiveTimezone) throws RevenueConfigException {

        if (category == null) {
            throw new RevenueConfigException(RevenueConfigErrors.INVALID_REVENUE_CONFIG_CATEGORY);
        }

        validatePartyRegistryReferences(responsibleMinistryCode, thirdPartyProviderCode);
        validatePercentages(golPercentage, ministryPercentage, thirdPartyPercentage, sendingDfspPercentage);
        validateEffectiveDate(effectiveDate, effectiveTimezone);
    }

    public void validateUniqueTaxCode(String taxCodeId,
                                      RevenueConfigId allowedRevenueConfigId)
        throws RevenueConfigException {

        if (taxCodeId == null || taxCodeId.isBlank()) {
            return;
        }

        boolean duplicateExists = this.revenueConfigRepository
                                      .findByTaxCodeId(taxCodeId)
                                      .stream()
                                      .filter(revenueConfig -> revenueConfig.getStatus() ==
                                          RevenueConfigStatus.ACTIVE)
                                      .anyMatch(revenueConfig -> !Objects.equals(
                                          revenueConfig.getRevenueConfigId(),
                                          allowedRevenueConfigId));

        if (duplicateExists) {
            throw new RevenueConfigException(
                RevenueConfigErrors.TAX_CODE_ALREADY_REGISTERED.format(taxCodeId));
        }
    }

    public void validateTaxCodeUnchanged(String existingTaxCodeId,
                                         String requestedTaxCodeId)
        throws RevenueConfigException {

        if (!Objects.equals(existingTaxCodeId, requestedTaxCodeId)) {
            throw new RevenueConfigException(
                RevenueConfigErrors.TAX_CODE_MODIFICATION_NOT_ALLOWED.format(
                    existingTaxCodeId, requestedTaxCodeId));
        }
    }

    private void validatePartyRegistryReferences(String responsibleMinistryCode,
                                                 String thirdPartyProviderCode)
        throws RevenueConfigException {

        if (!this.partyRegistryValidator.isActiveResponsibleMinistry(responsibleMinistryCode)) {
            throw new RevenueConfigException(RevenueConfigErrors.INVALID_RESPONSIBLE_MINISTRY.format(
                responsibleMinistryCode));
        }

        if (thirdPartyProviderCode != null && !thirdPartyProviderCode.isBlank() &&
                !this.partyRegistryValidator.isActiveThirdPartyProvider(thirdPartyProviderCode)) {
            throw new RevenueConfigException(RevenueConfigErrors.INVALID_THIRD_PARTY_PROVIDER.format(
                thirdPartyProviderCode));
        }
    }

    private void validatePercentages(BigDecimal golPercentage,
                                     BigDecimal ministryPercentage,
                                     BigDecimal thirdPartyPercentage,
                                     BigDecimal sendingDfspPercentage) throws RevenueConfigException {

        if (isOverScale(golPercentage) ||
                isOverScale(ministryPercentage) ||
                isOverScale(thirdPartyPercentage) ||
                isOverScale(sendingDfspPercentage)) {
            throw new RevenueConfigException(
                RevenueConfigErrors.INVALID_REVENUE_PERCENTAGE_SCALE.format(PERCENTAGE_SCALE));
        }

        if (isOutOfRange(golPercentage) ||
                isOutOfRange(ministryPercentage) ||
                isOutOfRange(thirdPartyPercentage) ||
                isOutOfRange(sendingDfspPercentage)) {
            throw new RevenueConfigException(RevenueConfigErrors.INVALID_REVENUE_PERCENTAGE);
        }

        BigDecimal total = golPercentage.add(ministryPercentage)
                                        .add(thirdPartyPercentage)
                                        .add(sendingDfspPercentage);
        if (total.compareTo(TOTAL_PERCENTAGE) != 0) {
            throw new RevenueConfigException(RevenueConfigErrors.INVALID_REVENUE_PERCENTAGE_TOTAL.format(total));
        }
    }

    private boolean isOutOfRange(BigDecimal percentage) {

        return percentage == null ||
                   percentage.compareTo(ZERO_PERCENTAGE) < 0 ||
                   percentage.compareTo(TOTAL_PERCENTAGE) > 0;
    }

    private boolean isOverScale(BigDecimal percentage) {

        return percentage != null &&
                   percentage.stripTrailingZeros().scale() > PERCENTAGE_SCALE;
    }

    private void validateEffectiveDate(Instant effectiveDate,
                                       String effectiveTimezone) throws RevenueConfigException {

        if (effectiveDate == null) {
            return;
        }

        ZoneId zoneId = TimeZoneUtil.zoneId(effectiveTimezone);
        LocalDate effectiveLocalDate = LocalDate.ofInstant(effectiveDate, zoneId);
        LocalDate currentLocalDate = LocalDate.now(zoneId);
        if (effectiveLocalDate.isBefore(currentLocalDate)) {
            throw new RevenueConfigException(RevenueConfigErrors.INVALID_EFFECTIVE_DATE);
        }
    }
}
