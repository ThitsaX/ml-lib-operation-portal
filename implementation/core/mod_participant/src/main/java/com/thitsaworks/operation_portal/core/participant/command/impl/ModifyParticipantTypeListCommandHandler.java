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
import com.thitsaworks.operation_portal.core.participant.command.ModifyParticipantTypeListCommand;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.model.Participant;
import com.thitsaworks.operation_portal.core.participant.model.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModifyParticipantTypeListCommandHandler implements ModifyParticipantTypeListCommand {

    private final ParticipantRepository participantRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws ParticipantException {

        List<Participant> participants = new ArrayList<>();
        List<ParticipantTypeInfo> participantTypeInfoList = new ArrayList<>();

        for (var participantTypeInfo : input.participantTypeInfoList()) {

            var participant =
                this.participantRepository.findByParticipantName(participantTypeInfo.participantName())
                                          .orElseThrow(
                                              () -> new ParticipantException(ParticipantErrors.PARTICIPANT_NOT_FOUND
                                                                                 .format(participantTypeInfo.participantName()
                                                                                                            .getValue())));

            participant.participantType(participantTypeInfo.participantType());
            participants.add(participant);
            participantTypeInfoList.add(participantTypeInfo);
        }

        this.participantRepository.saveAll(participants);

        return new Output(participantTypeInfoList);
    }

}
