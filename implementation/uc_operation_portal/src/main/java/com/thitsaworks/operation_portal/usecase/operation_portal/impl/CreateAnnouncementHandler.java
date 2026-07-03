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
import com.thitsaworks.operation_portal.core.participant.command.CreateAnnouncementCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateAnnouncement;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.ANNOUNCEMENT_AND_GREETING_CONTENT)
public class CreateAnnouncementHandler
    extends OperationPortalUseCase<CreateAnnouncement.Input, CreateAnnouncement.Output>
    implements CreateAnnouncement {

    private static final Logger LOG = LoggerFactory.getLogger(CreateAnnouncementHandler.class);

    private final CreateAnnouncementCommand createAnnouncementCommand;

    public CreateAnnouncementHandler(PrincipalCache principalCache,
                                     CreateAnnouncementCommand createAnnouncementCommand,
                                     ActionAuthorizationManager actionAuthorizationManager) {

        super(principalCache, actionAuthorizationManager);

        this.createAnnouncementCommand = createAnnouncementCommand;

    }

    @Override
    public CreateAnnouncement.Output onExecute(CreateAnnouncement.Input input)
        throws DomainException {

        CreateAnnouncementCommand.Output output = this.createAnnouncementCommand.execute(
            new CreateAnnouncementCommand.Input(
                input.announcementTitle(),
                input.announcementDetail(), input.announcementDate()));

        return new CreateAnnouncement.Output(output.created());
    }

}
