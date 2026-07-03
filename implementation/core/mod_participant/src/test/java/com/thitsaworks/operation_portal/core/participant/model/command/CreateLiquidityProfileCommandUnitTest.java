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
import com.thitsaworks.operation_portal.core.participant.command.CreateLiquidityProfileCommand;
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateLiquidityProfileCommandUnitTest extends EnvAwareUnitTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateLiquidityProfileCommandUnitTest.class);

    @Autowired
    private CreateLiquidityProfileCommand createLiquidityProfileCommand;

    @Test
    public void test_createLiquidityProfileSuccessfully() throws Exception {

        this.createLiquidityProfileCommand.execute(
                new CreateLiquidityProfileCommand.Input(new ParticipantId(390906682871414784L),
                                                        "",
                                                        "NYEIN NYEIN EI",
                                                        "2341895948958343",
                                                        "MMK", true
                ));
    }

}
