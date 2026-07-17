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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.ApprovalActionType;
import com.thitsaworks.operation_portal.component.common.type.ApprovalTabCode;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.approval.command.ModifyApprovalActionCommand;
import com.thitsaworks.operation_portal.core.approval.data.ApprovalRequestData;
import com.thitsaworks.operation_portal.core.approval.exception.ApprovalErrors;
import com.thitsaworks.operation_portal.core.approval.exception.ApprovalException;
import com.thitsaworks.operation_portal.core.approval.query.ApprovalRequestQuery;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.Approval.AmountApprovalActionHandler;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyApprovalAction;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.ConnectException;

@Service
@ActionMetadata(category = ActionCategory.APPROVAL_WORKFLOW)
public class ModifyApprovalActionHandler
    extends OperationPortalAuditableUseCase<ModifyApprovalAction.Input, ModifyApprovalAction.Output>
    implements ModifyApprovalAction {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyApprovalActionHandler.class);

    private final ObjectMapper objectMapper;

    private final ModifyApprovalActionCommand modifyApprovalActionCommand;

    private final ApprovalRequestQuery approvalRequestQuery;

    private final AmountApprovalActionHandler amountApprovalActionHandler;

    public ModifyApprovalActionHandler(CreateInputAuditCommand createInputAuditCommand,
                                       CreateOutputAuditCommand createOutputAuditCommand,
                                       CreateExceptionAuditCommand createExceptionAuditCommand,
                                       ObjectMapper objectMapper,
                                       PrincipalCache principalCache,
                                       ActionAuthorizationManager actionAuthorizationManager,
                                       ModifyApprovalActionCommand modifyApprovalActionCommand,
                                       ApprovalRequestQuery approvalRequestQuery,
                                       AmountApprovalActionHandler amountApprovalActionHandler) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.modifyApprovalActionCommand = modifyApprovalActionCommand;
        this.approvalRequestQuery = approvalRequestQuery;
        this.amountApprovalActionHandler = amountApprovalActionHandler;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Output onExecute(Input input)
        throws DomainException, ConnectException, JsonProcessingException {

        LOG.info(
            "Get Pending Approval Request by Id Query Request : approvalRequestId : {}",
            input.approvalRequestId());

        var approvalRequestData = this.approvalRequestQuery.getPendingApprovalRequestByID(
            input.approvalRequestId());

        LOG.info(
            "Get Pending Approval Response By Id Query Response : {}",
            this.objectMapper.writeValueAsString(approvalRequestData));

        if (this.isSelfApprovalAttempt(
            approvalRequestData.getRequestedBy(), input.responseUserId())) {
            throw new IAMException(IAMErrors.SELF_APPROVAL_NOT_ALLOWED);
        }

        ModifyApprovalActionCommand.Output output;

        if (input.action().equals(ApprovalActionType.REJECTED)) {

            output = this.executeApprovalAction(input);

            return new Output(output.approvalRequestId());
        }

        // Add conditions here for other Tab Code
        if (ApprovalTabCode.AMOUNT.equals(this.getApprovalTab(approvalRequestData))) {
            this.amountApprovalActionHandler.execute(input, approvalRequestData);
        }

        output = this.executeApprovalAction(input);

        return new Output(output.approvalRequestId());
    }

    private boolean isSelfApprovalAttempt(UserId requestedByUserId, UserId respondedByUserId) {

        return requestedByUserId.getId().equals(respondedByUserId.getId());
    }

    private ApprovalTabCode getApprovalTab(ApprovalRequestData approvalRequestData)
        throws ApprovalException {

        if (approvalRequestData.getFieldDetails() == null ||
                approvalRequestData.getFieldDetails().isEmpty()) {
            return ApprovalTabCode.AMOUNT;
        }

        for (var fieldDetail : approvalRequestData.getFieldDetails()) {
            try {
                return ApprovalTabCode.valueOf(fieldDetail.getTabCode());
            } catch (IllegalArgumentException e) {
                throw new ApprovalException(
                    ApprovalErrors.INVALID_APPROVAL_TAB_CODE.format(fieldDetail.getTabCode()));
            }
        }

        return null;
    }

    private ModifyApprovalActionCommand.Output executeApprovalAction(Input input)
        throws ApprovalException {

        return this.modifyApprovalActionCommand.execute(
            new ModifyApprovalActionCommand.Input(
                input.approvalRequestId(), input.action(),
                input.responseUserId()));
    }

}
