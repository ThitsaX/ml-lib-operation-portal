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

import com.thitsaworks.operation_portal.component.fspiop.model.ExtensionList;
import com.thitsaworks.operation_portal.component.fspiop.model.Money;
import com.thitsaworks.operation_portal.component.misc.usecase.UseCase;

import java.io.Serializable;

public interface UpdateParticipantAmount
        extends UseCase<UpdateParticipantAmount.Input, UpdateParticipantAmount.Output> {

    public record Input(
            String participantId,
            String accountId,
            String transferId,
            String externalReference,
            String action,
            String reason,
            Money amount,
            ExtensionList extensionList
    ) implements Serializable {}

    public record Output(
            String accessKey,
            String secretKey
    ) implements Serializable {}

}
