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
package com.thitsaworks.operation_portal.core.approval.exception;

import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;

public class ApprovalErrors {

    //@@formatter:off
    public static final ErrorMessage INVALID_REQUESTED_ACTION = new ErrorMessage("INVALID_REQUESTED_ACTION", "Requested Action is required and must NOT be blank or empty.");
    public static final ErrorMessage INVALID_APPROVAL_REQUEST = new ErrorMessage("INVALID_APPROVAL_REQUEST", "Approval Request is required and must NOT be blank or empty.");
    public static final ErrorMessage INVALID_DFSP = new ErrorMessage("INVALID_DFSP", "For DFSP is required and must NOT be blank or empty.");
    public static final ErrorMessage INVALID_CURRENCY = new ErrorMessage("INVALID_CURRENCY", "Currency is required and must NOT be blank or empty.");
    public static final ErrorMessage INVALID_AMOUNT = new ErrorMessage("INVALID_AMOUNT", "Amount is required and must NOT be blank or empty.");
    public static final ErrorMessage INVALID_REQUESTED_BY = new ErrorMessage("INVALID_REQUESTED_BY", "Requested By is required and must NOT be blank or empty.");
    public static final ErrorMessage INVALID_RESPONDED_BY = new ErrorMessage("INVALID_RESPONDED_BY", "Responded By is required and must NOT be blank or empty.");
    public static final ErrorMessage INVALID_REASON = new ErrorMessage("INVALID_REASON", "Reason is required and must NOT be blank or empty.");
    public static final ErrorMessage INVALID_APPROVAL_TAB_CODE = new ErrorMessage("INVALID_APPROVAL_TAB_CODE", "Approval Tab Code is invalid : [{0}].");

    public static final ErrorMessage APPROVAL_REQUEST_NOT_FOUND = new ErrorMessage("APPROVAL_REQUEST_NOT_FOUND", "System cannot find the Approval Request with provided : [{0}].");

    //@@formatter:on

}
