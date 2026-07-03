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
package com.thitsaworks.operation_portal.usecase.operation_portal.impl;

import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.command.GrantMenuActionCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GrantMenuActionList;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.ConnectException;

@Service
@ActionMetadata(
    category = ActionCategory.ROLE_MENU_PERMISSION_IAM,
    isMandatory = true)
public class GrantMenuActionListHandler
    extends OperationPortalUseCase<GrantMenuActionList.Input, GrantMenuActionList.Output>
    implements GrantMenuActionList {

    private static final Logger LOG = LoggerFactory.getLogger(GrantMenuActionListHandler.class);

    private final GrantMenuActionCommand grantMenuActionCommand;

    public GrantMenuActionListHandler(PrincipalCache principalCache,
                                      ActionAuthorizationManager actionAuthorizationManager,
                                      GrantMenuActionCommand grantMenuActionCommand) {

        super(principalCache, actionAuthorizationManager);

        this.grantMenuActionCommand = grantMenuActionCommand;
    }

    @Override
    public Output onExecute(Input input) throws DomainException, ConnectException {

        for (var menu : input.menuGrantList()) {

            for (var action : menu.actionList()) {
                this.grantMenuActionCommand.execute(
                    new GrantMenuActionCommand.Input(menu.menuName(), action));
            }
        }

        return new Output(true);
    }

}
