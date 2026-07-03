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
import com.thitsaworks.operation_portal.core.participant.command.ModifyAnnouncementCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyAnnouncement;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@ActionMetadata(category = ActionCategory.ANNOUNCEMENT_AND_GREETING_CONTENT)
public class ModifyAnnouncementHandler
    extends OperationPortalUseCase<ModifyAnnouncement.Input, ModifyAnnouncement.Output>
    implements ModifyAnnouncement {

    private static final Logger LOG = LoggerFactory.getLogger(
        ModifyAnnouncementHandler.class);

    private final ModifyAnnouncementCommand modifyAnnouncementCommand;

    @Autowired
    public ModifyAnnouncementHandler(ModifyAnnouncementCommand modifyAnnouncementCommand,
                                     PrincipalCache principalCache,
                                     ActionAuthorizationManager actionAuthorizationManager) {

        super(principalCache, actionAuthorizationManager);

        this.modifyAnnouncementCommand = modifyAnnouncementCommand;
    }

    @Override
    public ModifyAnnouncement.Output onExecute(ModifyAnnouncement.Input input)
        throws DomainException {

        ModifyAnnouncementCommand.Output output = this.modifyAnnouncementCommand.execute(
            new ModifyAnnouncementCommand.Input(
                input.announcementId(), input.announcementTitle(), input.announcementDetail(),
                input.announcementDate(), input.isDeleted()));

        return new ModifyAnnouncement.Output(output.announcementId(), output.modified());
    }

}
