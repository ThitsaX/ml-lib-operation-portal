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

import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.ApprovalActionType;
import com.thitsaworks.operation_portal.core.approval.ApprovalConfiguration;
import com.thitsaworks.operation_portal.core.approval.BaseVaultSetUpTest;
import com.thitsaworks.operation_portal.core.approval.TestSettings;
import com.thitsaworks.operation_portal.core.approval.exception.ApprovalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {
        ApprovalConfiguration.class, TestSettings.class})
public class ModifyApprovalActionCommandIT extends BaseVaultSetUpTest {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyApprovalActionCommandIT.class);

    @Autowired
    private ModifyApprovalActionCommand modifyApprovalActionCommand;

    @Test
    public void success() throws ApprovalException {

        LOG.info("Approval Request : [{}]",
                 this.modifyApprovalActionCommand.execute(new ModifyApprovalActionCommand.Input(new ApprovalRequestId(
                     730361712823226368L), ApprovalActionType.APPROVED, new UserId(728941546990530560L))));
    }

}
