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

import com.thitsaworks.operation_portal.component.common.identifier.LiquidityProfileId;
import com.thitsaworks.operation_portal.component.misc.spring.CacheQualifiers;
import com.thitsaworks.operation_portal.core.participant.cache.LiquidityProfileCache;
import com.thitsaworks.operation_portal.core.participant.data.LiquidityProfileData;
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
public class RedisLiquidityProfileCache implements LiquidityProfileCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLiquidityProfileCache.class);

    private static final String WITH_ID = "rd_liquidity_profile_with_id";

    private final RedissonClient redissonClient;

    @Override
    public void save(LiquidityProfileData liquidityProfileData) {

        RMapCache<Long, LiquidityProfileData> withId = this.redissonClient.getMapCache(WITH_ID);

        withId.remove(liquidityProfileData.liquidityProfileId()
                                          .getEntityId());

        withId.put(liquidityProfileData.liquidityProfileId()
                                       .getEntityId(), liquidityProfileData);
    }

    @Override
    public LiquidityProfileData get(LiquidityProfileId liquidityProfileId) {

        RMapCache<Long, LiquidityProfileData> withId = this.redissonClient.getMapCache(WITH_ID);

        return withId.get(liquidityProfileId.getId());
    }

    @Override
    public void delete(LiquidityProfileId liquidityProfileId) {

        RMapCache<Long, LiquidityProfileData> withId = this.redissonClient.getMapCache(WITH_ID);

        withId.remove(liquidityProfileId.getId());
    }

}
