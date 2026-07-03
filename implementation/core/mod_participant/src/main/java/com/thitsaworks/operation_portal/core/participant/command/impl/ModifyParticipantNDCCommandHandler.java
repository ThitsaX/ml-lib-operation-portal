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
import com.thitsaworks.operation_portal.core.participant.command.ModifyParticipantNDCCommand;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantNDCException;
import com.thitsaworks.operation_portal.core.participant.model.ParticipantNDC;
import com.thitsaworks.operation_portal.core.participant.model.repository.ParticipantNDCRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModifyParticipantNDCCommandHandler implements ModifyParticipantNDCCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyParticipantNDCCommandHandler.class);

    private final ParticipantNDCRepository participantNDCRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws ParticipantNDCException {

        ParticipantNDC
                existingParticipantNDC = this.participantNDCRepository.findById(input.participantNDCId())
                                                                      .orElseThrow(() -> new ParticipantNDCException(
                                                                              ParticipantErrors.PARTICIPANT_NDC_NOT_FOUND.format(input.participantNDCId().getId().toString())));

        this.participantNDCRepository.save(existingParticipantNDC.ndcPercent(input.ndcPercent())
                                                                 .updatedAt()
                                                                 .ndcAmount(input.ndcAmount())
                                                                 .balance(input.balance())
                                                                 .madeBy(input.madeBy()));

        return new Output(existingParticipantNDC.getParticipantNDCId());
    }

}
