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
import com.thitsaworks.operation_portal.core.approval.command.ModifyApprovalActionCommand;
import com.thitsaworks.operation_portal.core.approval.exception.ApprovalErrors;
import com.thitsaworks.operation_portal.core.approval.exception.ApprovalException;
import com.thitsaworks.operation_portal.core.approval.model.repository.ApprovalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModifyApprovalActionCommandHandler implements ModifyApprovalActionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyApprovalActionCommandHandler.class);

    private final ApprovalRequestRepository approvalRequestRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws ApprovalException {

        var
            approvalRequest = this.approvalRequestRepository.findById(input.approvalRequestId())
                                                            .orElseThrow(() -> new ApprovalException(
                                                                    ApprovalErrors.APPROVAL_REQUEST_NOT_FOUND.format(input.approvalRequestId().getId().toString())));

        approvalRequest.action(input.action());
        approvalRequest.respondedBy(input.respondedBy());
        approvalRequest.respondedDtm();

        this.approvalRequestRepository.saveAndFlush(approvalRequest);

        return new Output(approvalRequest.getApprovalRequestId());
    }

}
