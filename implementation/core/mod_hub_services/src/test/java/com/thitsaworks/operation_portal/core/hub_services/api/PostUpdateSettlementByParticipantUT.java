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

import com.thitsaworks.operation_portal.component.fspiop.model.Currency;
import com.thitsaworks.operation_portal.component.fspiop.model.ExtensionList;
import com.thitsaworks.operation_portal.component.fspiop.model.Money;
import com.thitsaworks.operation_portal.core.hub_services.BaseVaultSetUpTest;
import com.thitsaworks.operation_portal.core.hub_services.HubServicesConfiguration;
import com.thitsaworks.operation_portal.core.hub_services.ParticipantHubClient;
import com.thitsaworks.operation_portal.core.hub_services.TestSettings;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesException;
import com.thitsaworks.operation_portal.core.hub_services.support.SettlementAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HubServicesConfiguration.class, TestSettings.class})
public class PostUpdateSettlementByParticipantUT extends BaseVaultSetUpTest {

    private static final Logger logger = LoggerFactory.getLogger(PostUpdateSettlementByParticipantUT.class);

    @Autowired
    private ParticipantHubClient participantHubClient;

    @Test
    public void test() throws HubServicesException {

        PostParticipantBalance.Request request =
                new PostParticipantBalance.Request(UUID.randomUUID().toString(),
                                                   SettlementAction.recordFundsOutPrepareReserve.toString(),
                                                   "Business Operations Portal settlement ID 34 finalization report processing",
                                                   "",
                                                   new Money().currency(Currency.USD).amount("1000"),
                                                   new ExtensionList());

        var output = this.participantHubClient.postParticipantBalance("wallet1", "29", request);
        logger.info("Output: {}", output);
    }

}

