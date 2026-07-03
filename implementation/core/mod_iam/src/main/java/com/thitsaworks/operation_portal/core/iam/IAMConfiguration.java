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
package com.thitsaworks.operation_portal.core.iam;

import com.thitsaworks.operation_portal.component.infra.redis.RedisConfiguration;
import com.thitsaworks.operation_portal.component.infra.mysql.core.CorePersistenceConfiguration;
import com.thitsaworks.operation_portal.component.misc.MiscConfiguration;
import com.thitsaworks.operation_portal.core.iam.engine.IAMEngine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan("com.thitsaworks.operation_portal.core.iam")
@Import(value = {
        MiscConfiguration.class, RedisConfiguration.class, CorePersistenceConfiguration.class
})
@RequiredArgsConstructor
public class IAMConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(IAMConfiguration.class);

    private final IAMEngine iamEngine;

    @PostConstruct
    public void bootstrapIAMEngine() {

        try {

            LOG.info("Starting IAMEngine bootstrap...");
            this.iamEngine.bootstrap();
            LOG.info("IAMEngine bootstrap completed successfully");

        } catch (Exception e) {

            LOG.error("Failed to bootstrap IAMEngine", e);
            throw new IllegalStateException("Failed to bootstrap IAMEngine", e);
        }
    }

}
