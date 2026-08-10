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
package com.thitsaworks.operation_portal.core.participant.exception;

import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;

public class ParticipantErrors {

    //@@formatter:off
    public static final ErrorMessage CONTACT_NOT_FOUND = new ErrorMessage("CONTACT_NOT_FOUND", "System cannot find the contact with provided ID : [{0}].");
    public static final ErrorMessage EMAIL_NOT_FOUND = new ErrorMessage("EMAIL_NOT_FOUND", "System cannot find the email with provided ID : [{0}].");
    public static final ErrorMessage LIQUIDITY_PROFILE_NOT_FOUND = new ErrorMessage("LIQUIDITY_PROFILE_NOT_FOUND", "System cannot find the liquidity profile with provided ID : [{0}].");
    public static final ErrorMessage PARTICIPANT_NOT_FOUND = new ErrorMessage("PARTICIPANT_NOT_FOUND", "System cannot find the participant with provided ID : [{0}].");
    public static final ErrorMessage PARTICIPANT_ACCOUNT_NOT_FOUND = new ErrorMessage("PARTICIPANT_ACCOUNT_NOT_FOUND", "Settlement and position accounts were not found for participant [{0}] and currency [{1}].");
    public static final ErrorMessage PARTICIPANT_ACCOUNT_INACTIVE = new ErrorMessage("PARTICIPANT_ACCOUNT_INACTIVE", "Participant account for [{0}] and currency [{1}] is inactive.");
    public static final ErrorMessage INVALID_PARTICIPANT_ACCOUNT_DATA = new ErrorMessage("INVALID_PARTICIPANT_ACCOUNT_DATA", "Participant account information is incomplete for participant [{0}] and currency [{1}].");
    public static final ErrorMessage USER_NOT_FOUND = new ErrorMessage("USER_NOT_FOUND", "System cannot find the user with provided ID : [{0}].");
    public static final ErrorMessage PARTICIPANT_NDC_NOT_FOUND = new ErrorMessage("PARTICIPANT_NDC_NOT_FOUND", "System cannot find the participant ndc with provided ID : [{0}].");
    public static final ErrorMessage ANNOUNCEMENT_NOT_FOUND = new ErrorMessage("ANNOUNCEMENT_NOT_FOUND", "System cannot find the announcement with provided ID : [{0}].");
    public static final ErrorMessage GREETING_MESSAGE_NOT_FOUND = new ErrorMessage("GREETING_MESSAGE_NOT_FOUND", "System cannot find the Greeting Message with provided ID : [{0}].");

    public static final ErrorMessage PASSWORD_SAME_AS_CURRENT = new ErrorMessage("PASSWORD_SAME_AS_CURRENT", "New password must be different from current password.");
    public static final ErrorMessage EMAIL_ALREADY_REGISTERED = new ErrorMessage("EMAIL_ALREADY_REGISTERED", "The provided Email : [{0}] has already registered in the system.");
    public static final ErrorMessage PARTICIPANT_ALREADY_REGISTERED = new ErrorMessage("PARTICIPANT_ALREADY_REGISTERED", "The Participant has already registered in the system with provided DFSP Code : [{0}].");
    public static final ErrorMessage CONTACT_TYPE_ALREADY_REGISTERED = new ErrorMessage("CONTACT_TYPE_ALREADY_REGISTERED", "The provided Contact Type : [{0}] has already registered in the system.");
    public static final ErrorMessage LIQUIDITY_PROFILE_ALREADY_REGISTERED = new ErrorMessage("LIQUIDITY_PROFILE_ALREADY_REGISTERED", "The provided Currency : [{0}] for the Liquidity profile has already registered in the system.");
    public static final ErrorMessage ANNOUNCEMENT_ALREADY_REGISTERED = new ErrorMessage("ANNOUNCEMENT_ALREADY_REGISTERED", "The provided Announcement : [{0}] has already registered in the system.");
    public static final ErrorMessage GREETING_MESSAGE_ALREADY_REGISTERED = new ErrorMessage("GREETING_MESSAGE_ALREADY_REGISTERED", "The provided Greeting Message : [{0}] has already registered in the system.");


    public static final ErrorMessage INSUFFICIENT_BALANCE = new ErrorMessage("INSUFFICIENT_BALANCE", "Amount is invalid. Transaction amount cannot exceed the available balance.");
    public static final ErrorMessage BALANCE_BELOW_NDC = new ErrorMessage("BALANCE_BELOW_NDC", "Amount is invalid. Balance after this transaction cannot be lower than the NDC.");
    public static final ErrorMessage BALANCE_BELOW_CURRENT_POSITION = new ErrorMessage("BALANCE_BELOW_CURRENT_POSITION", "Amount is invalid. Balance after this transaction cannot be lower than the Current Position.");
    public static final ErrorMessage INVALID_PARTICIPANT_BALANCE_ACTION = new ErrorMessage("INVALID_PARTICIPANT_BALANCE_ACTION", "Action must be DEPOSIT or WITHDRAW.");
    public static final ErrorMessage ORG_INSUFFICIENT_BALANCE = new ErrorMessage("ORG_INSUFFICIENT_BALANCE", "The {0} organization does not have sufficient balance to perform this action.");
    public static final ErrorMessage NDC_BELOW_CURRENT_POSITION = new ErrorMessage("NDC_BELOW_CURRENT_POSITION", "Amount is invalid. This transaction amount results in NDC lower than the Current Position.");

    public static final ErrorMessage NDC_BALANCE_EXCEEDED = new ErrorMessage("NDC_BALANCE_EXCEEDED", "NDC value cannot exceed the participant’s Balance.");
    public static final ErrorMessage NDC_LOWER_THAN_CURRENT_POSITION = new ErrorMessage("NDC_LOWER_THAN_CURRENT_POSITION", "NDC value cannot be lower than the Current Position.");

    public static final ErrorMessage USER_NOT_ACTIVE = new ErrorMessage("USER_NOT_ACTIVE", "The user is not active.");
    //@@formatter:on
}
