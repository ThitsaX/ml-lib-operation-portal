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
package com.thitsaworks.operation_portal.core.hub_services.query;


import com.thitsaworks.operation_portal.core.hub_services.data.TransferData;
import com.thitsaworks.operation_portal.core.hub_services.exception.HubServicesException;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public interface GetTransfersQuery {

    @Value
    class Input {

        private String fromDate;

        private String toDate;

        private String transferId;

        private String payerFspId;

        private String payeeFspId;

        private String payerIdentifierTypeId;

        private String payeeIdentifierTypeId;

        private String payerIdentifierValue;

        private String payeeIdentifierValue;

        private String currencyId;

        private String transferStateId;

        private String fspName;

        private String timeZone;

        private Integer page;

        private Integer pageSize;

    }

    @Value
    class Output {

        private List<TransferData> transferInfoList;

        private long totalCount;

        @Value
        public static class TransferInfo implements Serializable {

            private String transferId;

            private String state;

            private String type;

            private String currency;

            private BigDecimal amount;

            private String payerDfsp;

            private String payeeDfsp;

            private String windowId;

            private String settlementBatch;

            private String submittedOnDate;

        }

    }

    Output execute(Input input) throws HubServicesException;

}
