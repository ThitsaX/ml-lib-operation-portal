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
package com.thitsaworks.operation_portal.api.operation.portal.validation;

import com.thitsaworks.operation_portal.component.common.type.PositionActionType;
import com.thitsaworks.operation_portal.component.fspiop.model.Currency;
import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;
import com.thitsaworks.operation_portal.component.misc.exception.InputException;
import com.thitsaworks.operation_portal.core.participant.exception.ParticipantErrors;

import java.math.BigDecimal;
import java.util.Locale;

public final class PostParticipantBalanceRequestValidator {

    private static final int MAX_INTEGER_DIGITS = 18;

    private static final int MAX_FRACTION_DIGITS = 4;

    private static final String DECIMAL_FORMAT = "^-?[0-9]+(?:\\.[0-9]+)?$";

    private PostParticipantBalanceRequestValidator() {
    }

    public static Values validate(String participantId,
                                  String action,
                                  String amount,
                                  String currency) {

        return new Values(validateParticipantId(participantId),
                          validateAction(action),
                          validateAmount(amount),
                          validateCurrency(currency));
    }

    private static String validateParticipantId(String participantId) {

        if (participantId == null || participantId.isBlank()) {
            throw invalid(ParticipantErrors.PARTICIPANT_BALANCE_PARTICIPANT_REQUIRED);
        }

        return participantId.trim();
    }

    private static PositionActionType validateAction(String action) {

        if (action == null || action.isBlank()) {
            throw invalid(ParticipantErrors.INVALID_PARTICIPANT_BALANCE_ACTION);
        }

        PositionActionType positionActionType;

        try {
            positionActionType = PositionActionType.valueOf(action.trim()
                                                                  .toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw invalid(ParticipantErrors.INVALID_PARTICIPANT_BALANCE_ACTION);
        }

        if (positionActionType != PositionActionType.DEPOSIT
            && positionActionType != PositionActionType.WITHDRAW) {
            throw invalid(ParticipantErrors.INVALID_PARTICIPANT_BALANCE_ACTION);
        }

        return positionActionType;
    }

    private static BigDecimal validateAmount(String amount) {

        if (amount == null || amount.isBlank()) {
            throw invalid(ParticipantErrors.PARTICIPANT_BALANCE_AMOUNT_REQUIRED);
        }

        String normalizedAmount = amount.trim();

        if (!normalizedAmount.matches(DECIMAL_FORMAT)) {
            throw invalid(ParticipantErrors.INVALID_PARTICIPANT_BALANCE_AMOUNT_FORMAT);
        }

        BigDecimal decimalAmount;

        try {
            decimalAmount = new BigDecimal(normalizedAmount);
        } catch (NumberFormatException exception) {
            throw invalid(ParticipantErrors.INVALID_PARTICIPANT_BALANCE_AMOUNT_FORMAT);
        }

        if (decimalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw invalid(ParticipantErrors.INVALID_PARTICIPANT_BALANCE_AMOUNT);
        }

        int fractionDigits = Math.max(decimalAmount.scale(), 0);
        int integerDigits = Math.max(decimalAmount.precision() - decimalAmount.scale(), 0);

        if (integerDigits > MAX_INTEGER_DIGITS || fractionDigits > MAX_FRACTION_DIGITS) {
            throw invalid(ParticipantErrors.INVALID_PARTICIPANT_BALANCE_AMOUNT_SCALE);
        }

        return decimalAmount;
    }

    private static Currency validateCurrency(String currency) {

        if (currency == null || currency.isBlank()) {
            throw invalid(ParticipantErrors.PARTICIPANT_BALANCE_CURRENCY_REQUIRED);
        }

        try {
            return Currency.valueOf(currency.trim()
                                            .toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw invalid(ParticipantErrors.INVALID_PARTICIPANT_BALANCE_CURRENCY);
        }
    }

    private static InputException invalid(ErrorMessage errorMessage) {

        return new InputException(errorMessage);
    }

    public record Values(String participantId,
                         PositionActionType action,
                         BigDecimal amount,
                         Currency currency) {
    }
}
