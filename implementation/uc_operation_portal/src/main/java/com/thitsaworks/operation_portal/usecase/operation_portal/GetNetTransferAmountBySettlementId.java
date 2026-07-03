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
package com.thitsaworks.operation_portal.usecase.operation_portal;

import com.thitsaworks.operation_portal.component.misc.usecase.UseCase;
import org.luaj.vm2.ast.Str;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public interface GetNetTransferAmountBySettlementId
    extends UseCase<GetNetTransferAmountBySettlementId.Input, GetNetTransferAmountBySettlementId.Output> {

    public record Input(

        int settlementId

    ) implements Serializable { }

    public record Output(

        int settlementId,
        String settlementWindowIds,
        String windowOpenedDate,
        String windowClosedDate,
        List<Detail> details

    ) implements Serializable { }

    public record Detail(

        String participantName,
        BigDecimal participantLimit,
        BigDecimal participantBalance,
        BigDecimal debitAmount,
        BigDecimal creditAmount,
        BigDecimal ndcPercent,
        String currency,
        String participantSettlementCurrencyId
    ) implements Serializable { }

}
