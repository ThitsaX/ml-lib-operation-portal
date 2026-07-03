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
package com.thitsaworks.operation_portal.core.participant.cache;

import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.misc.spring.CacheQualifiers;
import com.thitsaworks.operation_portal.component.misc.spring.SpringContext;
import com.thitsaworks.operation_portal.core.participant.data.UserData;
import com.thitsaworks.operation_portal.core.participant.model.User;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

public interface UserCache {

    void save(UserData userData);

    UserData get(UserId userId);

    void delete(UserId userId);

    public static class Updater {

        @PostPersist
        @PostUpdate
        public void persistOrUpdate(User user) {

            UserCache participantCache = SpringContext.getBean(UserCache.class,
                                                               CacheQualifiers.DEFAULT);

            UserData userData = new UserData(user);

            participantCache.save(userData);
        }

        @PostRemove
        public void postRemove(User user) {

            UserCache userCache = SpringContext.getBean(UserCache.class,
                                                        CacheQualifiers.DEFAULT);
            userCache.delete(user.getUserId());

        }

    }

}
