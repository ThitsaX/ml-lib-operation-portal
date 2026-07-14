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

import com.thitsaworks.operation_portal.component.common.identifier.ThresholdConfigurationId;
import com.thitsaworks.operation_portal.component.common.type.NdcConfigurationStatus;
import com.thitsaworks.operation_portal.component.common.type.ThresholdScopeType;
import com.thitsaworks.operation_portal.component.misc.usecase.UseCase;

import java.util.List;

public interface GetNdcThresholdConfigurationList
    extends UseCase<GetNdcThresholdConfigurationList.Input, GetNdcThresholdConfigurationList.Output> {

    record Input() { }

    record Output(List<NdcThresholdConfiguration> configurations) { }

    record NdcThresholdConfiguration(
            ThresholdConfigurationId thresholdConfigurationId,
            ThresholdScopeType scopeType,
            String schemeId,
            String dfspId,
            boolean thresholdEnabled,
            NdcConfigurationStatus status,
            String createdBy,
            String updatedBy
    ) {}

}
