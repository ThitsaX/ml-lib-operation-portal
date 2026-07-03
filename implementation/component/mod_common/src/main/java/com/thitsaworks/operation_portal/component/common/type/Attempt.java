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
package com.thitsaworks.operation_portal.component.common.type;

import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaInstantConverter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Attempt {

    @Column(name = "max_attempt")
    private int maxAttempt = 3;

    @Column(name = "current_attempt")
    private int currentAttempt = 0;

    @Column(name = "denied_until")
    @Convert(converter = JpaInstantConverter.class)
    private Instant deniedUntil;

    @Column(name = "last_attempt_at")
    @Convert(converter = JpaInstantConverter.class)
    private Instant lastAttemptAt;

    @Column(name = "attempt_cooldown")
    private int attemptCooldown = 3600;

    @Column(name = "denied_duration")
    private int deniedDuration = 3600 * 24;

    public Attempt(Options options) {

        assert options.maxAttempt > 0 : "The value must be greater than zero.";

        this.maxAttempt = options.maxAttempt;
        this.lastAttemptAt = Instant.now();
        this.attemptCooldown = options.attemptCooldown;
        this.deniedDuration = options.deniedDuration;

    }

    public void attempt() throws AttemptDeniedException {

        if (this.isDenied()) {

            throw new AttemptDeniedException();

        }

        if (this.isCooldownOver()) {

            this.currentAttempt = 0;

        }

        this.lastAttemptAt = Instant.now();

        this.currentAttempt++;

        if (this.currentAttempt > this.maxAttempt) {

            this.deniedUntil = Instant.now().plus(this.deniedDuration, ChronoUnit.SECONDS);
            this.currentAttempt = 0;

            throw new AttemptDeniedException();

        }

    }

    public void reset() {

        this.currentAttempt = 0;

    }

    public boolean isDenied() {

        return this.deniedUntil != null && this.deniedUntil.isAfter(Instant.now());

    }

    private boolean isCooldownOver() {

        return this.lastAttemptAt != null
                && this.lastAttemptAt.plusSeconds(this.attemptCooldown).isBefore(Instant.now());

    }

    public static class AttemptDeniedException extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

    }

    @Value
    public static class Options {

        private int maxAttempt;

        private int attemptCooldown;

        private int deniedDuration;

    }

}
