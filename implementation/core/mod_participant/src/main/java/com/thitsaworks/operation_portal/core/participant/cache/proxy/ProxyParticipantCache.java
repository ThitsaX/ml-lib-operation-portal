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
package com.thitsaworks.operation_portal.core.participant.cache.proxy;

import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.component.misc.spring.CacheQualifiers;
import com.thitsaworks.operation_portal.core.participant.cache.ParticipantCache;
import com.thitsaworks.operation_portal.core.participant.data.ParticipantData;
import com.thitsaworks.operation_portal.core.participant.model.Participant;
import com.thitsaworks.operation_portal.core.participant.model.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Primary
@Component
@Qualifier(CacheQualifiers.PROXY)
@CoreReadTransactional
public class ProxyParticipantCache implements ParticipantCache {

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    @Qualifier(CacheQualifiers.REDIS)
    private ParticipantCache participantCache;

    @Override
    public void save(ParticipantData participantData) {

        this.participantCache.save(participantData);
    }

    @Override
    public ParticipantData get(ParticipantId participantId) {

        ParticipantData participantData = this.participantCache.get(participantId);

        if (participantData == null) {

            Optional<Participant> optionalParticipant = this.participantRepository.findById(participantId);

            if (optionalParticipant.isEmpty()) {

                return null;
            }

            Participant participant = optionalParticipant.get();

            participantData = new ParticipantData(participant);

            this.participantCache.save(participantData);
        }

        return participantData;
    }


    @Override
    public void delete(ParticipantId participantId) {

        this.participantCache.delete(participantId);

    }

}
