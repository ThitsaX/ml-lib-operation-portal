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
package com.thitsaworks.operation_portal.core.scheduler.query.impl.jpa;

import com.thitsaworks.operation_portal.component.common.identifier.SchedulerConfigId;
import com.thitsaworks.operation_portal.component.infra.redis.RedisConfiguration;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.core.scheduler.BaseVaultSetUpTest;
import com.thitsaworks.operation_portal.core.scheduler.SchedulerConfiguration;
import com.thitsaworks.operation_portal.core.scheduler.TestSettings;
import com.thitsaworks.operation_portal.core.scheduler.model.repository.SchedulerConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SchedulerConfiguration.class, TestSettings.class, RedisConfiguration.class})
@Transactional
public class SchedulerConfigJpaQueryUnitTest extends BaseVaultSetUpTest {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerConfigJpaQueryUnitTest.class);

    @Autowired
    private SchedulerConfigRepository schedulerConfigRepository;
    
    @Autowired
    private SchedulerConfigJpaQueryHandler queryHandler;
    
    @Test
    void getSchedulerConfigs_ShouldReturnAllConfigs() {

        var result = queryHandler.getSchedulerConfigs(Sort.unsorted());

        LOG.info("Configs: {}", result);
        
    }
    
    @Test
    void get_ShouldReturnConfigWhenExists() throws DomainException {

        var result = queryHandler.get(new SchedulerConfigId(1L));

        LOG.info("Config: {}", result);
        
    }
    
    @Test
    void get_ShouldThrowWhenConfigNotFound() {
        // Act & Assert
        assertThrows(DomainException.class, () -> {
            queryHandler.get(new SchedulerConfigId(999L));
        });
    }
}
