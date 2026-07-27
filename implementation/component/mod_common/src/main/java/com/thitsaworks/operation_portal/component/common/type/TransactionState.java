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

public enum TransactionState {

    WAITING_FOR_ACTION("WAITING_FOR_ACTION"),
    WAITING_FOR_PARTY_ACCEPTANCE("WAITING_FOR_PARTY_ACCEPTANCE"),
    QUOTE_REQUEST_RECEIVED("QUOTE_REQUEST_RECEIVED"),
    WAITING_FOR_QUOTE_ACCEPTANCE("WAITING_FOR_QUOTE_ACCEPTANCE"),
    PREPARE_RECEIVED("PREPARE_RECEIVED"),
    ERROR_OCCURRED("ERROR_OCCURRED"),
    COMPLETED("COMPLETED"),
    ABORTED("ABORTED"),
    RESERVED("RESERVED");

    private final String value;

    TransactionState(String value) {

        this.value = value;
    }

    @Override
    public String toString() {

        return this.value;
    }
}
