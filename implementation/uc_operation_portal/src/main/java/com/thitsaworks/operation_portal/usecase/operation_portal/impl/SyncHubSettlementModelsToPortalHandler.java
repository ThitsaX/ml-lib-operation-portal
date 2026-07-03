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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.hub_services.data.HubSettlementModelData;
import com.thitsaworks.operation_portal.core.hub_services.query.HubSettlementModelQuery;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.settlement.command.CreateSettlementModelCommand;
import com.thitsaworks.operation_portal.core.settlement.data.SettlementModelData;
import com.thitsaworks.operation_portal.core.settlement.query.SettlementModelQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.SyncHubSettlementModelsToPortal;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@ActionMetadata(category = ActionCategory.SETTLEMENT_MODEL_MANAGEMENT)
public class SyncHubSettlementModelsToPortalHandler
    extends OperationPortalAuditableUseCase<SyncHubSettlementModelsToPortal.Input, SyncHubSettlementModelsToPortal.Output>
    implements SyncHubSettlementModelsToPortal {

    private static final Logger LOG = LoggerFactory.getLogger(
        SyncHubSettlementModelsToPortalHandler.class);

    private final HubSettlementModelQuery hubSettlementModelQuery;

    private final SettlementModelQuery settlementModelQuery;

    private final CreateSettlementModelCommand createSettlementModelCommand;

    public SyncHubSettlementModelsToPortalHandler(CreateInputAuditCommand createInputAuditCommand,
                                                  CreateOutputAuditCommand createOutputAuditCommand,
                                                  CreateExceptionAuditCommand createExceptionAuditCommand,
                                                  ObjectMapper objectMapper,
                                                  PrincipalCache principalCache,
                                                  ActionAuthorizationManager actionAuthorizationManager,
                                                  HubSettlementModelQuery hubSettlementModelQuery,
                                                  SettlementModelQuery settlementModelQuery,
                                                  CreateSettlementModelCommand createSettlementModelCommand) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.hubSettlementModelQuery = hubSettlementModelQuery;
        this.settlementModelQuery = settlementModelQuery;
        this.createSettlementModelCommand = createSettlementModelCommand;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        List<HubSettlementModelData> hubSettlementModelList = this.hubSettlementModelQuery.getSettlementModelList();

        Set<String> existingSettlementModelNames = this.settlementModelQuery
                                                       .getSettlementModels()
                                                       .stream()
                                                       .map(SettlementModelData::name)
                                                       .collect(Collectors.toSet());

        List<CreatedSettlementModelInfo> createdSettlementModelInfoList = new ArrayList<>();

        for (HubSettlementModelData hubSettlementModel : hubSettlementModelList) {
            if (!existingSettlementModelNames.contains(hubSettlementModel.name())) {

                CreateSettlementModelCommand.Output output = this.createSettlementModelCommand.execute(
                    new CreateSettlementModelCommand.Input(
                        hubSettlementModel.name(),
                        hubSettlementModel.settlementInterchangeName() + "_" +
                            hubSettlementModel.name() + "_" +
                            hubSettlementModel.settlementGranularityName(),
                        hubSettlementModel.currencyId(), hubSettlementModel.isActive(),
                        false, // set Manual as default
                        true, ZoneId.of("UTC").getId(), hubSettlementModel.requireLiquidityCheck(),
                        hubSettlementModel.autoPositionReset(), hubSettlementModel.adjustPosition(),
                        new ArrayList<>()));

                createdSettlementModelInfoList.add(
                    new CreatedSettlementModelInfo(
                        output.settlementModelId().getId(), hubSettlementModel.name()));

            }
        }

        try {
            LOG.info("Created settlement models : [{}]", createdSettlementModelInfoList);

        } catch (Exception e) {
            LOG.info("Something went wrong: [{}]", e.getMessage());
        }

        return new Output(true);
    }

    record CreatedSettlementModelInfo(Long settlementModelId, String name) { }

}
