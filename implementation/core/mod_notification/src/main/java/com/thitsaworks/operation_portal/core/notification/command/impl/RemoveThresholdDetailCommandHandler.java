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
package com.thitsaworks.operation_portal.core.notification.command.impl;

import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;
import com.thitsaworks.operation_portal.component.misc.exception.InputException;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.notification.command.RemoveThresholdDetailCommand;
import com.thitsaworks.operation_portal.core.notification.model.ThresholdDetail;
import com.thitsaworks.operation_portal.core.notification.model.repository.ThresholdDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RemoveThresholdDetailCommandHandler implements RemoveThresholdDetailCommand {

    private final ThresholdDetailRepository thresholdDetailRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) {

        ThresholdDetail detail =
            this.thresholdDetailRepository.findById(input.thresholdDetailId())
                                          .orElseThrow(() -> new InputException(
                                              new ErrorMessage(
                                                  "THRESHOLD_DETAIL_NOT_FOUND",
                                                  "Threshold detail was not found.")));

        detail.deactivate(input.updatedBy());
        this.thresholdDetailRepository.save(detail);

        return new Output(detail.getThresholdDetailId(), true);
    }
}
