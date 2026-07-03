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
package com.thitsaworks.operation_portal.core.scheduler.exception;

import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;

public class SchedulerErrors {

    //@@formatter:off
    public static final ErrorMessage SCHEDULER_CONFIG_NOT_FOUND = new ErrorMessage("SCHEDULER_CONFIG_NOT_FOUND", "System cannot find the scheduler config with provided ID : [{0}].");
    public static final ErrorMessage SCHEDULER_ALREADY_REGISTERED = new ErrorMessage("SCHEDULER_ALREADY_REGISTERED", "The Scheduler has already registered in the system with provided name : [{0}].");

    public static final ErrorMessage JOB_EXECUTION_LOG_NOT_FOUND = new ErrorMessage("JOB_EXECUTION_LOG_NOT_FOUND", "System cannot find the job execution log with provided ID : [{0}].");
    //@@formatter:on
}
