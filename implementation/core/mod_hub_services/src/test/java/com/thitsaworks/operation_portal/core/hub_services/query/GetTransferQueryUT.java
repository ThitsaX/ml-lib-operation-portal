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
package com.thitsaworks.operation_portal.core.hub_services.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.core.hub_services.BaseVaultSetUpTest;
import com.thitsaworks.operation_portal.core.hub_services.HubServicesConfiguration;
import com.thitsaworks.operation_portal.core.hub_services.TestSettings;
import com.thitsaworks.operation_portal.core.hub_services.data.TransferData;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HubServicesConfiguration.class, TestSettings.class})
public class GetTransferQueryUT extends BaseVaultSetUpTest {

    private static final Logger logger = LoggerFactory.getLogger(GetTransferQueryUT.class);

    @Autowired
    private GetTransfersQuery getTransfersQuery;

    @Test
    public void testGetTransfers() throws HubServicesException {

        var input = new GetTransfersQuery.Input(
                "2025-07-24T06:00:00Z", "2025-07-29T05:59:59Z",
            null,
            "wallet1",
            "wallet1",
            null,
            null,
            null,
            null,
            null,
            null,
            null, "0900", 1, 20
        );

        var output = getTransfersQuery.execute(input);

        ObjectMapper mapper = new ObjectMapper();

        if (output.getTransferInfoList().isEmpty()) {return;}

        for (TransferData transferData : output.getTransferInfoList()) {

            try {

                logger.info("Transfers retrieved: {}", mapper.writeValueAsString(transferData));

            } catch (Exception e) {

                logger.error("Error serializing transferData", e);
            }
        }
    }

}