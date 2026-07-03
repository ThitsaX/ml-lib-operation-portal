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
package com.thitsaworks.operation_portal.core.audit.command.impl;

import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.exception.AuditErrors;
import com.thitsaworks.operation_portal.core.audit.exception.AuditException;
import com.thitsaworks.operation_portal.core.audit.model.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateOutputAuditCommandHandler implements CreateOutputAuditCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateOutputAuditCommandHandler.class);

    private final AuditRepository auditRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws AuditException {

        var
            audit =
            this.auditRepository.findById(input.auditId())
                                .orElseThrow(() -> new AuditException(AuditErrors.AUDIT_NOT_FOUND.format(input.auditId().getId().toString())));

        audit.outputInfo(input.outputInfo());

        this.auditRepository.saveAndFlush(audit);

        return new Output();

    }

}
