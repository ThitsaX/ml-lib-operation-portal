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
package com.thitsaworks.operation_portal.core.scheduler.model;

import com.thitsaworks.operation_portal.component.common.identifier.JobExecutionLogId;
import com.thitsaworks.operation_portal.component.common.type.JobStatus;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_job_execution_log")
@Getter
@Setter
@NoArgsConstructor
public class JobExecutionLog extends JpaEntity<JobExecutionLogId> {

    @EmbeddedId
    private JobExecutionLogId jobExecutionLogId;
    
    @Column(name = "job_name")
    private String jobName;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JobStatus jobStatus;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "execution_message")
    private String executionMessage;

    @Column(name = "participant_name")
    private String participantName;

    @Column(name = "currency")
    private String currency;

    @Column(name = "ndc_used_percent", precision = 18, scale = 4)
    private BigDecimal ndcUsedPercent;

    @Column(name = "threshold_percent", precision = 18, scale = 4)
    private BigDecimal thresholdPercent;

    public JobExecutionLog(String jobName, JobStatus jobStatus, LocalDateTime startTime) {
        this.jobExecutionLogId = new JobExecutionLogId(Snowflake.get().nextId());
        this.jobName = jobName;
        this.jobStatus = jobStatus;
        this.startTime = startTime;
    }

    @Override
    public JobExecutionLogId getId() {

        return this.jobExecutionLogId;
    }

    public void status(JobStatus jobStatus) {

        this.jobStatus = jobStatus;
    }

    public void endTime(LocalDateTime endTime) {

        this.endTime = endTime;
    }

    public void executionMessage(String executionMessage) {

        this.executionMessage = executionMessage;
    }

    public void ndcEvaluation(String participantName,
                              String currency,
                              BigDecimal ndcUsedPercent,
                              BigDecimal thresholdPercent) {

        this.participantName = participantName;
        this.currency = currency;
        this.ndcUsedPercent = ndcUsedPercent;
        this.thresholdPercent = thresholdPercent;
    }
    
}
