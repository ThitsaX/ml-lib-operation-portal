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

package com.thitsaworks.operation_portal.core.revenue_config.exception;

import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;

public class RevenueConfigErrors {

    //@@formatter:off
    public static final ErrorMessage REVENUE_CONFIG_NOT_FOUND = new ErrorMessage("REVENUE_CONFIG_NOT_FOUND", "System cannot find the revenue configuration with provided ID : [{0}].");
    public static final ErrorMessage TAX_CODE_ALREADY_REGISTERED = new ErrorMessage("TAX_CODE_ALREADY_REGISTERED", "Revenue configuration has already registered in the system with provided Tax Code ID : [{0}].");
    public static final ErrorMessage INVALID_REVENUE_CONFIG_CATEGORY = new ErrorMessage("INVALID_REVENUE_CONFIG_CATEGORY", "Revenue configuration category must be DOMESTIC or CUSTOMS.");
    public static final ErrorMessage INVALID_REVENUE_PERCENTAGE_TOTAL = new ErrorMessage("INVALID_REVENUE_PERCENTAGE_TOTAL", "Revenue configuration percentage total must equal 100. Current total is [{0}].");
    public static final ErrorMessage INVALID_REVENUE_PERCENTAGE = new ErrorMessage("INVALID_REVENUE_PERCENTAGE", "Revenue configuration percentages must be between 0 and 100.");
    public static final ErrorMessage INVALID_RESPONSIBLE_MINISTRY = new ErrorMessage("INVALID_RESPONSIBLE_MINISTRY", "Responsible Ministry must reference an active Responsible Ministry from the Party Registry : [{0}].");
    public static final ErrorMessage INVALID_THIRD_PARTY_PROVIDER = new ErrorMessage("INVALID_THIRD_PARTY_PROVIDER", "Third Party Provider must reference an active Third Party Provider from the Party Registry : [{0}].");
    //@@formatter:on
}
