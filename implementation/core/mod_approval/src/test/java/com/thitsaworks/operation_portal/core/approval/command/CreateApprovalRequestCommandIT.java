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
package com.thitsaworks.operation_portal.core.approval.command;

import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.core.approval.ApprovalConfiguration;
import com.thitsaworks.operation_portal.core.approval.BaseVaultSetUpTest;
import com.thitsaworks.operation_portal.core.approval.TestSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
        ApprovalConfiguration.class, TestSettings.class})
public class CreateApprovalRequestCommandIT extends BaseVaultSetUpTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateApprovalRequestCommandIT.class);

    @Autowired
    private CreateApprovalRequestCommand createApprovalRequestCommand;

    @Test
    public void success() {

        LOG.info("Approval Request : [{}]",
                 this.createApprovalRequestCommand.execute(new CreateApprovalRequestCommand.Input("Deposit",
                                                                                                  "wallet2",
                                                                                                  "USD",
                                                                                                  "8",
                                                                                                  "9",
                                                                                                  BigDecimal.valueOf(
                                                                                                      50000.00),
                                                                                                  new UserId(
                                                                                                      728941546990530560L))));
    }

}
