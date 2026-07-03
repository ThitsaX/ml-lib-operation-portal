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

import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.query.IAMQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetActionListByUser;
import com.thitsaworks.operation_portal.usecase.util.UserPermissionManager;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.ROLE_MENU_PERMISSION_IAM)
public class GetActionListByUserHandler
    extends OperationPortalUseCase<GetActionListByUser.Input, GetActionListByUser.Output>
    implements GetActionListByUser {

    private final IAMQuery iamQuery;

    private final UserPermissionManager userPermissionManager;

    private final ActionAuthorizationManager actionAuthorizationManager;

    public GetActionListByUserHandler(PrincipalCache principalCache,
                                      ActionAuthorizationManager actionAuthorizationManager,
                                      IAMQuery iamQuery,
                                      UserPermissionManager userPermissionManager) {

        super(principalCache, actionAuthorizationManager);

        this.iamQuery = iamQuery;
        this.userPermissionManager = userPermissionManager;
        this.actionAuthorizationManager = actionAuthorizationManager;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        var currentUser = this.userPermissionManager.getCurrentUser();

        var auditableActionNames = this.actionAuthorizationManager.findAuditableActions();

        List<Output.Action> actionList = this.iamQuery
                                             .getGrantedActionListByPrincipal(
                                                 currentUser.principalId())
                                             .stream()
                                             .filter(action -> auditableActionNames.contains(
                                                 action.actionCode().getValue()))
                                             .map(action -> new Output.Action(
                                                 action.actionId(),
                                                 action.actionCode().getValue()))
                                             .toList();

        return new Output(actionList);

    }

}