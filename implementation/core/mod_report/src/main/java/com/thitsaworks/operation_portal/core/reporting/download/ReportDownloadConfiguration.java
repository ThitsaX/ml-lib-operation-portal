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
package com.thitsaworks.operation_portal.core.reporting.download;

import com.thitsaworks.operation_portal.component.infra.mysql.core.CorePersistenceConfiguration;
import com.thitsaworks.operation_portal.component.misc.MiscConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@ComponentScan("com.thitsaworks.operation_portal.core.reporting.download")
@Import(value = {
        MiscConfiguration.class,
        CorePersistenceConfiguration.class
})
public class ReportDownloadConfiguration {

    @Bean(name = "reportGenerationTaskExecutor")
    public ThreadPoolTaskExecutor reportGenerationTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("report-gen-");
        executor.initialize();
        return executor;
    }
}
