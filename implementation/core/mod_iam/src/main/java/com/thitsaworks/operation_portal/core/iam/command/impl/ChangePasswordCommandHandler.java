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

import com.thitsaworks.operation_portal.component.common.identifier.AccessKey;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.command.ChangePasswordCommand;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.iam.exception.IAMIgnorableException;
import com.thitsaworks.operation_portal.core.iam.model.Principal;
import com.thitsaworks.operation_portal.core.iam.model.repository.PrincipalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangePasswordCommandHandler implements ChangePasswordCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ChangePasswordCommandHandler.class);

    private final PrincipalRepository principalRepository;

    private final PrincipalCache principalCache;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws IAMException, IAMIgnorableException {

        Principal principal = this.principalRepository.findByPrincipalId(input.principalId())
                                                      .orElseThrow(() -> new IAMException(
                                                              IAMErrors.PRINCIPAL_NOT_FOUND.format(input.principalId().getId().toString())));

        AccessKey oldAccessKey = principal.getAccessKey();

        principal.change(input.newPassword(), input.oldPassword());

        principalRepository.save(principal);

        principalCache.delete(oldAccessKey);

        return new Output(principal.getAccessKey(), principal.getSecretKey());
    }

}
