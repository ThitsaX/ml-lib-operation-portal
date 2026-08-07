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

import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.data.RoleData;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.iam.query.RoleQuery;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetRoleListByParticipant;
import com.thitsaworks.operation_portal.usecase.util.UserPermissionManager;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.ROLE_MENU_PERMISSION_IAM)
public class GetRoleListByParticipantHandler
    extends OperationPortalUseCase<GetRoleListByParticipant.Input, GetRoleListByParticipant.Output>
    implements GetRoleListByParticipant {

    private static final String LRA_PARTICIPANT_TYPE = "LRA";

    private static final String HUB_PARTICIPANT_TYPE = "HUB";

    private static final String DFSP_ROLE_TYPE = "DFSP";

    private static final String HUB_ROLE_TYPE = "HUB";

    private static final String LRA_ROLE_TYPE = "LRA";

    private static final String INDIRECT_PARTICIPANT_TYPE = "INDIRECT";

    private static final String INDIRECT_DFSP_ROLE_TYPE = "INDIRECT_DFSP";

    private final RoleQuery roleQuery;

    private final ParticipantQuery participantQuery;

    private final UserPermissionManager userPermissionManager;

    public GetRoleListByParticipantHandler(PrincipalCache principalCache,
                                           ActionAuthorizationManager actionAuthorizationManager,
                                           RoleQuery roleQuery,
                                           ParticipantQuery participantQuery,
                                           UserPermissionManager userPermissionManager) {

        super(principalCache, actionAuthorizationManager);

        this.roleQuery = roleQuery;
        this.participantQuery = participantQuery;
        this.userPermissionManager = userPermissionManager;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        var currentUser = this.userPermissionManager.getCurrentUser();

        var participantData = this.participantQuery.get(input.participantName());

        if (participantData.isEmpty()) {
            throw new ParticipantException(ParticipantErrors.PARTICIPANT_NOT_FOUND);
        }

        var participantType = participantData.get().participantType();
        var participantId = participantData.get().participantId();

        if (this.userPermissionManager.isIndirectParticipant(participantId)) {
            participantType = "INDIRECT";
        }

        List<RoleData> roleList = this.roleQuery.getAll();

        boolean isDfspUser = this.userPermissionManager.isDfsp(currentUser.principalId());

        if (isDfspUser) {
            ParticipantId currentUserParticipantId = new ParticipantId(
                currentUser.realmId().getId());
            if (!currentUserParticipantId.equals(participantId)) {
                throw new IAMException(IAMErrors.UNAUTHORIZED_ROLE_LIST_ACCESS);
            }
        }

        roleList = isDfspUser ? this.filterRolesForDfspUser(roleList, participantType) :
                       this.filterRolesForHubUser(roleList, participantType);

        return new Output(roleList);
    }

    private List<RoleData> filterRolesForDfspUser(List<RoleData> roleList, String participantType) {

        return switch (this.normalizedParticipantType(participantType)) {
            case LRA_PARTICIPANT_TYPE -> roleList.stream().filter(this::isDfspOrLraRole).toList();
            case INDIRECT_PARTICIPANT_TYPE ->
                roleList.stream().filter(this::isDfspOrIndirectRole).toList();
            default -> roleList.stream().filter(this::isDfspRole).toList();
        };
    }

    private List<RoleData> filterRolesForHubUser(List<RoleData> roleList, String participantType) {

        return switch (this.normalizedParticipantType(participantType)) {
            case LRA_PARTICIPANT_TYPE -> roleList.stream().filter(this::isDfspOrLraRole).toList();
            case HUB_PARTICIPANT_TYPE ->
                roleList.stream().filter(this::isAssignableHubRole).toList();
            case INDIRECT_PARTICIPANT_TYPE ->
                roleList.stream().filter(this::isDfspOrIndirectRole).toList();
            default -> roleList.stream().filter(this::isDfspRole).toList();
        };
    }

    private boolean isDfspOrLraRole(RoleData role) {
        return this.isDfspRole(role) || this.isLraRole(role);
    }

    private boolean isDfspRole(RoleData role) {
        return this.isRoleType(role, DFSP_ROLE_TYPE);
    }

    private boolean isLraRole(RoleData role) {
        return this.isRoleType(role, LRA_ROLE_TYPE);
    }

    private boolean isIndirectRole(RoleData role) {
        return role.roleType() != null && role.roleType().equalsIgnoreCase(INDIRECT_DFSP_ROLE_TYPE);
    }

    private boolean isDfspOrIndirectRole(RoleData role) {
        return this.isDfspRole(role) || this.isIndirectRole(role);
    }

    private boolean isAssignableHubRole(RoleData role) {
        return this.isRoleType(role, HUB_ROLE_TYPE) || this.isRoleType(role, LRA_ROLE_TYPE);
    }

    private boolean isRoleType(RoleData role, String roleType) {
        return role.roleType() != null && role.roleType().equalsIgnoreCase(roleType);
    }

    private String normalizedParticipantType(String participantType) {

        return participantType == null ? "" : participantType.trim().toUpperCase();
    }

}
