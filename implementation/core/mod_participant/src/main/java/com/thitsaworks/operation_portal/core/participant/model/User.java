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
package com.thitsaworks.operation_portal.core.participant.model;

import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.Email;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import com.thitsaworks.operation_portal.core.participant.cache.UserCache;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

@Entity
@EntityListeners(value = {UserCache.Updater.class})
@Table(name = "tbl_user")
@Getter
@NoArgsConstructor
public class User extends JpaEntity<UserId> {

    @EmbeddedId
    protected UserId userId;

    @ManyToOne()
    @JoinColumn(name = "participant_id")
    protected Participant participant;

    @Column(name = "name")
    protected String name;

    @Column(name = "email")
    @Convert(converter = Email.JpaConverter.class)
    protected Email email;

    @Column(name = "first_name")
    protected String firstName;

    @Column(name = "last_name")
    protected String lastName;

    @Column(name = "job_title")
    protected String jobTitle;

    @Column(name = "allow_notification")
    protected boolean allowNotification;

    @Column(name = "is_deleted")
    protected boolean isDeleted;

    @Column(name = "is_visible", nullable = false)
    protected boolean isVisible;

    public User(String name, Email email, Participant participant, String firstName, String lastName,
                String jobTitle) {

        this(name, email, participant, firstName, lastName, jobTitle, false, true);
    }

    public User(String name, Email email, Participant participant, String firstName, String lastName,
                String jobTitle, boolean allowNotification) {

        this(name, email, participant, firstName, lastName, jobTitle, allowNotification, true);
    }

    public User(String name, Email email, Participant participant, String firstName, String lastName,
                String jobTitle, boolean allowNotification, boolean isVisible) {

        Validate.notNull(participant);

        this.userId = new UserId(Snowflake.get().nextId());
        this.name = name;
        this.email = email;
        this.participant = participant;
        this.firstName = firstName;
        this.lastName = lastName;
        this.jobTitle = jobTitle;
        this.allowNotification = allowNotification;
        this.isVisible = isVisible;
    }

    @Override
    public UserId getId() {

        return this.userId;
    }

    public User name(String name) {

        this.name = name;
        return this;

    }

    public User email(Email email) {

        this.email = email;
        return this;

    }

    public User firstName(String firstName) {

        this.firstName = firstName;
        return this;

    }

    public User lastName(String lastName) {

        this.lastName = lastName;
        return this;

    }

    public User participant(Participant participant){

        this.participant = participant;
        return this;
    }

    public User jobTitle(String jobTitle) {

        this.jobTitle = jobTitle;
        return this;

    }

    public User allowNotification(boolean allowNotification) {

        this.allowNotification = allowNotification;
        return this;

    }

    public User isDeleted(boolean isDeleted) {

        this.isDeleted = isDeleted;
        return this;

    }

    public User isVisible(boolean isVisible) {

        this.isVisible = isVisible;
        return this;

    }

}


