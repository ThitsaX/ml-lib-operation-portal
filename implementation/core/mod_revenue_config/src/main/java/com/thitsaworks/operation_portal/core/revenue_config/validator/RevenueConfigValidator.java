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

import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigErrors;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigException;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RevenueConfigValidator {

    private static final BigDecimal ZERO_PERCENTAGE = BigDecimal.ZERO;

    private static final BigDecimal TOTAL_PERCENTAGE = new BigDecimal("100.00");

    private static final int PERCENTAGE_SCALE = 2;

    private final PartyRegistryValidator partyRegistryValidator;

    public void validate(RevenueConfigCategory category,
                         Long responsibleMinistryId,
                         Long thirdPartyProviderId,
                         BigDecimal golPercentage,
                         BigDecimal ministryPercentage,
                         BigDecimal thirdPartyPercentage,
                         BigDecimal sendingDfspPercentage) throws RevenueConfigException {

        if (category == null) {
            throw new RevenueConfigException(RevenueConfigErrors.INVALID_REVENUE_CONFIG_CATEGORY);
        }

        validatePartyRegistryReferences(responsibleMinistryId, thirdPartyProviderId);
        validatePercentages(golPercentage, ministryPercentage, thirdPartyPercentage, sendingDfspPercentage);
    }

    private void validatePartyRegistryReferences(Long responsibleMinistryId, Long thirdPartyProviderId)
        throws RevenueConfigException {

        if (!this.partyRegistryValidator.isActiveResponsibleMinistry(responsibleMinistryId)) {
            throw new RevenueConfigException(RevenueConfigErrors.INVALID_RESPONSIBLE_MINISTRY.format(
                String.valueOf(responsibleMinistryId)));
        }

        if (thirdPartyProviderId != null && !this.partyRegistryValidator.isActiveThirdPartyProvider(thirdPartyProviderId)) {
            throw new RevenueConfigException(RevenueConfigErrors.INVALID_THIRD_PARTY_PROVIDER.format(
                String.valueOf(thirdPartyProviderId)));
        }
    }

    private void validatePercentages(BigDecimal golPercentage,
                                     BigDecimal ministryPercentage,
                                     BigDecimal thirdPartyPercentage,
                                     BigDecimal sendingDfspPercentage) throws RevenueConfigException {

        if (isOutOfRange(golPercentage) ||
                isOutOfRange(ministryPercentage) ||
                isOutOfRange(thirdPartyPercentage) ||
                isOutOfRange(sendingDfspPercentage)) {
            throw new RevenueConfigException(RevenueConfigErrors.INVALID_REVENUE_PERCENTAGE);
        }

        BigDecimal total = golPercentage.add(ministryPercentage)
                                        .add(thirdPartyPercentage)
                                        .add(sendingDfspPercentage);
        if (total.compareTo(TOTAL_PERCENTAGE) != 0) {
            throw new RevenueConfigException(RevenueConfigErrors.INVALID_REVENUE_PERCENTAGE_TOTAL.format(total));
        }
    }

    private boolean isOutOfRange(BigDecimal percentage) {

        return percentage == null ||
                   percentage.compareTo(ZERO_PERCENTAGE) < 0 ||
                   percentage.compareTo(TOTAL_PERCENTAGE) > 0 ||
                   percentage.stripTrailingZeros().scale() > PERCENTAGE_SCALE;
    }
}
