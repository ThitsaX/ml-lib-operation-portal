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
package com.thitsaworks.operation_portal.component.misc.exception;

import java.text.MessageFormat;

public class ErrorMessage {

    private final String code;

    private final String defaultMessage;

    private final String description;

    public ErrorMessage(String code, String defaultMessage, String description) {

        this.code = code;
        this.defaultMessage = defaultMessage;
        this.description = description;
    }

    public ErrorMessage(String code, String defaultMessage) {

        this(code, defaultMessage, "");
    }

    public String getCode() {

        return this.code;
    }

    public String getDefaultMessage() {

        return this.defaultMessage;
    }

    public String getDescription() {

        return this.description;
    }

    public ErrorMessage code(String code) {

        return new ErrorMessage(code, defaultMessage, description);
    }

    public ErrorMessage defaultMessage(String message) {

        return new ErrorMessage(code, message, description);
    }

    public ErrorMessage description(String description) {

        return new ErrorMessage(code, defaultMessage, description);
    }

    public ErrorMessage format(Object... args) {

        return new ErrorMessage(this.code, MessageFormat.format(this.defaultMessage, args), this.description);
    }

}


