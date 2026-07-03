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
import com.thitsaworks.operation_portal.core.iam.command.CreateMenuCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateMenu;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.ConnectException;

@Service
@ActionMetadata(category = ActionCategory.ROLE_MENU_PERMISSION_IAM)
public class CreateMenuHandler extends OperationPortalUseCase<CreateMenu.Input, CreateMenu.Output>
    implements CreateMenu {

    private static final Logger LOG = LoggerFactory.getLogger(CreateMenuHandler.class);

    private final CreateMenuCommand createMenuCommand;

    public CreateMenuHandler(PrincipalCache principalCache,
                             ActionAuthorizationManager actionAuthorizationManager,
                             CreateMenuCommand createMenuCommand) {

        super(principalCache, actionAuthorizationManager);

        this.createMenuCommand = createMenuCommand;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException, ConnectException {

        var output = this.createMenuCommand.execute(
            new CreateMenuCommand.Input(
                input.menuId(), input.menuName(), input.parentId(),
                input.isActive()));

        return new Output(output.menuId());
    }

}
