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
package com.thitsaworks.operation_portal.core.scheduler.command.impl;

import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.scheduler.command.CreateJobExecutionLogCommand;
import com.thitsaworks.operation_portal.core.scheduler.model.JobExecutionLog;
import com.thitsaworks.operation_portal.core.scheduler.model.repository.JobExecutionLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateJobExecutionLogCommandHandler implements CreateJobExecutionLogCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CreateJobExecutionLogCommandHandler.class);

    private final JobExecutionLogRepository jobExecutionLogRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws DomainException {

        JobExecutionLog jobExecutionLog = new JobExecutionLog(
            input.jobName(),
            input.jobStatus(),
            input.startTime()
        );

        if (input.ndcUsedPercent() != null) {
            jobExecutionLog.ndcEvaluation(
                input.participantName(),
                input.currency(),
                input.ndcUsedPercent(),
                input.thresholdPercent()
                                         );
        }

        var output = this.jobExecutionLogRepository.saveAndFlush(jobExecutionLog);

        return new Output(true, output.getJobExecutionLogId());
    }

}
