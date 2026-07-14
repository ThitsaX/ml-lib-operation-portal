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
package com.thitsaworks.operation_portal.core.scheduler.data;

import com.thitsaworks.operation_portal.component.common.identifier.JobExecutionLogId;
import com.thitsaworks.operation_portal.component.common.type.JobStatus;
import com.thitsaworks.operation_portal.core.scheduler.model.JobExecutionLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data transfer object for JobExecutionLog entities.
 * Provides a clean interface for transferring job execution log data between layers.
 */
public record JobExecutionLogData(
        JobExecutionLogId jobExecutionLogId,
        String jobName,
        JobStatus status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String executionMessage,
        String participantName,
        String currency,
        BigDecimal ndcUsedPercent,
        BigDecimal thresholdPercent
) {
    /**
     * Creates a new JobExecutionLogData from a JobExecutionLog entity.
     *
     * @param log the JobExecutionLog entity to convert
     */
    public JobExecutionLogData(JobExecutionLog log) {
        this(
                log.getJobExecutionLogId(),
                log.getJobName(),
                log.getJobStatus(),
                log.getStartTime(),
                log.getEndTime(),
                log.getExecutionMessage(),
                log.getParticipantName(),
                log.getCurrency(),
                log.getNdcUsedPercent(),
                log.getThresholdPercent()
        );
    }
}
