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
package com.thitsaworks.operation_portal.core.participant.model.command;

import com.thitsaworks.operation_portal.component.common.type.ParticipantName;
import com.thitsaworks.operation_portal.component.common.type.ParticipantStatus;
import com.thitsaworks.operation_portal.component.infra.redis.RedisConfiguration;
import com.thitsaworks.operation_portal.component.common.type.Mobile;
import com.thitsaworks.operation_portal.core.participant.ParticipantConfiguration;
import com.thitsaworks.operation_portal.core.participant.command.CreateParticipantCommand;
import com.thitsaworks.operation_portal.core.participant.model.TestSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ParticipantConfiguration.class, TestSettings.class, RedisConfiguration.class})
public class CreateParticipantCommandUnitTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateParticipantCommandUnitTest.class);

    @Autowired
    private CreateParticipantCommand createParticipantCommand;

    @Test
    public void test_createParticipantSuccessfully() throws Exception {

        this.createParticipantCommand.execute(new CreateParticipantCommand.Input(1, new ParticipantName("abcdefg"),
                                                                                 "Vision Fund",
                                                                                 "address",
                                                                                 new Mobile("+959250661838"),
                                                                                 ParticipantStatus.ACTIVE,
                                                                                 null,
                                                                                 null));

    }

}
