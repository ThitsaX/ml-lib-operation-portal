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

package com.thitsaworks.operation_portal.usecase.util;

import com.thitsaworks.operation_portal.component.common.identifier.AccessKey;
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.common.identifier.PrincipalId;
import com.thitsaworks.operation_portal.component.common.identifier.RoleId;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.security.SecurityContext;
import com.thitsaworks.operation_portal.component.misc.usecase.UseCaseContext;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.data.PrincipalData;
import com.thitsaworks.operation_portal.core.iam.data.RoleData;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.iam.query.RoleQuery;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPermissionManager {

    private static final Logger LOG = LoggerFactory.getLogger(UserPermissionManager.class);

    private static final String HUB_PARTICIPANT_TYPE = "HUB";

    private static final String DFSP_ROLE_TYPE = "DFSP";

    private static final String INDIRECT_DFSP_ROLE_TYPE = "INDIRECT_DFSP";

    private final RoleQuery roleQuery;

    private final ParticipantQuery participantQuery;

    private final PrincipalCache principalCache;

    public boolean isDfsp(PrincipalId principalId) throws DomainException {

        PrincipalData principal = this.principalCache.get(principalId);

        if (principal == null) {
            throw new IAMException(IAMErrors.PRINCIPAL_NOT_FOUND.format(principalId.toString()));
        }

        var participant = this.participantQuery.get(new ParticipantId(principal.realmId().getId()));

        return !this.isHubParticipant(participant.participantType());

    }

    public boolean areRolesAllowed(boolean isDfsp,
                                   ParticipantId participantId,
                                   List<RoleId> roleIdList)
        throws IAMException, ParticipantException {

        List<RoleData> roleList = this.roleQuery.getAll();

        if (isDfsp) {

            String participantType = this.participantQuery.get(participantId).participantType();

            boolean isIndirectParticipant =
                participantType != null && this.isIndirectParticipant(participantId);

            Set<String> allowedRoleTypes =
                isIndirectParticipant ? Set.of(DFSP_ROLE_TYPE, INDIRECT_DFSP_ROLE_TYPE) :
                    Set.of(DFSP_ROLE_TYPE);

            roleList = roleList
                           .stream()
                           .filter(role -> role.roleType() != null &&
                                               allowedRoleTypes.contains(role.roleType()))
                           .toList();
        }

        Set<String> allowedRoleIds = roleList
                                         .stream()
                                         .map(r -> r.roleId().getId().toString())
                                         .collect(Collectors.toSet());

        return roleIdList != null && !roleIdList.isEmpty() && roleIdList
                                                                  .stream()
                                                                  .allMatch(
                                                                      roleId -> roleId != null &&
                                                                                    allowedRoleIds.contains(
                                                                                        roleId
                                                                                            .getId()
                                                                                            .toString()));

    }

    public PrincipalData getCurrentUser() throws IAMException {

        SecurityContext securityContext = (SecurityContext) UseCaseContext.get();

        PrincipalData currentUser = this.principalCache.get(
            new AccessKey(securityContext.accessKey()));

        if (currentUser == null) {
            throw new IAMException(
                IAMErrors.PRINCIPAL_NOT_FOUND.format(securityContext.userId().toString()));

        }

        return currentUser;
    }

    public boolean isSameParticipant(ParticipantId loggedInUserParticipantId,
                                     ParticipantId requestParticipantId) {

        return loggedInUserParticipantId.equals(requestParticipantId);
    }

    private boolean isHubParticipant(String participantType) {

        return participantType != null && participantType.equalsIgnoreCase(HUB_PARTICIPANT_TYPE);
    }

    private boolean isDfspRole(RoleData role) {

        return role.roleType() != null && role.roleType().equalsIgnoreCase(DFSP_ROLE_TYPE);
    }

    public boolean isIndirectParticipant(ParticipantId participantId) throws ParticipantException {

        return !this.participantQuery.isDirectParticipant(participantId);
    }

}
