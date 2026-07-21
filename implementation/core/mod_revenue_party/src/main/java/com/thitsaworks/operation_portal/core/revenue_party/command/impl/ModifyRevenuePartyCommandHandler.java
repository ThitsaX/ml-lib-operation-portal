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
package com.thitsaworks.operation_portal.core.revenue_party.command.impl;

import com.thitsaworks.operation_portal.component.common.type.RevenuePartyActionType;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.revenue_party.command.ModifyRevenuePartyCommand;
import com.thitsaworks.operation_portal.core.revenue_party.exception.RevenuePartyErrors;
import com.thitsaworks.operation_portal.core.revenue_party.exception.RevenuePartyException;
import com.thitsaworks.operation_portal.core.revenue_party.model.RevenueParty;
import com.thitsaworks.operation_portal.core.revenue_party.model.RevenuePartyHistory;
import com.thitsaworks.operation_portal.core.revenue_party.repository.RevenuePartyHistoryRepository;
import com.thitsaworks.operation_portal.core.revenue_party.repository.RevenuePartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModifyRevenuePartyCommandHandler implements ModifyRevenuePartyCommand {
    private final RevenuePartyRepository revenuePartyRepository;
    private final RevenuePartyHistoryRepository revenuePartyHistoryRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) throws RevenuePartyException {
        RevenueParty party = this.revenuePartyRepository.findById(input.revenuePartyId())
                .orElseThrow(() -> new RevenuePartyException(RevenuePartyErrors.REVENUE_PARTY_NOT_FOUND.format(input.revenuePartyId().toString())));
        if (!input.partyCode().equals(party.getPartyCode()) &&
                this.revenuePartyRepository.findByPartyCode(input.partyCode()).isPresent()) {
            throw new RevenuePartyException(RevenuePartyErrors.REVENUE_PARTY_ALREADY_REGISTERED.format(input.partyCode()));
        }
        this.revenuePartyHistoryRepository.save(new RevenuePartyHistory(party, RevenuePartyActionType.UPDATE, input.updatedBy()));
        party.partyCode(input.partyCode()).partyName(input.partyName()).partyType(input.partyType())
             .description(input.description()).updatedBy(input.updatedBy());
        this.revenuePartyRepository.save(party);
        return new Output(true, party.getRevenuePartyId());
    }
}
