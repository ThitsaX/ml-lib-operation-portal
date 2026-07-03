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
package com.thitsaworks.operation_portal.core.participant.command.impl;

import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.participant.command.ModifyAnnouncementCommand;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.model.Announcement;
import com.thitsaworks.operation_portal.core.participant.model.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModifyAnnouncementCommandHandler implements ModifyAnnouncementCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ModifyAnnouncementCommandHandler.class);

    private AnnouncementRepository announcementRepository;

    @Override
    @CoreWriteTransactional
    public ModifyAnnouncementCommand.Output execute(ModifyAnnouncementCommand.Input input)
            throws ParticipantException {

        Announcement announcement =
                this.announcementRepository.findById(input.announcementId()).orElseThrow(() -> new ParticipantException(
                        ParticipantErrors.ANNOUNCEMENT_NOT_FOUND.format(input.announcementId().getId().toString())));

        Optional<Announcement> optionalAnnouncementTitle = this.announcementRepository.findOne(
                AnnouncementRepository.Filters.findByAnnouncementTitle(input.announcementTitle()));

        if (optionalAnnouncementTitle.isPresent() &&
                !optionalAnnouncementTitle.get().getAnnouncementId().equals(announcement.getAnnouncementId())) {
            throw new ParticipantException(ParticipantErrors.ANNOUNCEMENT_ALREADY_REGISTERED.format(input.announcementTitle()));
        }

        this.announcementRepository.save(announcement.announcementTitle(input.announcementTitle())
                                                     .announcementDetail(input.announcementDetail())
                                                     .announcementDate(input.announcementDate())
                                                     .isDeleted(input.isDeleted()));

        return new ModifyAnnouncementCommand.Output(input.announcementId(), true);
    }

}
