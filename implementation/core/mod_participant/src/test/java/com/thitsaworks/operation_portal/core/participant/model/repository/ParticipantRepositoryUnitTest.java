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
package com.thitsaworks.operation_portal.core.participant.model.repository;

import com.thitsaworks.operation_portal.component.common.type.Mobile;
import com.thitsaworks.operation_portal.component.common.type.ParticipantName;
import com.thitsaworks.operation_portal.component.common.type.ParticipantStatus;
import com.thitsaworks.operation_portal.component.test.EnvAwareUnitTest;
import com.thitsaworks.operation_portal.core.participant.model.Participant;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ParticipantRepositoryUnitTest extends EnvAwareUnitTest {

    private static final Logger LOG = LoggerFactory.getLogger(ParticipantRepositoryUnitTest.class);

    @Autowired
    private ParticipantRepository participantRepository;

    @Test
    public void test_createParticipantSuccessfully() {

        Participant participant = new Participant(1,
                                                  new ParticipantName("okdollar"),
                                                  "OK Dollar",
                                                  "Sanchaung Township, Yangon.",
                                                  new Mobile("+959400547258"),
                                                  ParticipantStatus.ACTIVE);

        this.participantRepository.save(participant);

    }

}
