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
import com.thitsaworks.operation_portal.core.scheduler.command.CreateSchedulerConfigCommand;
import com.thitsaworks.operation_portal.core.scheduler.data.SchedulerConfigData;
import com.thitsaworks.operation_portal.core.scheduler.exception.SchedulerErrors;
import com.thitsaworks.operation_portal.core.scheduler.exception.SchedulerException;
import com.thitsaworks.operation_portal.core.scheduler.model.SchedulerConfig;
import com.thitsaworks.operation_portal.core.scheduler.model.repository.SchedulerConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateSchedulerConfigCommandHandler implements CreateSchedulerConfigCommand {

    private final SchedulerConfigRepository schedulerConfigRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws DomainException {

        Optional<SchedulerConfig> optionalSchedulerConfig = schedulerConfigRepository.findByName(input.name());

        if (optionalSchedulerConfig.isPresent()) {
            throw new SchedulerException(SchedulerErrors.SCHEDULER_ALREADY_REGISTERED.format(input.name()));
        }

        SchedulerConfig schedulerConfig = new SchedulerConfig(input.name(),
                                                              input.jobName(),
                                                              input.description(),
                                                              input.cronExpression(),
                                                              input.zoneId());

        this.schedulerConfigRepository.save(schedulerConfig);

        return new Output(new SchedulerConfigData(schedulerConfig));
    }

}
