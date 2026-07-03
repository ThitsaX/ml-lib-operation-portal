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
import com.thitsaworks.operation_portal.core.iam.command.CreatePrincipalCommand;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.iam.model.Principal;
import com.thitsaworks.operation_portal.core.iam.model.repository.PrincipalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreatePrincipalCommandHandler implements CreatePrincipalCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CreatePrincipalCommandHandler.class);

    private final PrincipalRepository principalRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws IAMException {

        Optional<Principal> optionalPrincipal = this.principalRepository.findByPrincipalId(input.principalId());

        if (optionalPrincipal.isPresent()) {

            throw new IAMException(IAMErrors.DUPLICATE_PRINCIPAL.format(input.principalId().getId().toString()));
        }

        Principal newPrincipal = new Principal(input.principalId(),
                                               input.passwordPlain(),
                                               input.realmId(),
                                               input.principalStatus());

        this.principalRepository.save(newPrincipal);

        return new Output(newPrincipal.getAccessKey(), newPrincipal.getSecretKey());
    }

}
