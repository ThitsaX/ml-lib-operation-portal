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

import com.thitsaworks.operation_portal.component.common.identifier.AnnouncementId;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.test.EnvAwareUnitTest;
import com.thitsaworks.operation_portal.core.participant.command.ModifyAnnouncementCommand;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Calendar;

public class ModifyAnnouncementCommandUnitTest extends EnvAwareUnitTest {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyAnnouncementCommandUnitTest.class);

    @Autowired
    private ModifyAnnouncementCommand modifyAnnouncementCommand;

    @Test
    public void test_modifyAnnouncementSuccessfully() throws DomainException {

        Calendar c1 = Calendar.getInstance();
        c1.set(Calendar.MONTH, Calendar.MARCH);
        c1.set(Calendar.DATE, 1);
        c1.set(Calendar.YEAR, 2023);

        Instant announcementDate = Instant.ofEpochMilli(c1.getTimeInMillis());
        this.modifyAnnouncementCommand.execute(
                new ModifyAnnouncementCommand.Input(new AnnouncementId(410480760326225920L), "March Announcement",
                                                    "March Announcement blah blah blah", null, false));
    }

}
