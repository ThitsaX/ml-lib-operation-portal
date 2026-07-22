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
package com.thitsaworks.operation_portal.usecase.operation_portal.impl;

import com.thitsaworks.operation_portal.component.common.identifier.ThresholdConfigurationId;
import com.thitsaworks.operation_portal.component.common.identifier.ThresholdDetailId;
import com.thitsaworks.operation_portal.component.common.type.ThresholdScopeType;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;
import com.thitsaworks.operation_portal.component.misc.exception.InputException;
import com.thitsaworks.operation_portal.core.hub_services.data.HubParticipantDetailData;
import com.thitsaworks.operation_portal.core.hub_services.query.HubParticipantQuery;
import com.thitsaworks.operation_portal.core.notification.data.ThresholdConfigurationData;
import com.thitsaworks.operation_portal.core.notification.model.ThresholdDetail;
import com.thitsaworks.operation_portal.core.notification.query.ThresholdConfigurationQuery;
import com.thitsaworks.operation_portal.core.notification.query.ThresholdDetailQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ThresholdDetailParticipantCurrencyValidator {

    private static final Integer POSITION_ACCOUNT_TYPE_ID = 1;

    private final ThresholdConfigurationQuery thresholdConfigurationQuery;

    private final ThresholdDetailQuery thresholdDetailQuery;

    private final HubParticipantQuery hubParticipantQuery;

    public String validateForConfiguration(ThresholdConfigurationId thresholdConfigurationId,
                                           Long participantCurrencyId,
                                           String currency) throws DomainException {

        ThresholdConfigurationData configuration = this.thresholdConfigurationQuery
            .get(thresholdConfigurationId)
            .orElseThrow(() -> inputException(
                "THRESHOLD_CONFIGURATION_NOT_FOUND",
                "Threshold configuration was not found."));

        if (configuration.scopeType() != ThresholdScopeType.DFSP
            || configuration.dfspId() == null
            || configuration.dfspId().isBlank()) {

            throw inputException(
                "THRESHOLD_DETAIL_REQUIRES_DFSP_CONFIGURATION",
                "Threshold detail must reference a DFSP configuration.");
        }

        String normalizedCurrency = ThresholdDetail.normalizeCurrency(currency);
        boolean valid = this.hubParticipantQuery.getHubParticipantDetailDataList()
            .stream()
            .filter(participant -> configuration.dfspId().equals(participant.getParticipantName()))
            .map(HubParticipantDetailData::getAccounts)
            .flatMap(java.util.Collection::stream)
            .anyMatch(account -> matches(account, participantCurrencyId, normalizedCurrency));

        if (!valid) {
            throw inputException(
                "THRESHOLD_DETAIL_PARTICIPANT_CURRENCY_INVALID",
                "Participant currency does not belong to the configured DFSP position account.");
        }

        return normalizedCurrency;
    }

    public String validateForDetail(ThresholdDetailId thresholdDetailId,
                                    Long participantCurrencyId,
                                    String currency) throws DomainException {

        var detail = this.thresholdDetailQuery.get(thresholdDetailId)
            .orElseThrow(() -> inputException(
                "THRESHOLD_DETAIL_NOT_FOUND",
                "Threshold detail was not found."));

        return validateForConfiguration(
            detail.thresholdConfigurationId(), participantCurrencyId, currency);
    }

    private static boolean matches(HubParticipantDetailData.AccountData account,
                                   Long participantCurrencyId,
                                   String currency) {

        return account.getParticipantCurrencyId() != null
            && participantCurrencyId != null
            && account.getParticipantCurrencyId().longValue() == participantCurrencyId
            && currency.equalsIgnoreCase(account.getCurrencyId())
            && POSITION_ACCOUNT_TYPE_ID.equals(account.getLedgerAccountTypeId());
    }

    private static InputException inputException(String code, String message) {

        return new InputException(new ErrorMessage(code, message));
    }
}
