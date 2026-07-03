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
package com.thitsaworks.operation_portal.core.participant.query.impl.jpa;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.thitsaworks.operation_portal.component.common.identifier.AnnouncementId;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.core.participant.data.AnnouncementData;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.model.Announcement;
import com.thitsaworks.operation_portal.core.participant.model.QAnnouncement;
import com.thitsaworks.operation_portal.core.participant.model.repository.AnnouncementRepository;
import com.thitsaworks.operation_portal.core.participant.query.AnnouncementQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@CoreReadTransactional
public class AnnouncementJpaQueryHandler implements AnnouncementQuery {

    private final AnnouncementRepository announcementRepository;

    private final QAnnouncement announcement = QAnnouncement.announcement;

    @Override
    public List<AnnouncementData> getAnnouncements() {

        BooleanExpression predicate = this.announcement.isNotNull().and(announcement.isDeleted.isFalse());

        List<Announcement> announcements = (List<Announcement>) this.announcementRepository.findAll(predicate);

        return announcements.stream().map(AnnouncementData::new).toList();
    }

    @Override
    public AnnouncementData get(AnnouncementId announcementId) throws ParticipantException {

        BooleanExpression predicate = this.announcement.announcementId.eq(announcementId);

        Optional<Announcement> optionalAnnouncement = this.announcementRepository.findOne(predicate);

        if (optionalAnnouncement.isEmpty()) {

            throw new ParticipantException(ParticipantErrors.ANNOUNCEMENT_NOT_FOUND.format(announcementId.getId().toString()));
        }

        return new AnnouncementData(optionalAnnouncement.get());
    }

}
