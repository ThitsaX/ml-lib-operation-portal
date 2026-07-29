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

package com.thitsaworks.operation_portal.usecase.operation_portal.impl;

import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigEffectiveStatus;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigStatus;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.revenue_config.data.RevenueConfigData;
import com.thitsaworks.operation_portal.core.revenue_config.query.RevenueConfigQuery;
import com.thitsaworks.operation_portal.core.revenue_party.data.RevenuePartyData;
import com.thitsaworks.operation_portal.core.revenue_party.query.RevenuePartyQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetRevenueConfigList;
import com.thitsaworks.operation_portal.usecase.util.Utility;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ActionMetadata(category = ActionCategory.REVENUE_CONFIG)
public class GetRevenueConfigListHandler
    extends OperationPortalUseCase<GetRevenueConfigList.Input, GetRevenueConfigList.Output>
    implements GetRevenueConfigList {

    private final RevenueConfigQuery revenueConfigQuery;

    private final RevenuePartyQuery revenuePartyQuery;

    private final Utility utility;

    private static final Comparator<RevenueConfigData> LATEST_UPDATED_REVENUE_CONFIG_FIRST =
        Comparator
            .comparing(
                GetRevenueConfigListHandler::updatedAtOrCreatedAt,
                Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(
                data -> data.revenueConfigId().getEntityId(),
                Comparator.nullsLast(Comparator.naturalOrder()))
            .reversed();

    public GetRevenueConfigListHandler(PrincipalCache principalCache,
                                       ActionAuthorizationManager actionAuthorizationManager,
                                       RevenueConfigQuery revenueConfigQuery,
                                       RevenuePartyQuery revenuePartyQuery,
                                       Utility utility) {

        super(principalCache, actionAuthorizationManager);
        this.revenueConfigQuery = revenueConfigQuery;
        this.revenuePartyQuery = revenuePartyQuery;
        this.utility = utility;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        Sort sort = Sort.by(input.sortDirection(), this.sortField(input.sortBy()));
        List<GetRevenueConfigList.RevenueConfig> revenueConfigs = this.revenueConfigQuery
                                                                      .getRevenueConfigs(sort)
                                                                      .stream()
                                                                      .filter(this::isVisibleStatus)
                                                                      .collect(Collectors.collectingAndThen(
                                                                          Collectors.toList(),
                                                                          this::visibleRevenueConfigs))
                                                                      .stream()
                                                                      .map(this::map)
                                                                      .toList();

        return new Output(revenueConfigs);
    }

    private List<RevenueConfigData> visibleRevenueConfigs(List<RevenueConfigData> revenueConfigs) {

        Set<Long> visibleIds = new HashSet<>();
        revenueConfigs
            .stream()
            .filter(data -> data.status() == RevenueConfigStatus.INACTIVE)
            .map(data -> data.revenueConfigId().getEntityId())
            .forEach(visibleIds::add);

        Map<String, List<RevenueConfigData>> activeConfigsByTaxCode = revenueConfigs
                                                                          .stream()
                                                                          .filter(data -> data.status() ==
                                                                                              RevenueConfigStatus.ACTIVE)
                                                                          .collect(Collectors.groupingBy(
                                                                              RevenueConfigData::taxCodeId));

        Instant now = Instant.now();
        for (List<RevenueConfigData> taxCodeConfigs : activeConfigsByTaxCode.values()) {
            this.visibleActiveRevenueConfigs(taxCodeConfigs, now)
                .stream()
                .map(data -> data.revenueConfigId().getEntityId())
                .forEach(visibleIds::add);
        }

        return revenueConfigs
                   .stream()
                   .filter(data -> visibleIds.contains(data.revenueConfigId().getEntityId()))
                   .toList();
    }

    private List<RevenueConfigData> visibleActiveRevenueConfigs(List<RevenueConfigData> revenueConfigs,
                                                                Instant now) {

        Optional<RevenueConfigData> currentRevenueConfig = revenueConfigs
                                                              .stream()
                                                              .filter(data -> this.isCurrent(data, now))
                                                              .sorted(
                                                                  LATEST_UPDATED_REVENUE_CONFIG_FIRST)
                                                              .findFirst();

        List<RevenueConfigData> futureRevenueConfigs = revenueConfigs
                                                           .stream()
                                                           .filter(data -> !this.isCurrent(data, now))
                                                           .toList();

        return Stream
                   .concat(currentRevenueConfig.stream(), futureRevenueConfigs.stream())
                   .toList();
    }

    private String sortField(String sortBy) {

        return "createdDate".equals(sortBy) ? "createdAt" : sortBy;
    }

    private GetRevenueConfigList.RevenueConfig map(RevenueConfigData data) {

        return new GetRevenueConfigList.RevenueConfig(
            data.revenueConfigId(), data.taxCodeId(), data.taxCodeDescription(),
            data.category().name(), data.responsibleMinistryCode(),
            this.revenuePartyName(data.responsibleMinistryCode()), data.thirdPartyProviderCode(),
            this.revenuePartyName(data.thirdPartyProviderCode()), data.golPercentage(),
            data.ministryPercentage(), data.thirdPartyPercentage(), data.sendingDfspPercentage(),
            this.status(data), data.effectiveDate(), data.effectiveTimezone(), data.respondedDate(),
            data.createdAt() == null ? null : data.createdAt().getEpochSecond(),
            data.createdBy() == null ? null :
                this.utility.getEmail(new UserId(data.createdBy().getId())),
            data.respondedDate() == null ? null : data.respondedDate().getEpochSecond(),
            data.updatedBy() == null ? null :
                this.utility.getEmail(new UserId(data.updatedBy().getId())));
    }

    private String status(RevenueConfigData data) {

        if (data.status() == RevenueConfigStatus.INACTIVE) {
            return RevenueConfigStatus.INACTIVE.name();
        }

        return RevenueConfigEffectiveStatus.fromEffectiveDate(data.effectiveDate()).name();
    }

    private boolean isVisibleStatus(RevenueConfigData data) {

        return data.status() == RevenueConfigStatus.ACTIVE ||
                   data.status() == RevenueConfigStatus.INACTIVE;
    }

    private boolean isCurrent(RevenueConfigData data, Instant now) {

        Instant effectiveDate = data.effectiveDate();
        return effectiveDate == null || !effectiveDate.isAfter(now);
    }

    private String revenuePartyName(String partyCode) {

        if (partyCode == null || partyCode.isBlank()) {
            return null;
        }

        return this.revenuePartyQuery.get(partyCode).map(RevenuePartyData::partyName).orElse(null);
    }

    private static Instant updatedAtOrCreatedAt(RevenueConfigData data) {

        return data.updatedAt() != null ? data.updatedAt() : data.createdAt();
    }

}
