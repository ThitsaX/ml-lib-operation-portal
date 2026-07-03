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
import com.thitsaworks.operation_portal.core.iam.data.RoleData;
import com.thitsaworks.operation_portal.core.iam.query.RoleQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetRoleListByParticipant;
import com.thitsaworks.operation_portal.usecase.util.UserPermissionManager;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.ROLE_MENU_PERMISSION_IAM)
public class GetRoleListByParticipantHandler
    extends OperationPortalUseCase<GetRoleListByParticipant.Input, GetRoleListByParticipant.Output>
    implements GetRoleListByParticipant {

    private static final Logger LOG = LoggerFactory.getLogger(
        GetRoleListByParticipantHandler.class);

    private final RoleQuery roleQuery;

    private final UserPermissionManager userPermissionManager;

    public GetRoleListByParticipantHandler(PrincipalCache principalCache,
                                           ActionAuthorizationManager actionAuthorizationManager,
                                           RoleQuery roleQuery,
                                           UserPermissionManager userPermissionManager) {

        super(principalCache, actionAuthorizationManager);

        this.roleQuery = roleQuery;
        this.userPermissionManager = userPermissionManager;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        var currentUser = this.userPermissionManager.getCurrentUser();

        List<RoleData> roleList = this.roleQuery.getAll();

        boolean isDfspUser = this.userPermissionManager.isDfsp(currentUser.principalId());

        if (isDfspUser || !input.participantName().equalsIgnoreCase("hub")) {

            roleList = roleList.stream().filter(RoleData::isDfsp).toList();

        } else {

            roleList = roleList
                           .stream()
                           .filter(role -> !role.isDfsp() &&
                                               !role.name().equalsIgnoreCase("SYSTEM-Admin"))
                           .toList();
        }

        return new Output(roleList);
    }

}
