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
package com.thitsaworks.operation_portal.usecase.operation_portal;

import com.thitsaworks.operation_portal.component.misc.usecase.UseCase;
import com.thitsaworks.operation_portal.core.scheduler.data.SchedulerConfigData;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

/**
 * Use case for retrieving scheduler configurations with optional filtering and sorting.
 * <p>
 * This use case supports:
 * <ul>
 *     <li>Filtering by active jobStatus</li>
 *     <li>Sorting by any field with configurable direction</li>
 * </ul>
 */
public interface GetSchedulerConfigList
    extends UseCase<GetSchedulerConfigList.Input, GetSchedulerConfigList.Output> {

    /**
     * The default field to sort by when none is specified.
     */
    String DEFAULT_SORT_FIELD = "name";

    /**
     * Input parameters for the use case.
     *
     * @param active        optional filter for active/inactive jobStatus
     * @param sortBy        the field to sort by, defaults to {@value #DEFAULT_SORT_FIELD}
     * @param sortDirection the sort direction, defaults to ASC
     */
    record Input(
        Boolean active,
        String sortBy,
        Sort.Direction sortDirection
    ) {
        /**
         * Creates a new Input with default values for any null parameters.
         */
        public Input {
            sortBy = sortBy != null ? sortBy : DEFAULT_SORT_FIELD;
            sortDirection = sortDirection != null ? sortDirection : Sort.Direction.ASC;
        }
    }

    /**
     * Output containing the list of scheduler configurations.
     *
     * @param configs the list of scheduler configurations
     */
    record Output(
        @NotNull List<@NotNull SchedulerConfigData> configs
    ) {
        /**
         * Creates a new Output with the provided values.
         *
         * @throws IllegalArgumentException if configs is null or contains null elements
         */
        public Output {
            if (configs == null) {
                throw new IllegalArgumentException("Configs list cannot be null");
            }
            if (configs.stream().anyMatch(c -> c == null)) {
                throw new IllegalArgumentException("Configs list cannot contain null elements");
            }
        }
    }
}
