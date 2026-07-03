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
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.model.Audit;
import com.thitsaworks.operation_portal.core.audit.model.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateInputAuditCommandHandler implements CreateInputAuditCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateInputAuditCommandHandler.class);

    private final AuditRepository auditRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) {

        Audit audit = new Audit(input.actionId(),
                                input.actionBy(),
                                input.realmId(),
                                input.traceId(),
                                input.inputInfo(),
                                null);

        this.auditRepository.save(audit);

        return new Output(audit.getAuditId());
    }

}
