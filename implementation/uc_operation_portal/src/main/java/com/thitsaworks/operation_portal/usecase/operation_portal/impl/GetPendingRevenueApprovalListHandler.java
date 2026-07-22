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

import com.thitsaworks.operation_portal.component.common.identifier.PrincipalId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.ActionCode;
import com.thitsaworks.operation_portal.component.common.type.ApprovalTabCode;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.approval.data.ApprovalRequestData;
import com.thitsaworks.operation_portal.core.approval.query.ApprovalRequestQuery;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.iam.query.ActionQuery;
import com.thitsaworks.operation_portal.core.iam.query.IAMQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetPendingRevenueApprovalList;
import com.thitsaworks.operation_portal.usecase.util.UserPermissionManager;
import com.thitsaworks.operation_portal.usecase.util.Utility;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.APPROVAL_WORKFLOW)
public class GetPendingRevenueApprovalListHandler
    extends OperationPortalUseCase<GetPendingRevenueApprovalList.Input, GetPendingRevenueApprovalList.Output>
    implements GetPendingRevenueApprovalList {

    private static final Logger LOG = LoggerFactory.getLogger(
        GetPendingRevenueApprovalListHandler.class);

    private final ApprovalRequestQuery approvalRequestQuery;

    private final Utility utility;

    private final UserPermissionManager userPermissionManager;

    private final IAMQuery iamQuery;

    private final ActionQuery actionQuery;

    public GetPendingRevenueApprovalListHandler(PrincipalCache principalCache,
                                                ActionAuthorizationManager actionAuthorizationManager,
                                                ApprovalRequestQuery approvalRequestQuery,
                                                Utility utility,
                                                UserPermissionManager userPermissionManager,
                                                ActionQuery actionQuery,
                                                IAMQuery iamQuery) {

        super(principalCache, actionAuthorizationManager);

        this.approvalRequestQuery = approvalRequestQuery;
        this.utility = utility;
        this.userPermissionManager = userPermissionManager;
        this.actionQuery = actionQuery;
        this.iamQuery = iamQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        var currentUser = this.userPermissionManager.getCurrentUser();
        var principalId = currentUser.principalId();
        final List<ApprovalRequestData> requests;

        if (hasApprovalPermissions(principalId)) {

            requests = this.approvalRequestQuery.getPendingApprovalRequestsByTabCode(
                ApprovalTabCode.REVENUE.name());

        } else {

            var userId = new UserId(principalId.getId());
            requests = this.approvalRequestQuery.getPendingApprovalRequestsByRequestedIdAndTabCode(
                userId, ApprovalTabCode.REVENUE.name());
        }

        LOG.debug("Pending revenue approval request count : [{}]", requests.size());

        return new Output(requests.stream().map(request -> new Output.PendingApproval(
            request.getApprovalRequestId(), request.getRequestedAction(),
            request.getParticipantName(), request.getCurrency(),
            this.normalize(request.getAmount()),
            this.utility.getEmail(new UserId(request.getRequestedBy().getId())),
            request.getRequestedDtm(), request.getRespondedBy() == null ? null :
                                           this.utility.getEmail(
                                               new UserId(request.getRespondedBy().getId())),
            request.getRespondedDtm(), request.getAction(), request.getReason(),
            request.getRequestCategory(),
            request.getFieldDetails().stream().map(fieldDetail -> new Output.PendingApprovalDetail(
                fieldDetail.getTabCode(), fieldDetail.getFieldKey(), fieldDetail.getFieldLabel(),
                fieldDetail.getFieldValue(), this.normalizeText(fieldDetail.getBeforeValue()),
                this.normalizeText(fieldDetail.getAfterValue()), fieldDetail.getValueType(),
                fieldDetail.getDisplayOrder())).toList())).toList());
    }

    private BigDecimal normalize(BigDecimal value) {

        if (value == null) {
            return null;
        }

        var normalized = value.stripTrailingZeros();
        return normalized.scale() < 0 ? normalized.setScale(0) : normalized;
    }

    private String normalizeText(String value) {

        if (value == null) {
            return null;
        }

        try {
            return this.normalize(new BigDecimal(value)).toPlainString();
        } catch (NumberFormatException ex) {
            return value;
        }
    }

    private boolean hasApprovalPermissions(PrincipalId principalId) throws IAMException {

        final var grantedActions = this.iamQuery.getGrantedActionListByPrincipal(principalId);

        final var createRevenueApprovalRequest = this.actionQuery.get(
            new ActionCode("CreateRevenueApprovalRequest"));
        final var modifyRevenueApprovalAction = this.actionQuery.get(
            new ActionCode("ModifyRevenueApprovalAction"));

        if (grantedActions.contains(createRevenueApprovalRequest) &&
                grantedActions.contains(modifyRevenueApprovalAction)) {
            return true;

        } else {
            return !grantedActions.contains(createRevenueApprovalRequest) &&
                       grantedActions.contains(modifyRevenueApprovalAction);
        }

    }

}
