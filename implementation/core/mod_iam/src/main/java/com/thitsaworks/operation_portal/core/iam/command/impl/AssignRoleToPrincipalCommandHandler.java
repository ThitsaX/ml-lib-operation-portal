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
package com.thitsaworks.operation_portal.core.iam.command.impl;

import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.iam.command.AssignRoleToPrincipalCommand;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.iam.model.Principal;
import com.thitsaworks.operation_portal.core.iam.model.PrincipalRole;
import com.thitsaworks.operation_portal.core.iam.model.Role;
import com.thitsaworks.operation_portal.core.iam.model.repository.PrincipalRepository;
import com.thitsaworks.operation_portal.core.iam.model.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssignRoleToPrincipalCommandHandler implements AssignRoleToPrincipalCommand {

    private final PrincipalRepository principalRepository;

    private final RoleRepository roleRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws IAMException {

        Principal principal = this.principalRepository.findById(input.principalId())
                                                      .orElseThrow(() -> new IAMException(
                                                          IAMErrors.PRINCIPAL_NOT_FOUND.format(input.principalId()
                                                                                                    .getId().toString())));

        Role role = this.roleRepository.findById(input.roleId())
                                       .orElseThrow(() -> new IAMException(IAMErrors.ROLE_NOT_FOUND.format(input.roleId()
                                                                                                                .getId().toString())));

        PrincipalRole principalRole = principal.assignRole(role);

        this.principalRepository.saveAndFlush(principal);

        return new Output(principalRole.getPrincipalRoleId());
    }

}

