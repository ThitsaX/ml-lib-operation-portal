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
import com.thitsaworks.operation_portal.core.participant.command.UpdateGreetingCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.UpdateGreeting;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.ANNOUNCEMENT_AND_GREETING_CONTENT)
public class UpdateGreetingHandler
    extends OperationPortalUseCase<UpdateGreeting.Input, UpdateGreeting.Output>
    implements UpdateGreeting {

    private final UpdateGreetingCommand updateGreetingCommand;

    public UpdateGreetingHandler(PrincipalCache principalCache,
                                 ActionAuthorizationManager actionAuthorizationManager,
                                 UpdateGreetingCommand updateGreetingCommand) {

        super(principalCache, actionAuthorizationManager);

        this.updateGreetingCommand = updateGreetingCommand;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        var output = this.updateGreetingCommand.execute(new UpdateGreetingCommand.Input(
            input.greetingId(), input.greetingTitle(), input.greetingDetail(), input.isDeleted(),
            input.greetingDate()));

        return new Output(output.greetingId());
    }

}
