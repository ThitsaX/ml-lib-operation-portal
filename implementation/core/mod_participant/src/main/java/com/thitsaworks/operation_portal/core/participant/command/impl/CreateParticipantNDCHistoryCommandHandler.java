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
import com.thitsaworks.operation_portal.core.participant.command.CreateParticipantNDCHistoryCommand;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.model.ParticipantNDCHistory;
import com.thitsaworks.operation_portal.core.participant.model.repository.ParticipantNDCHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateParticipantNDCHistoryCommandHandler implements CreateParticipantNDCHistoryCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CreateParticipantNDCHistoryCommandHandler.class);

    private final ParticipantNDCHistoryRepository participantNDCHistoryRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws ParticipantException {

        ParticipantNDCHistory participantNDCHistory = new ParticipantNDCHistory(input.participantNDC());

        this.participantNDCHistoryRepository.save(participantNDCHistory);

        return new Output(participantNDCHistory.getParticipantNDCHistoryId());
    }

}
