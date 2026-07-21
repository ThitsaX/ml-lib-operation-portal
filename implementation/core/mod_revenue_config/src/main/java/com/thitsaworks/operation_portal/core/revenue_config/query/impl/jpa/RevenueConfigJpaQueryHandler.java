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
package com.thitsaworks.operation_portal.core.revenue_config.query.impl.jpa;

import com.thitsaworks.operation_portal.component.common.identifier.RevenueConfigId;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.core.revenue_config.data.RevenueConfigData;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigErrors;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigException;
import com.thitsaworks.operation_portal.core.revenue_config.model.RevenueConfig;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigStatus;
import com.thitsaworks.operation_portal.core.revenue_config.model.repository.RevenueConfigRepository;
import com.thitsaworks.operation_portal.core.revenue_config.query.RevenueConfigQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CoreReadTransactional
public class RevenueConfigJpaQueryHandler implements RevenueConfigQuery {

    private static final List<RevenueConfigStatus> ACTIVE_AND_PENDING_STATUSES =
        List.of(RevenueConfigStatus.ACTIVE, RevenueConfigStatus.PENDING);

    private final RevenueConfigRepository revenueConfigRepository;

    @Override
    public List<RevenueConfigData> getRevenueConfigs(Sort sort) {

        List<RevenueConfig> revenueConfigs = sort == null
            ? this.revenueConfigRepository.findAll()
            : this.revenueConfigRepository.findAll(sort);

        return revenueConfigs.stream()
                             .map(RevenueConfigData::new)
                             .collect(Collectors.toList());
    }

    @Override
    public List<RevenueConfigData> getActiveAndPendingRevenueConfigs(Sort sort) {

        Sort effectiveSort = sort == null ? Sort.by(Sort.Direction.ASC, "taxCodeId") : sort;

        return this.revenueConfigRepository.findByStatusIn(ACTIVE_AND_PENDING_STATUSES, effectiveSort)
                                           .stream()
                                           .map(RevenueConfigData::new)
                                           .collect(Collectors.toList());
    }

    @Override
    public RevenueConfigData get(RevenueConfigId revenueConfigId) throws DomainException {

        return findById(revenueConfigId)
            .orElseThrow(() -> new RevenueConfigException(RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(
                revenueConfigId)));
    }

    @Override
    public Optional<RevenueConfigData> findById(RevenueConfigId revenueConfigId) {

        return this.revenueConfigRepository.findById(revenueConfigId)
                                           .map(RevenueConfigData::new);
    }

    @Override
    public Optional<RevenueConfigData> findByTaxCodeId(String taxCodeId) {

        return this.revenueConfigRepository.findByTaxCodeId(taxCodeId)
                                           .map(RevenueConfigData::new);
    }
}
