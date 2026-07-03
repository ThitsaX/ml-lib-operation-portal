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
package com.thitsaworks.operation_portal.core.iam.engine;

import com.thitsaworks.operation_portal.component.common.identifier.ActionId;
import com.thitsaworks.operation_portal.component.common.identifier.PrincipalId;
import com.thitsaworks.operation_portal.component.common.identifier.RoleId;
import com.thitsaworks.operation_portal.component.common.type.ActionCode;
import com.thitsaworks.operation_portal.core.iam.data.ActionData;
import com.thitsaworks.operation_portal.core.iam.data.EngineData;
import com.thitsaworks.operation_portal.core.iam.data.PrincipalData;
import com.thitsaworks.operation_portal.core.iam.data.RoleData;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAMEngine {

    void bootstrap();

    void print();

    EngineData dumpEngineState();

    List<ActionData> getActions();

    ActionData getAction(ActionCode actionCode);

    boolean isGrantedAction(PrincipalId principalId, ActionCode actionCode);

    boolean isGrantedAction(PrincipalId principalId, ActionId actionId);

    List<RoleData> getRolesByPrincipal(PrincipalId principalId);

    PrincipalData getPrincipal(PrincipalId principalId);

    void addPrincipal(PrincipalId principalId, PrincipalData principalData);

    void removePrincipal(PrincipalId principalId, PrincipalData principalData);

    void addPrincipalRole(PrincipalId principalId, RoleData roleData);

    void removePrincipalRole(PrincipalId principalId, RoleData roleData);

    void addAction(ActionId actionId, ActionCode actionCode, ActionData actionData);

    void removeAction(ActionId actionId, ActionCode actionCode, ActionData actionData);

    void addRoleGrantedAction(RoleId roleId, ActionData actionData);

    void removeRoleGrantedAction(RoleId roleId, ActionData actionData);

    void addPrincipalGrantedAction(PrincipalId principalId, ActionData actionData);

    void removePrincipalGrantedAction(PrincipalId principalId, ActionData actionData);

    void addPrincipalDeniedAction(PrincipalId principalId, ActionData actionData);

    void removePrincipalDeniedAction(PrincipalId principalId, ActionData actionData);

    Map<PrincipalId, PrincipalData> getPrincipalsMap();

    Map<PrincipalId, Set<RoleData>> getPrincipalRolesMap();

    Map<ActionCode, ActionData> getActionCodesMap();

    Map<ActionId, ActionData> getActionIdsMap();

    Map<RoleId, Set<ActionData>> getRoleGrantedActionsMap();

    Map<PrincipalId, Set<ActionData>> getPrincipalGrantedActionsMap();

    Map<PrincipalId, Set<ActionData>> getPrincipalDeniedActionsMap();

}
