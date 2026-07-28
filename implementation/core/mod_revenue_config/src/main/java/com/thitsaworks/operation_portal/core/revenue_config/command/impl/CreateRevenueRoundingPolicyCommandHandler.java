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
package com.thitsaworks.operation_portal.core.revenue_config.command.impl;

import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.revenue_config.command.CreateRevenueRoundingPolicyCommand;
import com.thitsaworks.operation_portal.core.revenue_config.model.RevenueRoundingPolicy;
import com.thitsaworks.operation_portal.core.revenue_config.model.repository.RevenueRoundingPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateRevenueRoundingPolicyCommandHandler
    implements CreateRevenueRoundingPolicyCommand {

    private final RevenueRoundingPolicyRepository revenueRoundingPolicyRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) {

        RevenueRoundingPolicy policy = new RevenueRoundingPolicy(
            input.roundingMode(), input.remainderRecipient(), input.createdBy());

        this.revenueRoundingPolicyRepository.save(policy);

        return new Output(policy.getRevenueRoundingPolicyId());
    }
}
