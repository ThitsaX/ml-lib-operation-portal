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
package com.thitsaworks.operation_portal.core.iam.model.command;

import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.test.EnvAwareUnitTest;
import com.thitsaworks.operation_portal.core.iam.command.ChangePasswordCommand;
import com.thitsaworks.operation_portal.core.iam.IAMConfiguration;
import com.thitsaworks.operation_portal.component.common.identifier.PrincipalId;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {IAMConfiguration.class})
public class ChangePasswordCommandUnitTest extends EnvAwareUnitTest {

    private static final Logger LOG = LoggerFactory.getLogger(ChangePasswordCommandUnitTest.class);

    @Autowired
    private ChangePasswordCommand changePasswordCommand;

    @Test
    public void test_ChangePasswordSuccessfully() throws DomainException {

        this.changePasswordCommand.execute(
                new ChangePasswordCommand.Input(new PrincipalId(Long.parseLong("392628367895068672")), "testpassword",
                                                "Nne@12345"));
    }

    @Test //expected = PasswordAuthenticationFailureException.class
    public void test_PasswordAuthenticationFailureException()
            throws DomainException {

        this.changePasswordCommand.execute(
                new ChangePasswordCommand.Input(new PrincipalId(Long.parseLong("343028466997653504")), "testpassword",
                                                "newpassword"));
    }

}
