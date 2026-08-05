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
package com.thitsaworks.operation_portal.core.scheduler.query;

import com.thitsaworks.operation_portal.component.common.identifier.SchedulerConfigId;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.core.scheduler.data.SchedulerConfigData;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/**
 * Interface defining query operations for scheduler configurations.
 */
public interface SchedulerConfigQuery {

    /**
     * Get all scheduler configurations with optional sorting.
     *
     * @param sort the sort specification (can be null for no sorting)
     * @return list of scheduler configurations
     */
    List<SchedulerConfigData> getSchedulerConfigs(Sort sort);

    /**
     * Get all scheduler configurations filtered by active jobStatus with optional sorting.
     *
     * @param active filter by active jobStatus
     * @param sort the sort specification (can be null for no sorting)
     * @return list of filtered scheduler configurations
     */
    List<SchedulerConfigData> getSchedulerConfigs(boolean active, Sort sort);

    /**
     * Get a specific scheduler configuration by ID.
     *
     * @param schedulerConfigId the ID of the configuration to retrieve
     * @return the scheduler configuration data
     * @throws com.thitsaworks.operation_portal.component.misc.exception.ResourceNotFoundException if config not found
     */
    SchedulerConfigData get(SchedulerConfigId schedulerConfigId) throws DomainException;

    /**
     * Get a specific scheduler configuration by job name.
     *
     * @param jobName the job name of the configuration to retrieve
     * @return the scheduler configuration data
     * @throws com.thitsaworks.operation_portal.component.misc.exception.ResourceNotFoundException if config not found
     */
    SchedulerConfigData getByJobName(String jobName) throws DomainException;

    /**
     * Get a specific scheduler configuration by ID if it exists.
     *
     * @param schedulerConfigId the ID of the configuration to retrieve
     * @return an Optional containing the scheduler configuration if found, empty otherwise
     */
    Optional<SchedulerConfigData> findById(SchedulerConfigId schedulerConfigId);

    /**
     * Get a specific scheduler configuration by job name if it exists.
     *
     * @param jobName the job name of the configuration to retrieve
     * @return an Optional containing the scheduler configuration if found, empty otherwise
     */
    Optional<SchedulerConfigData> findByJobName(String jobName);
}
