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

package com.thitsaworks.operation_portal.core.revenue_config.validator;

import com.thitsaworks.operation_portal.core.revenue_party.data.RevenuePartyData;
import com.thitsaworks.operation_portal.core.revenue_party.query.RevenuePartyQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PartyRegistryValidatorHandler implements PartyRegistryValidator {

    private static final String RESPONSIBLE_MINISTRY = "responsible_ministry";

    private static final String THIRD_PARTY_PROVIDER = "3rd_party";

    private final RevenuePartyQuery revenuePartyQuery;

    @Override
    public boolean isActiveResponsibleMinistry(String partyCode) {

        return this.isActiveParty(partyCode, RESPONSIBLE_MINISTRY);
    }

    @Override
    public boolean isActiveThirdPartyProvider(String partyCode) {

        return this.isActiveParty(partyCode, THIRD_PARTY_PROVIDER);
    }

    private boolean isActiveParty(String partyCode, String expectedPartyType) {

        if (partyCode == null || partyCode.isBlank()) {
            return false;
        }

        return this.revenuePartyQuery
                   .get(partyCode)
                   .filter(RevenuePartyData::isActive)
                   .map(RevenuePartyData::partyType)
                   .map(this::normalizePartyType)
                   .filter(expectedPartyType::equals)
                   .isPresent();
    }

    private String normalizePartyType(String value) {

        return value == null ? "" : value
                                        .trim()
                                        .toLowerCase(Locale.ROOT)
                                        .replaceAll("[^a-z0-9]+", "_")
                                        .replaceAll("^_+|_+$", "");
    }

}
