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
import com.thitsaworks.operation_portal.component.common.identifier.RevenueConfigId;
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
import com.thitsaworks.operation_portal.core.approval.exception.ApprovalErrors;
import com.thitsaworks.operation_portal.core.approval.exception.ApprovalException;
import com.thitsaworks.operation_portal.core.approval.query.ApprovalRequestQuery;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.revenue_config.command.CreateRevenueConfigCommand;
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
import java.util.Comparator;
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

    private static final String REVENUE_CONFIG_ID_FIELD_KEY = "revenue_config_id";

    private static final String TAX_CODE_ID_FIELD_KEY = "tax_code_id";

    private static final String TAX_CODE_DESCRIPTION_FIELD_KEY = "tax_code_description";

    private static final String CATEGORY_FIELD_KEY = "category";

    private static final String RESPONSIBLE_MINISTRY_NAME_FIELD_KEY = "responsible_ministry_name";

    private static final String THIRD_PARTY_PROVIDER_NAME_FIELD_KEY = "third_party_provider_name";

    private static final String PERCENTAGES_FIELD_KEY = "percentages";

    private static final String EFFECTIVE_DATE_FIELD_KEY = "effective_date";

    private static final Comparator<RevenueConfigData> LATEST_UPDATED_REVENUE_CONFIG_FIRST = Comparator
                                                                                                 .comparing(
                                                                                                     ModifyRevenueApprovalActionHandler::updatedAtOrCreatedAt,
                                                                                                     Comparator.nullsFirst(
                                                                                                         Comparator.naturalOrder()))
                                                                                                 .thenComparing(
                                                                                                     revenueConfig -> revenueConfig
                                                                                                                          .revenueConfigId()
                                                                                                                          .getEntityId(),
                                                                                                     Comparator.nullsLast(
                                                                                                         Comparator.naturalOrder()))
                                                                                                 .reversed();

    private final ObjectMapper objectMapper;

    private final ModifyApprovalActionCommand modifyApprovalActionCommand;

    private final ApprovalRequestQuery approvalRequestQuery;

    private final RevenueConfigQuery revenueConfigQuery;

    private final CreateRevenueConfigCommand createRevenueConfigCommand;

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
                                              CreateRevenueConfigCommand createRevenueConfigCommand,
                                              ModifyRevenueConfigStatusCommand modifyRevenueConfigStatusCommand) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.objectMapper = objectMapper;
        this.modifyApprovalActionCommand = modifyApprovalActionCommand;
        this.approvalRequestQuery = approvalRequestQuery;
        this.revenueConfigQuery = revenueConfigQuery;
        this.createRevenueConfigCommand = createRevenueConfigCommand;
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
            approvalRequestData.getRequestedAction());

        RevenueConfigData revenueConfig =
            requestedAction == RevenueActionType.CREATE_REVENUE_CONFIG ? null :
                this.revenueConfig(approvalRequestData);
        Instant respondedDate = Instant.now();

        if (input.action() == ApprovalActionType.REJECTED) {
            this.validateRejectedReason(input.reason());

        } else if (input.action() == ApprovalActionType.APPROVED) {

            if (requestedAction == RevenueActionType.CREATE_REVENUE_CONFIG) {
                this.createRevenueConfig(
                    approvalRequestData, input.responseUserId(), respondedDate);

            } else if (requestedAction == RevenueActionType.UPDATE_REVENUE_CONFIG) {

                this.createModifiedRevenueConfig(
                    approvalRequestData, revenueConfig, input.responseUserId(), respondedDate);

            } else if (requestedAction == RevenueActionType.DELETE_REVENUE_CONFIG) {

                this.modifyRevenueConfigStatusCommand.execute(
                    new ModifyRevenueConfigStatusCommand.Input(
                        revenueConfig.revenueConfigId(), RevenueConfigStatus.INACTIVE,
                        input.responseUserId(), respondedDate));
            }
        }

        ModifyApprovalActionCommand.Output output = this.executeApprovalAction(input);

        return new Output(output.approvalRequestId());
    }

    private void validateRejectedReason(String reason) throws ApprovalException {

        if (reason == null || reason.isBlank()) {
            throw new ApprovalException(ApprovalErrors.INVALID_REASON);
        }
    }

    private void createRevenueConfig(ApprovalRequestData approvalRequestData,
                                     UserId responseUserId,
                                     Instant respondedDate)
        throws DomainException, JsonProcessingException {

        List<BigDecimal> percentages = this.approvedPercentages(approvalRequestData);

        this.createRevenueConfigCommand.execute(new CreateRevenueConfigCommand.Input(
            this.requiredFieldOrAfterValue(approvalRequestData, TAX_CODE_ID_FIELD_KEY),
            this.requiredFieldOrAfterValue(approvalRequestData, TAX_CODE_DESCRIPTION_FIELD_KEY),
            RevenueConfigCategory.valueOf(
                this.requiredFieldOrAfterValue(approvalRequestData, CATEGORY_FIELD_KEY)),
            this.requiredFieldValue(approvalRequestData, RESPONSIBLE_MINISTRY_NAME_FIELD_KEY),
            this.fieldValueOrDefault(
                approvalRequestData, THIRD_PARTY_PROVIDER_NAME_FIELD_KEY,
                null), percentages.get(0), percentages.get(1), percentages.get(2),
            percentages.get(3), responseUserId, this.toNullableInstant(
            this.afterValueOrDefault(approvalRequestData, EFFECTIVE_DATE_FIELD_KEY, null)),
            RevenueConfigStatus.ACTIVE, respondedDate, true));
    }

    private void createModifiedRevenueConfig(ApprovalRequestData approvalRequestData,
                                             RevenueConfigData revenueConfig,
                                             UserId responseUserId,
                                             Instant respondedDate)
        throws DomainException, JsonProcessingException {

        List<BigDecimal> percentages = this.approvedPercentages(approvalRequestData, revenueConfig);

        Instant effectiveDate = this.toNullableInstant(
            this.afterValueOrDefault(
                approvalRequestData, EFFECTIVE_DATE_FIELD_KEY,
                this.toNullableString(revenueConfig.effectiveDate())));

        String taxCodeId = this.afterOrFieldValueOrDefault(
            approvalRequestData, TAX_CODE_ID_FIELD_KEY, revenueConfig.taxCodeId());

        this.createRevenueConfigCommand.execute(new CreateRevenueConfigCommand.Input(
            taxCodeId,
            this.afterOrFieldValueOrDefault(
                approvalRequestData, TAX_CODE_DESCRIPTION_FIELD_KEY,
                revenueConfig.taxCodeDescription()), RevenueConfigCategory.valueOf(
            this.afterValueOrDefault(
                approvalRequestData, CATEGORY_FIELD_KEY,
                revenueConfig.category().name())),
            this.fieldValueOrDefault(
                approvalRequestData, RESPONSIBLE_MINISTRY_NAME_FIELD_KEY,
                revenueConfig.responsibleMinistryCode()), this.fieldValueOrDefault(
            approvalRequestData, THIRD_PARTY_PROVIDER_NAME_FIELD_KEY,
            revenueConfig.thirdPartyProviderCode()), percentages.get(0), percentages.get(1),
            percentages.get(2), percentages.get(3), responseUserId, effectiveDate,

            RevenueConfigStatus.ACTIVE, respondedDate, false));
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

    private List<BigDecimal> approvedPercentages(ApprovalRequestData approvalRequestData)
        throws DomainException, JsonProcessingException {

        ApprovalRequestFieldDetailData percentageDetail = this
                                                              .fieldDetail(
                                                                  approvalRequestData,
                                                                  PERCENTAGES_FIELD_KEY)
                                                              .orElseThrow(
                                                                  () -> new RevenueConfigException(
                                                                      RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(
                                                                          PERCENTAGES_FIELD_KEY)));

        Map<String, BigDecimal> afterPercentages = this.objectMapper.readValue(
            percentageDetail.getAfterValue(),
            new TypeReference<LinkedHashMap<String, BigDecimal>>() { });

        return this.percentageValues(afterPercentages);
    }

    private RevenueConfigData revenueConfig(ApprovalRequestData approvalRequestData)
        throws RevenueConfigException {

        RevenueConfigId revenueConfigId = this.requiredLookupRevenueConfigId(approvalRequestData);

        return this.revenueConfigQuery
                   .findById(revenueConfigId)
                   .orElseThrow(() -> new RevenueConfigException(
                       RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(revenueConfigId)));
    }

    private List<BigDecimal> percentageValues(Map<String, BigDecimal> percentages) {

        var percentageValues = new ArrayList<>(percentages.values());
        while (percentageValues.size() < 4) {
            percentageValues.add(BigDecimal.ZERO);
        }
        return percentageValues;
    }

    private RevenueConfigId requiredLookupRevenueConfigId(ApprovalRequestData approvalRequestData)
        throws RevenueConfigException {

        Optional<RevenueConfigId> revenueConfigId = this
                                                        .fieldDetail(
                                                            approvalRequestData,
                                                            REVENUE_CONFIG_ID_FIELD_KEY)
                                                        .map(
                                                            ApprovalRequestFieldDetailData::getFieldValue)
                                                        .filter(value -> !value.isBlank())
                                                        .map(value -> new RevenueConfigId(
                                                            Long.parseLong(value)));

        if (revenueConfigId.isPresent() &&
                this.revenueConfigQuery.findById(revenueConfigId.get()).isPresent()) {
            return revenueConfigId.get();
        }

        String taxCodeId = this.requiredLookupTaxCodeId(approvalRequestData);
        Instant now = Instant.now();

        return this.revenueConfigQuery
                   .findByTaxCodeId(taxCodeId)
                   .stream()
                   .filter(revenueConfig -> revenueConfig.status() == RevenueConfigStatus.ACTIVE)
                   .filter(revenueConfig -> this.isCurrent(revenueConfig, now))
                   .sorted(LATEST_UPDATED_REVENUE_CONFIG_FIRST)
                   .map(RevenueConfigData::revenueConfigId)
                   .findFirst()
                   .orElseThrow(() -> new RevenueConfigException(
                       RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(taxCodeId)));
    }

    private String requiredLookupTaxCodeId(ApprovalRequestData approvalRequestData)
        throws RevenueConfigException {

        ApprovalRequestFieldDetailData taxCodeDetail = this
                                                           .fieldDetail(
                                                               approvalRequestData,
                                                               TAX_CODE_ID_FIELD_KEY)
                                                           .orElseThrow(
                                                               () -> new RevenueConfigException(
                                                                   RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(
                                                                       TAX_CODE_ID_FIELD_KEY)));

        if (taxCodeDetail.getBeforeValue() != null && !taxCodeDetail.getBeforeValue().isBlank()) {
            return taxCodeDetail.getBeforeValue();
        }

        if (taxCodeDetail.getFieldValue() != null && !taxCodeDetail.getFieldValue().isBlank()) {
            return taxCodeDetail.getFieldValue();
        }

        if (taxCodeDetail.getAfterValue() != null && !taxCodeDetail.getAfterValue().isBlank()) {
            return taxCodeDetail.getAfterValue();
        }

        throw new RevenueConfigException(
            RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(TAX_CODE_ID_FIELD_KEY));
    }

    private String requiredFieldOrAfterValue(ApprovalRequestData approvalRequestData,
                                             String fieldKey) throws RevenueConfigException {

        ApprovalRequestFieldDetailData fieldDetail = this
                                                         .fieldDetail(approvalRequestData, fieldKey)
                                                         .orElseThrow(
                                                             () -> new RevenueConfigException(
                                                                 RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(
                                                                     fieldKey)));

        if (fieldDetail.getFieldValue() != null && !fieldDetail.getFieldValue().isBlank()) {
            return fieldDetail.getFieldValue();
        }

        if (fieldDetail.getAfterValue() != null && !fieldDetail.getAfterValue().isBlank()) {
            return fieldDetail.getAfterValue();
        }

        throw new RevenueConfigException(
            RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(fieldKey));
    }

    private String requiredFieldValue(ApprovalRequestData approvalRequestData, String fieldKey)
        throws RevenueConfigException {

        return this
                   .fieldDetail(approvalRequestData, fieldKey)
                   .map(ApprovalRequestFieldDetailData::getFieldValue)
                   .filter(value -> !value.isBlank())
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

    private String afterOrFieldValueOrDefault(ApprovalRequestData approvalRequestData,
                                              String fieldKey,
                                              String defaultValue) {

        Optional<ApprovalRequestFieldDetailData> fieldDetail = this.fieldDetail(
            approvalRequestData, fieldKey);
        if (fieldDetail.isEmpty()) {
            return defaultValue;
        }

        String afterValue = fieldDetail.get().getAfterValue();
        if (afterValue != null && !afterValue.isBlank()) {
            return afterValue;
        }

        String fieldValue = fieldDetail.get().getFieldValue();
        return fieldValue != null && !fieldValue.isBlank() ? fieldValue : defaultValue;
    }

    private String fieldValueOrDefault(ApprovalRequestData approvalRequestData,
                                       String fieldKey,
                                       String defaultValue) {

        Optional<ApprovalRequestFieldDetailData> fieldDetail = this.fieldDetail(
            approvalRequestData, fieldKey);
        return fieldDetail.isPresent() ? fieldDetail.get().getFieldValue() : defaultValue;
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
                input.approvalRequestId(), input.action(), input.responseUserId(),
                input.action() == ApprovalActionType.REJECTED ? input.reason() : null));
    }

    private Instant toNullableInstant(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.matches("-?\\d+") ? Instant.ofEpochSecond(Long.parseLong(value)) :
                   Instant.parse(value);
    }

    private String toNullableString(Instant value) {

        return value == null ? null : value.toString();
    }

    private boolean isCurrent(RevenueConfigData revenueConfig, Instant now) {

        Instant effectiveDate = revenueConfig.effectiveDate();
        return effectiveDate == null || !effectiveDate.isAfter(now);
    }

    private static Instant updatedAtOrCreatedAt(RevenueConfigData revenueConfig) {

        return revenueConfig.updatedAt() != null ? revenueConfig.updatedAt() :
                   revenueConfig.createdAt();
    }

}
