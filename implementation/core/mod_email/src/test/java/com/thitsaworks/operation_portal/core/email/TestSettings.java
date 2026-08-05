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
package com.thitsaworks.operation_portal.core.email;

import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.Email;
import com.thitsaworks.operation_portal.core.participant.data.UserData;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantException;
import com.thitsaworks.operation_portal.core.participant.query.UserQuery;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

public class TestSettings {

    @Bean
    public EmailConfiguration.EmailSettings emailSettings() {

        return new EmailConfiguration.EmailSettings(
            "test.com",
            587,
            "OP System",
            "test@thitsa.com",
            "**",
            true,
            true,
            false);
    }

    @Bean
    public UserQuery userQuery() {

        UserData receiver = this.userData(865225336894033920L, "test@thitsa.com");

        return new UserQuery() {

            public List<UserData> getUsers() {

                return List.of(receiver);
            }

            @Override
            public List<UserData> getUsers(ParticipantId participantId) {

                return List.of(receiver);
            }

            @Override
            public UserData get(UserId userId) throws ParticipantException {

                if (receiver.userId().equals(userId)) {
                    return receiver;
                }

                throw new IllegalArgumentException("User not found");
            }

            @Override
            public Optional<UserData> find(UserId userId) {

                return receiver.userId().equals(userId) ? Optional.of(receiver) : Optional.empty();
            }

            @Override
            public UserData get(Email email) {

                if (receiver.email().equals(email)) {
                    return receiver;
                }

                throw new IllegalArgumentException("User not found");
            }
        };
    }

    private UserData userData(long id, String email) {

        try {
            for (Constructor<?> constructor : UserData.class.getConstructors()) {
                if (constructor.getParameterCount() == 9) {
                    return (UserData) constructor.newInstance(
                        new UserId(id), null, null, null, new Email(email), null, null, null, null);
                }

                if (constructor.getParameterCount() == 11) {
                    return (UserData) constructor.newInstance(
                        new UserId(id), null, null, null, "User " + id, new Email(email), null, null, null, false,
                        null);
                }
            }

            throw new IllegalStateException("Unsupported UserData constructor.");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to build UserData test fixture.", e);
        }
    }

}
