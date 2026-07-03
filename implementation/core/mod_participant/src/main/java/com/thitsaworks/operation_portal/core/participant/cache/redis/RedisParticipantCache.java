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
package com.thitsaworks.operation_portal.core.participant.cache.redis;

import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.misc.spring.CacheQualifiers;
import com.thitsaworks.operation_portal.core.participant.cache.ParticipantCache;
import com.thitsaworks.operation_portal.core.participant.data.ParticipantData;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier(CacheQualifiers.REDIS)
@RequiredArgsConstructor
public class RedisParticipantCache implements ParticipantCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisParticipantCache.class);

    private static final String WITH_ID = "rd_participant_with_id";

    private final RedissonClient redissonClient;

    @Override
    public void save(ParticipantData participantData) {

        RMapCache<Long, ParticipantData> withId = this.redissonClient.getMapCache(WITH_ID);

        withId.remove(participantData.participantId()
                                     .getEntityId());

        withId.put(participantData.participantId()
                                  .getEntityId(), participantData);
    }

    @Override
    public ParticipantData get(ParticipantId participantId) {

        RMapCache<Long, ParticipantData> withId = this.redissonClient.getMapCache(WITH_ID);

        return withId.get(participantId.getId());
    }

    @Override
    public void delete(ParticipantId participantId) {

        RMapCache<Long, ParticipantData> withId = this.redissonClient.getMapCache(WITH_ID);

        withId.remove(participantId.getId());
    }

}
