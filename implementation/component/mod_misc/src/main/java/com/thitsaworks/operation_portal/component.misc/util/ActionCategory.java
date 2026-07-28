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
package com.thitsaworks.operation_portal.component.misc.util;

public enum ActionCategory {

    AUTHENTICATION_AND_ACCOUNT_SECURITY("Authentication & Account Security"),

    USER_MANAGEMENT("User Management"),

    ROLE_MENU_PERMISSION_IAM("Role / Menu / Permission / IAM"),

    PARTICIPANT_MANAGEMENT("Participant Management"),

    PARTICIPANT_PROFILE_AND_FINANCIAL_CONFIGURATION(
        "Participant Profile / NDC / Currency / Position"),

    CONTACT_MANAGEMENT("Contact Management"),

    LIQUIDITY_PROFILE("Liquidity Profile"),

    ANNOUNCEMENT_AND_GREETING_CONTENT("Announcement & Greeting Content"),

    APPROVAL_WORKFLOW("Approval Workflow"),

    SETTLEMENT_CORE_OPERATIONS("Settlement Core Operations"),

    SETTLEMENT_MODEL_MANAGEMENT("Settlement Model Management"),

    TRANSFER_OPERATIONS("Transfer Operations"),

    REPORTING("Reporting"),

    AUDIT_AND_LOGS("Audit & Logs"),

    REVENUE_CONFIG("Revenue Config"),

    SCHEDULER_AND_JOB_CONFIGURATION("Scheduler / Job Configuration"),

    REVENUE_PARTY("Revenue Party"),

    REVENUE_TRANSACTION("Revenue Transaction"),

    SYSTEM_JOBS_AND_SCHEDULED_EXECUTORS("Scheduled Job Executors / System Jobs");

    private final String displayName;

    ActionCategory(String displayName) {

        this.displayName = displayName;
    }

    public String getDisplayName() {

        return displayName;
    }
}
