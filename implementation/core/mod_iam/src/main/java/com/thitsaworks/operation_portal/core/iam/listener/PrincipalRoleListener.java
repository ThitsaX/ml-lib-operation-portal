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
package com.thitsaworks.operation_portal.core.iam.listener;

import com.thitsaworks.operation_portal.core.iam.data.RoleData;
import com.thitsaworks.operation_portal.core.iam.engine.IAMEngine;
import com.thitsaworks.operation_portal.core.iam.model.PrincipalRole;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class PrincipalRoleListener {

    private static IAMEngine iamEngine;

    @Autowired
    private IAMEngine iamEngineInstance;

    @PostConstruct
    public void init() {

        PrincipalRoleListener.iamEngine = iamEngineInstance;
    }

    @PostPersist
    @PostUpdate
    public void onUserRolePersistOrUpdate(PrincipalRole principalRole) {

        var roleData = new RoleData(principalRole.getRole());
        var
            principalId =
            principalRole.getPrincipal()
                         .getPrincipalId();

        iamEngine.addPrincipalRole(principalId, roleData);

    }

    @PostRemove
    public void onUserRolePostRemove(PrincipalRole principalRole) {

        var roleData = new RoleData(principalRole.getRole());
        var
            principalId =
            principalRole.getPrincipal()
                         .getPrincipalId();

        iamEngine.removePrincipalRole(principalId, roleData);
    }

}
