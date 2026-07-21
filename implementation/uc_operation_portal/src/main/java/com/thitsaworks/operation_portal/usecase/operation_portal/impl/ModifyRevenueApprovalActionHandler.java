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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.ApprovalActionType;
import com.thitsaworks.operation_portal.component.common.type.ApprovalTabCode;
import com.thitsaworks.operation_portal.component.common.type.RevenueActionType;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigCategory;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigStatus;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.approval.command.ModifyApprovalActionCommand;
import com.thitsaworks.operation_portal.core.approval.data.ApprovalRequestData;
import com.thitsaworks.operation_portal.core.approval.data.ApprovalRequestFieldDetailData;
import com.thitsaworks.operation_portal.core.approval.exception.ApprovalException;
import com.thitsaworks.operation_portal.core.approval.query.ApprovalRequestQuery;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.revenue_config.command.ModifyRevenueConfigCommand;
import com.thitsaworks.operation_portal.core.revenue_config.command.ModifyRevenueConfigStatusCommand;
import com.thitsaworks.operation_portal.core.revenue_config.data.RevenueConfigData;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigErrors;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigException;
import com.thitsaworks.operation_portal.core.revenue_config.query.RevenueConfigQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.ModifyRevenueApprovalAction;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@ActionMetadata(category = ActionCategory.APPROVAL_WORKFLOW)
public class ModifyRevenueApprovalActionHandler
    extends OperationPortalAuditableUseCase<ModifyRevenueApprovalAction.Input, ModifyRevenueApprovalAction.Output>
    implements ModifyRevenueApprovalAction {

    private static final String TAX_CODE_ID_FIELD_KEY = "tax_code_id";

    private static final String TAX_CODE_DESCRIPTION_FIELD_KEY = "tax_code_description";

    private static final String CATEGORY_FIELD_KEY = "category";

    private static final String RESPONSIBLE_MINISTRY_ID_FIELD_KEY = "responsible_ministry_id";

    private static final String THIRD_PARTY_PROVIDER_ID_FIELD_KEY = "third_party_provider_id";

    private static final String PERCENTAGES_FIELD_KEY = "percentages";

    private static final String START_DATE_FIELD_KEY = "start_date";

    private final ObjectMapper objectMapper;

    private final ModifyApprovalActionCommand modifyApprovalActionCommand;

    private final ApprovalRequestQuery approvalRequestQuery;

    private final RevenueConfigQuery revenueConfigQuery;

    private final ModifyRevenueConfigCommand modifyRevenueConfigCommand;

    private final ModifyRevenueConfigStatusCommand modifyRevenueConfigStatusCommand;

    public ModifyRevenueApprovalActionHandler(CreateInputAuditCommand createInputAuditCommand,
                                              CreateOutputAuditCommand createOutputAuditCommand,
                                              CreateExceptionAuditCommand createExceptionAuditCommand,
                                              ObjectMapper objectMapper,
                                              PrincipalCache principalCache,
                                              ActionAuthorizationManager actionAuthorizationManager,
                                              ModifyApprovalActionCommand modifyApprovalActionCommand,
                                              ApprovalRequestQuery approvalRequestQuery,
                                              RevenueConfigQuery revenueConfigQuery,
                                              ModifyRevenueConfigCommand modifyRevenueConfigCommand,
                                              ModifyRevenueConfigStatusCommand modifyRevenueConfigStatusCommand) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.objectMapper = objectMapper;
        this.modifyApprovalActionCommand = modifyApprovalActionCommand;
        this.approvalRequestQuery = approvalRequestQuery;
        this.revenueConfigQuery = revenueConfigQuery;
        this.modifyRevenueConfigCommand = modifyRevenueConfigCommand;
        this.modifyRevenueConfigStatusCommand = modifyRevenueConfigStatusCommand;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException, JsonProcessingException {

        ApprovalRequestData approvalRequestData = this.approvalRequestQuery.getPendingApprovalRequestByID(
            input.approvalRequestId(), ApprovalTabCode.REVENUE.name());

        if (this.isSelfApprovalAttempt(
            approvalRequestData.getRequestedBy(), input.responseUserId())) {
            throw new IAMException(IAMErrors.SELF_APPROVAL_NOT_ALLOWED);
        }

        RevenueActionType requestedAction = this.toRequestedAction(
            approvalRequestData.getFundInOutAction());
        String taxCodeId = this.requiredAfterValue(approvalRequestData, TAX_CODE_ID_FIELD_KEY);
        RevenueConfigData revenueConfig = this.revenueConfigQuery
                                              .findByTaxCodeId(taxCodeId)
                                              .orElseThrow(() -> new RevenueConfigException(
                                                  RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(
                                                      taxCodeId)));

        if (input.action() == ApprovalActionType.REJECTED) {
            this.modifyRevenueConfigStatusCommand.execute(
                new ModifyRevenueConfigStatusCommand.Input(
                    revenueConfig.revenueConfigId(), RevenueConfigStatus.REJECTED,
                    input.responseUserId()));
        } else if (input.action() == ApprovalActionType.APPROVED) {
            if (requestedAction == RevenueActionType.CREATE_REVENUE_CONFIG) {
                this.modifyRevenueConfigStatusCommand.execute(
                    new ModifyRevenueConfigStatusCommand.Input(
                        revenueConfig.revenueConfigId(), RevenueConfigStatus.ACTIVE,
                        input.responseUserId()));
            } else if (requestedAction == RevenueActionType.UPDATE_REVENUE_CONFIG) {
                this.modifyRevenueConfig(
                    approvalRequestData, revenueConfig, input.responseUserId());
            }
        }

        ModifyApprovalActionCommand.Output output = this.executeApprovalAction(input);
        return new Output(output.approvalRequestId());
    }

    private void modifyRevenueConfig(ApprovalRequestData approvalRequestData,
                                     RevenueConfigData revenueConfig,
                                     UserId responseUserId)
        throws DomainException, JsonProcessingException {

        List<BigDecimal> percentages = this.approvedPercentages(approvalRequestData, revenueConfig);

        this.modifyRevenueConfigCommand.execute(new ModifyRevenueConfigCommand.Input(
            revenueConfig.revenueConfigId(), revenueConfig.taxCodeId(), this.afterValueOrDefault(
            approvalRequestData, TAX_CODE_DESCRIPTION_FIELD_KEY,
            revenueConfig.taxCodeDescription()), RevenueConfigCategory.valueOf(
            this.afterValueOrDefault(
                approvalRequestData, CATEGORY_FIELD_KEY,
                revenueConfig.category().name())), Long.valueOf(this.afterValueOrDefault(
            approvalRequestData, RESPONSIBLE_MINISTRY_ID_FIELD_KEY,
            String.valueOf(revenueConfig.responsibleMinistryId()))), this.toNullableLong(
            this.afterValueOrDefault(
                approvalRequestData, THIRD_PARTY_PROVIDER_ID_FIELD_KEY,
                this.toNullableString(revenueConfig.thirdPartyProviderId()))), percentages.get(0),
            percentages.get(1), percentages.get(2), percentages.get(3), responseUserId,
            this.toNullableInstant(this.afterValueOrDefault(
                approvalRequestData, START_DATE_FIELD_KEY,
                this.toNullableString(revenueConfig.startDate())))));

        this.modifyRevenueConfigStatusCommand.execute(
            new ModifyRevenueConfigStatusCommand.Input(
                revenueConfig.revenueConfigId(),
                RevenueConfigStatus.ACTIVE, responseUserId));
    }

    private List<BigDecimal> approvedPercentages(ApprovalRequestData approvalRequestData,
                                                 RevenueConfigData revenueConfig)
        throws JsonProcessingException {

        Optional<ApprovalRequestFieldDetailData> percentageDetail = this.fieldDetail(
            approvalRequestData, PERCENTAGES_FIELD_KEY);
        if (percentageDetail.isEmpty()) {
            return List.of(
                revenueConfig.golPercentage(), revenueConfig.ministryPercentage(),
                revenueConfig.thirdPartyPercentage(), revenueConfig.sendingDfspPercentage());
        }

        Map<String, BigDecimal> afterPercentages = this.objectMapper.readValue(
            percentageDetail.get().getAfterValue(),
            new TypeReference<LinkedHashMap<String, BigDecimal>>() { });
        return this.percentageValues(afterPercentages);
    }

    private List<BigDecimal> percentageValues(Map<String, BigDecimal> percentages) {

        var percentageValues = new ArrayList<>(percentages.values());
        while (percentageValues.size() < 4) {
            percentageValues.add(BigDecimal.ZERO);
        }
        return percentageValues;
    }

    private String requiredAfterValue(ApprovalRequestData approvalRequestData, String fieldKey)
        throws RevenueConfigException {

        return this
                   .fieldDetail(approvalRequestData, fieldKey)
                   .map(ApprovalRequestFieldDetailData::getAfterValue)
                   .orElseThrow(() -> new RevenueConfigException(
                       RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(fieldKey)));
    }

    private String afterValueOrDefault(ApprovalRequestData approvalRequestData,
                                       String fieldKey,
                                       String defaultValue) {

        Optional<ApprovalRequestFieldDetailData> fieldDetail = this.fieldDetail(
            approvalRequestData, fieldKey);
        return fieldDetail.isPresent() ? fieldDetail.get().getAfterValue() : defaultValue;
    }

    private Optional<ApprovalRequestFieldDetailData> fieldDetail(ApprovalRequestData approvalRequestData,
                                                                 String fieldKey) {

        return approvalRequestData
                   .getFieldDetails()
                   .stream()
                   .filter(fieldDetail -> fieldKey.equals(fieldDetail.getFieldKey()))
                   .findFirst();
    }

    private RevenueActionType toRequestedAction(String value) {

        return RevenueActionType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private boolean isSelfApprovalAttempt(UserId requestedByUserId, UserId respondedByUserId) {

        return requestedByUserId.getId().equals(respondedByUserId.getId());
    }

    private ModifyApprovalActionCommand.Output executeApprovalAction(Input input)
        throws ApprovalException {

        return this.modifyApprovalActionCommand.execute(
            new ModifyApprovalActionCommand.Input(
                input.approvalRequestId(), input.action(),
                input.responseUserId()));
    }

    private Long toNullableLong(String value) {

        return value == null || value.isBlank() ? null : Long.valueOf(value);
    }

    private Instant toNullableInstant(String value) {

        return value == null || value.isBlank() ? null : Instant.parse(value);
    }

    private String toNullableString(Long value) {

        return value == null ? null : String.valueOf(value);
    }

    private String toNullableString(Instant value) {

        return value == null ? null : value.toString();
    }

}
