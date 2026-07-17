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
package com.thitsaworks.operation_portal.core.approval.command.impl;

import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.approval.command.CreateApprovalRequestFieldDetailCommand;
import com.thitsaworks.operation_portal.core.approval.model.ApprovalRequestFieldDetail;
import com.thitsaworks.operation_portal.core.approval.model.repository.ApprovalRequestFieldDetailRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateApprovalRequestFieldDetailCommandHandler implements CreateApprovalRequestFieldDetailCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CreateApprovalRequestFieldDetailCommandHandler.class);

    private final ApprovalRequestFieldDetailRepository approvalRequestFieldDetailRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) {

        var approvalRequestFieldDetail = new ApprovalRequestFieldDetail(input.approvalRequestId(),
                                                                        input.fieldKey(),
                                                                        input.fieldLabel(),
                                                                        input.fieldValue(),
                                                                        input.beforeValue(),
                                                                        input.afterValue(),
                                                                        input.valueType(),
                                                                        input.displayOrder(),
                                                                        input.tabCode());

        this.approvalRequestFieldDetailRepository.save(approvalRequestFieldDetail);

        return new Output(approvalRequestFieldDetail.getApprovalRequestFieldDetailId());
    }

}
