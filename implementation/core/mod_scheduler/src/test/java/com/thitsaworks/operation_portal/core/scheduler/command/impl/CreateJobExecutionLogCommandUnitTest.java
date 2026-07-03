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
package com.thitsaworks.operation_portal.core.scheduler.command.impl;

import com.thitsaworks.operation_portal.component.common.type.JobStatus;
import com.thitsaworks.operation_portal.component.test.EnvAwareUnitTest;
import com.thitsaworks.operation_portal.core.scheduler.SchedulerConfiguration;
import com.thitsaworks.operation_portal.core.scheduler.TestSettings;
import com.thitsaworks.operation_portal.core.scheduler.command.CreateJobExecutionLogCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SchedulerConfiguration.class, TestSettings.class})
public class CreateJobExecutionLogCommandUnitTest extends EnvAwareUnitTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateJobExecutionLogCommandUnitTest.class);

    @Autowired
    private CreateJobExecutionLogCommand createJobExecutionLogCommand;

    @Test
    public void test_createJobExecutionLogSuccessfully() throws Exception {

        CreateJobExecutionLogCommand.Output output =
                createJobExecutionLogCommand.execute(new CreateJobExecutionLogCommand.Input("DailyTransaction",
                                                                                            JobStatus.STARTED,
                                                                                            LocalDateTime.now()));

        // Assert
        LOG.info("Created log : {}", output);
    }

}
