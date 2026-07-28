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

import com.thitsaworks.operation_portal.component.common.type.ParticipantName;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.participant.command.ModifyParticipantTypeListCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyParticipantType;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_MANAGEMENT)
public class ModifyParticipantTypeHandler
    extends OperationPortalUseCase<ModifyParticipantType.Input, ModifyParticipantType.Output>
    implements ModifyParticipantType {

    private final ModifyParticipantTypeListCommand modifyParticipantTypeListCommand;

    @Autowired
    public ModifyParticipantTypeHandler(ModifyParticipantTypeListCommand modifyParticipantTypeListCommand,
                                        PrincipalCache principalCache,
                                        ActionAuthorizationManager actionAuthorizationManager) {

        super(principalCache, actionAuthorizationManager);

        this.modifyParticipantTypeListCommand = modifyParticipantTypeListCommand;
    }

    @Override
    public Output onExecute(Input input) throws DomainException {

        List<ModifyParticipantTypeListCommand.ParticipantTypeInfo> participantTypeInfoList = new ArrayList<>();

        for (var participantTypeInfo : input.participantTypeInfoList()) {
            participantTypeInfoList.add(new ModifyParticipantTypeListCommand.ParticipantTypeInfo(
                new ParticipantName(participantTypeInfo.participantName()),
                normalizeParticipantType(participantTypeInfo.participantType())));
        }

        var output = this.modifyParticipantTypeListCommand.execute(
            new ModifyParticipantTypeListCommand.Input(participantTypeInfoList));

        List<ParticipantTypeInfo> responseParticipantTypeInfoList = new ArrayList<>();

        for (var participantTypeInfo : output.participantTypeInfoList()) {
            responseParticipantTypeInfoList.add(new ModifyParticipantType.ParticipantTypeInfo(
                participantTypeInfo.participantName().getValue(),
                participantTypeInfo.participantType()));
        }

        return new Output(responseParticipantTypeInfoList);
    }

    private String normalizeParticipantType(String participantType) {

        if (participantType == null || participantType.isBlank()) {
            return null;
        }

        return participantType.trim()
                              .toUpperCase(Locale.ROOT);
    }

}
