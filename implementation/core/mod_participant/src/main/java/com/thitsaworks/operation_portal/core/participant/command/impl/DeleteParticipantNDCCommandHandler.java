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
package com.thitsaworks.operation_portal.core.participant.command.impl;

import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.participant.command.DeleteParticipantNDCCommand;
import com.thitsaworks.operation_portal.core.participant.model.ParticipantNDC;
import com.thitsaworks.operation_portal.core.participant.model.repository.ParticipantNDCRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeleteParticipantNDCCommandHandler implements DeleteParticipantNDCCommand {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteParticipantNDCCommandHandler.class);

    private final ParticipantNDCRepository participantNDCRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) {

        Optional<ParticipantNDC> optionalParticipantNDC =
                this.participantNDCRepository.findById(input.participantNDCId());

        boolean removed = false;

        if (!optionalParticipantNDC.isEmpty()) {

            this.participantNDCRepository.delete(optionalParticipantNDC.get());
            removed = true;
        }

        return new Output(removed);
    }

}
