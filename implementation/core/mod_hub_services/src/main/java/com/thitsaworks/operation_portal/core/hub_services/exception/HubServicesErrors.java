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
package com.thitsaworks.operation_portal.core.hub_services.exception;

import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;

public class HubServicesErrors {

    //@@formatter:off
    public static final ErrorMessage CONNECTION_ERROR = new ErrorMessage("CONNECTION_ERROR", "Failed to connect to the hub service!");
    public static final ErrorMessage HUB_TRANSFER_ERROR = new ErrorMessage("HUB_TRANSFER_ERROR", "Failed to process the operation for Transaction on Hub.");
    public static final ErrorMessage HUB_CURRENCY_ERROR = new ErrorMessage("HUB_CURRENCY_ERROR", "Failed to process the operation for Currency on Hub.");
    public static final ErrorMessage PARTY_IDENTIFIER_TYPE_ID  = new ErrorMessage("PARTY_IDENTIFIER_TYPE_ID", "Failed to process the operation for Party Identifier Type on Hub.");
    public static final ErrorMessage SETTLEMENT_WINDOW_ERROR = new ErrorMessage("SETTLEMENT_WINDOW_ERROR", "Failed to process the operation for Settlement Window on Hub.");
    public static final ErrorMessage SETTLEMENT_ERROR = new ErrorMessage("SETTLEMENT_ERROR", "Failed to process the operation for Settlement on Hub.");
    public static final ErrorMessage HUB_PARTICIPANT_ERROR = new ErrorMessage("HUB_PARTICIPANT_ERROR", "Failed to process the operation for Participant on Hub.");
    public static final ErrorMessage HUB_PARTICIPANT_BALANCE_ERROR = new ErrorMessage("HUB_PARTICIPANT_BALANCE_ERROR", "Failed to get the participant's balance on Hub.");
    public static final ErrorMessage HUB_PARTICIPANT_POSITION_ERROR = new ErrorMessage("HUB_PARTICIPANT_POSITION_ERROR", "Failed to get the participant's position data on Hub.");
    public static final ErrorMessage SETTLEMENT_WINDOW_STATE_ERROR = new ErrorMessage("SETTLEMENT_WINDOW_STATE_ERROR", "Failed to get settlement window states.");
    public static final ErrorMessage SETTLEMENT_STATE_ERROR = new ErrorMessage("SETTLEMENT_STATE_ERROR", "Failed to get settlement states.");
    public static final ErrorMessage HUB_PARTICIPANT_DESCRIPTION_ERROR = new ErrorMessage("HUB_PARTICIPANT_DESCRIPTION_ERROR", "Failed to update the description for Participant on Hub.");

    //@@formatter:on
}
