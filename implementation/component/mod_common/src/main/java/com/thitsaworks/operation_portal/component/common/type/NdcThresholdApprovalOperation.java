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

public enum NdcThresholdApprovalOperation {

    CREATE_NDC_ALERT_THRESHOLD,
    UPDATE_NDC_VISUAL_ALERT,
    UPDATE_NDC_NOTIFICATION_ALERT,
    UPDATE_NDC_VISUAL_AND_NOTIFICATION_ALERT,
    DELETE_NDC_ALERT_THRESHOLD;

    public String requestedAction() {

        return this.name();
    }

    public static NdcThresholdApprovalOperation fromRequestedAction(String requestedAction) {

        // Keep already-persisted create requests readable after the action name was clarified.
        if ("CREATE_NDC_THRESHOLD".equals(requestedAction)) {
            return CREATE_NDC_ALERT_THRESHOLD;
        }

        for (var operation : values()) {
            if (operation.requestedAction().equals(requestedAction)) {
                return operation;
            }
        }

        throw new IllegalArgumentException("Unsupported NDC threshold approval action.");
    }
}
