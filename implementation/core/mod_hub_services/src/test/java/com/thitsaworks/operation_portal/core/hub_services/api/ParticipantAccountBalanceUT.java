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
package com.thitsaworks.operation_portal.core.hub_services.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.core.hub_services.HubServicesConfiguration;
import com.thitsaworks.operation_portal.core.hub_services.ParticipantHubClient;
import com.thitsaworks.operation_portal.core.hub_services.TestSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HubServicesConfiguration.class, TestSettings.class})
public class ParticipantAccountBalanceUT {

    private static final Logger LOG = LoggerFactory.getLogger(ParticipantAccountBalanceUT.class);

    @Test
    public void getParticipantAccountBalance() throws Exception {

        var participantHubClient = new ParticipantHubClient(new HubServicesConfiguration.Settings(
                "http://example.com:1234",
                "http://example.com:1234/v2/"));

        GetParticipantAccountBalance.Request request = new GetParticipantAccountBalance.Request("wallet1");

        var output = participantHubClient.getParticipantAccountBalance(request);

        if (output != null) {

            LOG.info("Output : <{}>", new ObjectMapper().writeValueAsString(output));

        } else {

            LOG.info("Output : {} ", "");
        }

    }

}
