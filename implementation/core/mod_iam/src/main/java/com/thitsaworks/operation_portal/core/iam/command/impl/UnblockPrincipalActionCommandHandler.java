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
import com.thitsaworks.operation_portal.core.iam.command.UnblockPrincipalActionCommand;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.iam.model.Action;
import com.thitsaworks.operation_portal.core.iam.model.Principal;
import com.thitsaworks.operation_portal.core.iam.model.repository.ActionRepository;
import com.thitsaworks.operation_portal.core.iam.model.repository.PrincipalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnblockPrincipalActionCommandHandler implements UnblockPrincipalActionCommand {

    private final PrincipalRepository principalRepository;

    private final ActionRepository actionRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws IAMException {

        Optional<Principal> optPrincipal = this.principalRepository.findById(input.principalId());

        if (optPrincipal.isEmpty()) {
            throw new IAMException(IAMErrors.PRINCIPAL_NOT_FOUND.format(input.principalId()
                                                                             .getId().toString()));
        }

        Optional<Action> optAction = this.actionRepository.findById(input.actionId());

        if (optAction.isEmpty()) {
            throw new IAMException(IAMErrors.ACTION_NOT_FOUND.format(input.actionId()
                                                                          .getId().toString()));
        }

        Principal principal = optPrincipal.get();
        var unBlocked = principal.unblockAction(optAction.get());

        this.principalRepository.saveAndFlush(principal);

        return new Output(true, unBlocked);
    }

}
