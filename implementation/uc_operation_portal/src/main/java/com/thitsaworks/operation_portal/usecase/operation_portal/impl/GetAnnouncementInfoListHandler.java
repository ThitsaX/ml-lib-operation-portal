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
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.participant.data.AnnouncementData;
import com.thitsaworks.operation_portal.core.participant.query.AnnouncementQuery;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetAnnouncementInfoList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.ANNOUNCEMENT_AND_GREETING_CONTENT)
public class GetAnnouncementInfoListHandler implements GetAnnouncementInfoList {

    private static final Logger LOG = LoggerFactory.getLogger(GetAnnouncementInfoListHandler.class);

    private final AnnouncementQuery announcementQuery;

    @Autowired
    public GetAnnouncementInfoListHandler(AnnouncementQuery announcementQuery) {

        this.announcementQuery = announcementQuery;
    }

    @Override
    public Output execute(Input input) {

        List<AnnouncementData> announcementDataList = this.announcementQuery.getAnnouncements();

        List<GetAnnouncementInfoList.Output.AnnouncementInfo> announcementInfoList = new ArrayList<>();

        for (AnnouncementData announcementData : announcementDataList) {

            announcementInfoList.add(new Output.AnnouncementInfo(
                announcementData.announcementId(), announcementData.announcementTitle(),
                announcementData.announcementDetail(), announcementData.announcementDate()));
        }

        return new Output(announcementInfoList);
    }

}
