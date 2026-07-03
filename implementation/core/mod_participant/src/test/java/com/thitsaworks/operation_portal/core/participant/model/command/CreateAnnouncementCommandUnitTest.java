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

import com.thitsaworks.operation_portal.component.test.EnvAwareUnitTest;
import com.thitsaworks.operation_portal.core.participant.ParticipantConfiguration;
import com.thitsaworks.operation_portal.core.participant.command.CreateAnnouncementCommand;
import com.thitsaworks.operation_portal.core.participant.model.TestSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Calendar;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ParticipantConfiguration.class, TestSettings.class})
public class CreateAnnouncementCommandUnitTest extends EnvAwareUnitTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateAnnouncementCommandUnitTest.class);

    @Autowired
    CreateAnnouncementCommand createAnnouncementCommand;

    @Test
    public void test_createAnnouncementSuccessfully() throws Exception {

        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.MONTH, Calendar.JULY);
        c1.set(Calendar.DATE, 10);
        c1.set(Calendar.YEAR, 2025);

        Instant announcementDate = Instant.ofEpochMilli(c1.getTimeInMillis());

        this.createAnnouncementCommand.execute(
                new CreateAnnouncementCommand.Input("Announcement for January",
                                                    "Announcement for January blah blah blah",
                                                    announcementDate));
    }

}
