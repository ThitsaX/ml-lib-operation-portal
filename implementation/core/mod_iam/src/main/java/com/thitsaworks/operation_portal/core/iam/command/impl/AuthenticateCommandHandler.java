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

import com.thitsaworks.operation_portal.component.common.type.PrincipalStatus;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.iam.command.AuthenticateCommand;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.iam.exception.IAMIgnorableException;
import com.thitsaworks.operation_portal.core.iam.model.Principal;
import com.thitsaworks.operation_portal.core.iam.model.SecurityToken;
import com.thitsaworks.operation_portal.core.iam.model.repository.PrincipalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticateCommandHandler implements AuthenticateCommand {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticateCommandHandler.class);

    private final PrincipalRepository principalRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws IAMException, IAMIgnorableException {

        Principal principal = this.principalRepository.findByPrincipalId(input.principalId())
                                                      .orElseThrow(() -> new IAMException(
                                                              IAMErrors.PRINCIPAL_NOT_FOUND.format(input.principalId().getId().toString())));

        if (principal.getPrincipalStatus()
                     .equals(PrincipalStatus.INACTIVE)) {

            throw new IAMException(IAMErrors.PRINCIPAL_NOT_FOUND.format(input.principalId().getId().toString()));
        }
        SecurityToken securityToken = principal.authenticate(input.passwordPlain());

        this.principalRepository.save(principal);

        return new AuthenticateCommand.Output(securityToken);
    }

}
