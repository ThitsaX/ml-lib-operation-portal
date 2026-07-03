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
package com.thitsaworks.operation_portal.core.iam.cache.redis;

import com.thitsaworks.operation_portal.component.common.identifier.AccessKey;
import com.thitsaworks.operation_portal.component.common.identifier.PrincipalId;
import com.thitsaworks.operation_portal.component.misc.spring.CacheQualifiers;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.data.PrincipalData;
import com.thitsaworks.operation_portal.core.iam.model.Principal;
import com.thitsaworks.operation_portal.core.iam.model.repository.PrincipalRepository;
import jakarta.annotation.PostConstruct;
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
public class RedisPrincipalCache implements PrincipalCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisPrincipalCache.class);

    private static final String WITH_ID = "rd_principal_with_id";

    private static final String WITH_ACCESS_KEY = "rd_principal_with_access_key";

    private final PrincipalRepository principalRepository;

    private final RedissonClient redissonClient;

    @Override
    public void save(PrincipalData principalData) {

        RMapCache<Long, PrincipalData> withId = this.redissonClient.getMapCache(WITH_ID);
        RMapCache<Long, PrincipalData> withAccessKey = this.redissonClient.getMapCache(WITH_ACCESS_KEY);

        var deleted = withId.remove(principalData.principalId()
                                                 .getEntityId());

        if (deleted != null) {
            withAccessKey.remove(deleted.accessKey()
                                        .getEntityId());
        }

        withId.put(principalData.principalId()
                                .getEntityId(), principalData);

        withAccessKey.put(principalData.accessKey()
                                       .getId(), principalData);

    }

    @Override
    public PrincipalData get(AccessKey accessKey) {

        RMapCache<Long, PrincipalData> withAccessKey = this.redissonClient.getMapCache(WITH_ACCESS_KEY);

        return withAccessKey.get(accessKey.getId());
    }

    @Override
    public PrincipalData get(PrincipalId principalId) {

        RMapCache<Long, PrincipalData> withId = this.redissonClient.getMapCache(WITH_ID);

        return withId.get(principalId.getId());

    }

    @Override
    public void delete(AccessKey accessKey) {

        RMapCache<Long, PrincipalData> withId = this.redissonClient.getMapCache(WITH_ID);
        RMapCache<Long, PrincipalData> withAccessKey = this.redissonClient.getMapCache(WITH_ACCESS_KEY);

        PrincipalData deleted = null;

        deleted = withAccessKey.remove(accessKey.getId());

        if (deleted != null) {

            withId.remove(deleted.principalId()
                                 .getId());
        }

    }

    @PostConstruct
    private void postConstruct() {

        for (Principal principal : this.principalRepository.findAll()) {

            this.save(new PrincipalData(principal));
        }
    }

}
