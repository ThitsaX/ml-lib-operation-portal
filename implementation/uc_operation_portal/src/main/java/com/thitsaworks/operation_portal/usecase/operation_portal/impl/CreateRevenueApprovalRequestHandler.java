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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.common.type.ApprovalTabCode;
import com.thitsaworks.operation_portal.component.common.type.RevenueActionType;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigStatus;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.component.misc.util.TimeZoneUtil;
import com.thitsaworks.operation_portal.core.approval.command.CreateApprovalRequestByCategoryCommand;
import com.thitsaworks.operation_portal.core.approval.command.CreateApprovalRequestFieldDetailCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.revenue_config.data.RevenueConfigData;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigErrors;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigException;
import com.thitsaworks.operation_portal.core.revenue_config.query.RevenueConfigQuery;
import com.thitsaworks.operation_portal.core.revenue_config.validator.RevenueConfigValidator;
import com.thitsaworks.operation_portal.core.revenue_party.data.RevenuePartyData;
import com.thitsaworks.operation_portal.core.revenue_party.query.RevenuePartyQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.CreateRevenueApprovalRequest;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@ActionMetadata(category = ActionCategory.APPROVAL_WORKFLOW)
public class CreateRevenueApprovalRequestHandler
    extends OperationPortalAuditableUseCase<CreateRevenueApprovalRequest.Input, CreateRevenueApprovalRequest.Output>
    implements CreateRevenueApprovalRequest {

    private static final String REQUEST_CATEGORY = "REVENUE_CONFIG";

    private static final DateTimeFormatter EFFECTIVE_DATE_DISPLAY_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String REVENUE_CONFIG_ID_FIELD_KEY = "revenue_config_id";

    private static final String TAX_CODE_ID_FIELD_KEY = "tax_code_id";

    private static final String TAX_CODE_DESCRIPTION_FIELD_KEY = "tax_code_description";

    private static final String CATEGORY_FIELD_KEY = "category";

    private static final String RESPONSIBLE_MINISTRY_NAME_FIELD_KEY = "responsible_ministry_name";

    private static final String THIRD_PARTY_PROVIDER_NAME_FIELD_KEY = "third_party_provider_name";

    private static final String PERCENTAGES_FIELD_KEY = "percentages";

    private static final String PERCENTAGES_FIELD_LABEL = "Percentages";

    private static final String EFFECTIVE_DATE_FIELD_KEY = "effective_date";

    private static final String EFFECTIVE_DATE_DISPLAY_FIELD_KEY = "effective_date_display";

    private static final String EFFECTIVE_TIMEZONE_FIELD_KEY = "effective_timezone";

    private static final String STATUS_FIELD_KEY = "status";

    private final CreateApprovalRequestByCategoryCommand createApprovalRequestByCategoryCommand;

    private final CreateApprovalRequestFieldDetailCommand createApprovalRequestFieldDetailCommand;

    private final RevenueConfigQuery revenueConfigQuery;

    private final RevenueConfigValidator revenueConfigValidator;

    private final RevenuePartyQuery revenuePartyQuery;

    private final ObjectMapper objectMapper;

    public CreateRevenueApprovalRequestHandler(CreateInputAuditCommand createInputAuditCommand,
                                               CreateOutputAuditCommand createOutputAuditCommand,
                                               CreateExceptionAuditCommand createExceptionAuditCommand,
                                               ObjectMapper objectMapper,
                                               PrincipalCache principalCache,
                                               ActionAuthorizationManager actionAuthorizationManager,
                                               CreateApprovalRequestByCategoryCommand createApprovalRequestByCategoryCommand,
                                               CreateApprovalRequestFieldDetailCommand createApprovalRequestFieldDetailCommand,
                                               RevenueConfigQuery revenueConfigQuery,
                                               RevenueConfigValidator revenueConfigValidator,
                                               RevenuePartyQuery revenuePartyQuery) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.createApprovalRequestByCategoryCommand = createApprovalRequestByCategoryCommand;
        this.createApprovalRequestFieldDetailCommand = createApprovalRequestFieldDetailCommand;
        this.revenueConfigQuery = revenueConfigQuery;
        this.revenueConfigValidator = revenueConfigValidator;
        this.revenuePartyQuery = revenuePartyQuery;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException, JsonProcessingException {

        RevenueActionType requestedAction = this.toRequestedAction(input.requestedAction());
        RevenueConfigData existingRevenueConfig = null;

        if (requestedAction == RevenueActionType.CREATE_REVENUE_CONFIG) {

            this.validateRevenueConfig(input, true);

        } else if (requestedAction == RevenueActionType.UPDATE_REVENUE_CONFIG ||
                       requestedAction == RevenueActionType.DELETE_REVENUE_CONFIG) {

            existingRevenueConfig = this.existingRevenueConfig(input);
        }

        var output = this.createApprovalRequestByCategoryCommand.execute(
            new CreateApprovalRequestByCategoryCommand.Input(
                requestedAction, input.requestedBy(),
                REQUEST_CATEGORY));

        if (requestedAction == RevenueActionType.CREATE_REVENUE_CONFIG) {

            this.createRequestDetails(output, input);

        } else if (requestedAction == RevenueActionType.UPDATE_REVENUE_CONFIG) {

            this.updateRequestDetails(output, input, existingRevenueConfig);

        } else if (requestedAction == RevenueActionType.DELETE_REVENUE_CONFIG) {

            this.deleteRequestDetails(output, input, existingRevenueConfig);
        }

        return new Output(output.approvalRequestId());
    }

    private void validateRevenueConfig(Input input,
                                       boolean validateUniqueTaxCode) throws DomainException {

        if (input.category() == null) {
            throw new RevenueConfigException(RevenueConfigErrors.REVENUE_CONFIG_CATEGORY_REQUIRED);
        }

        if (validateUniqueTaxCode) {
            this.revenueConfigValidator.validateUniqueTaxCode(
                input.taxCodeId(), null);
        }

        List<BigDecimal> percentages = this.percentageValues(input.percentages());
        this.revenueConfigValidator.validate(
            input.category(), input.responsibleMinistryCode(), input.thirdPartyProviderCode(),
            percentages.get(0), percentages.get(1), percentages.get(2), percentages.get(3),
            input.effectiveDate(), input.effectiveTimezone());
    }

    private RevenueConfigData existingRevenueConfig(Input input) throws DomainException {

        if (input.revenueConfigId() == null) {
            throw new RevenueConfigException(RevenueConfigErrors.REVENUE_CONFIG_ID_REQUIRED);
        }

        boolean isUpdateRevenueConfig = input
                        .requestedAction()
                        .equalsIgnoreCase(RevenueActionType.UPDATE_REVENUE_CONFIG.name());

        if (isUpdateRevenueConfig &&
                input.category() == null) {
            throw new RevenueConfigException(RevenueConfigErrors.REVENUE_CONFIG_CATEGORY_REQUIRED);

        }

        RevenueConfigData revenueConfig = this.revenueConfigQuery
                                              .findById(input.revenueConfigId())
                                              .orElseThrow(() -> new RevenueConfigException(
                                                  RevenueConfigErrors.REVENUE_CONFIG_NOT_FOUND.format(
                                                      input.revenueConfigId())));

        if (isUpdateRevenueConfig) {
            this.revenueConfigValidator.validateTaxCodeUnchanged(
                revenueConfig.taxCodeId(), input.taxCodeId());
            this.validateRevenueConfig(input, false);
        }

        return revenueConfig;
    }

    private void createRequestDetails(CreateApprovalRequestByCategoryCommand.Output output,
                                      Input input) throws DomainException, JsonProcessingException {

        this.createFieldValueTextFieldDetail(
            output, TAX_CODE_ID_FIELD_KEY, "Tax Code ID", input.taxCodeId(), 1);

        this.createFieldValueTextFieldDetail(
            output, TAX_CODE_DESCRIPTION_FIELD_KEY,
            "Tax Code ID (Description)", input.taxCodeDescription(), 2);

        this.createTextFieldDetail(
            output, CATEGORY_FIELD_KEY, "Category", input.category().name(), 3);

        this.createRevenuePartyFieldDetail(
            output, RESPONSIBLE_MINISTRY_NAME_FIELD_KEY,
            "Responsible Ministry Name", input.responsibleMinistryCode(), 4);

        this.createRevenuePartyFieldDetail(
            output, THIRD_PARTY_PROVIDER_NAME_FIELD_KEY,
            "Third Party Provider Name", input.thirdPartyProviderCode(), 5);

        if (input.effectiveDate() != null) {
            this.createTextFieldDetail(
                output, EFFECTIVE_DATE_FIELD_KEY, "Effective Date (UTC)",
                this.toNullableString(input.effectiveDate()), 6);

            this.createTextFieldDetail(
                output, EFFECTIVE_DATE_DISPLAY_FIELD_KEY, "Effective Date",
                input.effectiveDateDisplay(), 7);
        }

        this.createTextFieldDetail(
            output, EFFECTIVE_TIMEZONE_FIELD_KEY, "Effective Timezone",
            input.effectiveTimezone(), 8);

        this.createJsonFieldDetail(
            output, PERCENTAGES_FIELD_KEY, PERCENTAGES_FIELD_LABEL, null,
            this.objectMapper.writeValueAsString(this.toPercentageJson(input.percentages())), 9);
    }

    private void updateRequestDetails(CreateApprovalRequestByCategoryCommand.Output output,
                                      Input input,
                                      RevenueConfigData revenueConfig)
        throws DomainException, JsonProcessingException {

        this.createFieldValueTextFieldDetail(
            output, REVENUE_CONFIG_ID_FIELD_KEY,
            "Revenue Config ID",
            this.toNullableString(revenueConfig.revenueConfigId().getEntityId()), 0);

        this.createChangedOrFieldValueTextFieldDetail(
            output, TAX_CODE_ID_FIELD_KEY, "Tax Code ID",
            revenueConfig.taxCodeId(), input.taxCodeId(), 1);

        this.createChangedOrFieldValueTextFieldDetail(
            output, TAX_CODE_DESCRIPTION_FIELD_KEY,
            "Tax Code ID (Description)", revenueConfig.taxCodeDescription(),
            input.taxCodeDescription(), 2);

        this.createSnapshotTextFieldDetail(
            output, CATEGORY_FIELD_KEY, "Category",
            revenueConfig.category().name(), input.category().name(), 3);

        this.createRevenuePartySnapshotFieldDetail(
            output, RESPONSIBLE_MINISTRY_NAME_FIELD_KEY,
            "Responsible Ministry Name", revenueConfig.responsibleMinistryCode(),
            input.responsibleMinistryCode(), 4);

        this.createRevenuePartySnapshotFieldDetail(
            output, THIRD_PARTY_PROVIDER_NAME_FIELD_KEY,
            "Third Party Provider Name", revenueConfig.thirdPartyProviderCode(),
            input.thirdPartyProviderCode(), 5);

        if (input.effectiveDate() != null) {
            this.createSnapshotTextFieldDetail(
                output, EFFECTIVE_DATE_FIELD_KEY, "Effective Date (UTC)",
                this.toNullableString(revenueConfig.effectiveDate()),
                this.toNullableString(input.effectiveDate()), 6);

            this.createSnapshotTextFieldDetail(
                output, EFFECTIVE_DATE_DISPLAY_FIELD_KEY, "Effective Date",
                this.formatEffectiveDateDisplay(
                    revenueConfig.effectiveDate(), revenueConfig.effectiveTimezone()),
                input.effectiveDateDisplay(), 7);
        }

        this.createSnapshotTextFieldDetail(
            output, EFFECTIVE_TIMEZONE_FIELD_KEY, "Effective Timezone",
            revenueConfig.effectiveTimezone(), input.effectiveTimezone(), 8);

        this.createChangedJsonFieldDetail(output, input, revenueConfig);
    }

    private void deleteRequestDetails(CreateApprovalRequestByCategoryCommand.Output output,
                                      Input input,
                                      RevenueConfigData revenueConfig) {

        this.createFieldValueTextFieldDetail(
            output, REVENUE_CONFIG_ID_FIELD_KEY,
            "Revenue Config ID",
            this.toNullableString(revenueConfig.revenueConfigId().getEntityId()), 0);

        this.createFieldValueTextFieldDetail(
            output, TAX_CODE_ID_FIELD_KEY, "Tax Code ID", revenueConfig.taxCodeId(), 1);
        this.createFieldValueTextFieldDetail(
            output, TAX_CODE_DESCRIPTION_FIELD_KEY,
            "Tax Code ID (Description)", revenueConfig.taxCodeDescription(), 2);

        this.createChangedTextFieldDetail(
            output, STATUS_FIELD_KEY, "Status", RevenueConfigStatus.ACTIVE.name(),
            RevenueConfigStatus.INACTIVE.name(), 3);
    }

    private void createFieldValueTextFieldDetail(CreateApprovalRequestByCategoryCommand.Output output,
                                                 String fieldKey,
                                                 String fieldLabel,
                                                 String fieldValue,
                                                 Integer displayOrder) {

        this.createApprovalRequestFieldDetailCommand.execute(
            new CreateApprovalRequestFieldDetailCommand.Input(
                output.approvalRequestId(), fieldKey, fieldLabel, fieldValue, null, null, "TEXT",
                displayOrder, ApprovalTabCode.REVENUE.name()));
    }

    private void createChangedOrFieldValueTextFieldDetail(CreateApprovalRequestByCategoryCommand.Output output,
                                                          String fieldKey,
                                                          String fieldLabel,
                                                          String beforeValue,
                                                          String afterValue,
                                                          Integer displayOrder) {

        if (Objects.equals(beforeValue, afterValue)) {
            this.createFieldValueTextFieldDetail(
                output, fieldKey, fieldLabel, afterValue, displayOrder);
            return;
        }

        this.createApprovalRequestFieldDetailCommand.execute(
            new CreateApprovalRequestFieldDetailCommand.Input(
                output.approvalRequestId(), fieldKey, fieldLabel, afterValue, beforeValue,
                afterValue, "TEXT", displayOrder, ApprovalTabCode.REVENUE.name()));
    }

    private void createSnapshotTextFieldDetail(CreateApprovalRequestByCategoryCommand.Output output,
                                               String fieldKey,
                                               String fieldLabel,
                                               String beforeValue,
                                               String afterValue,
                                               Integer displayOrder) {

        this.createApprovalRequestFieldDetailCommand.execute(
            new CreateApprovalRequestFieldDetailCommand.Input(
                output.approvalRequestId(), fieldKey, fieldLabel, afterValue, beforeValue,
                afterValue, "TEXT", displayOrder, ApprovalTabCode.REVENUE.name()));
    }

    private void createTextFieldDetail(CreateApprovalRequestByCategoryCommand.Output output,
                                       String fieldKey,
                                       String fieldLabel,
                                       String afterValue,
                                       Integer displayOrder) {

        this.createApprovalRequestFieldDetailCommand.execute(
            new CreateApprovalRequestFieldDetailCommand.Input(
                output.approvalRequestId(), fieldKey, fieldLabel, null, null, afterValue, "TEXT",
                displayOrder, ApprovalTabCode.REVENUE.name()));
    }

    private void createChangedTextFieldDetail(CreateApprovalRequestByCategoryCommand.Output output,
                                              String fieldKey,
                                              String fieldLabel,
                                              String beforeValue,
                                              String afterValue,
                                              Integer displayOrder) {

        if (Objects.equals(beforeValue, afterValue)) {
            return;
        }

        this.createApprovalRequestFieldDetailCommand.execute(
            new CreateApprovalRequestFieldDetailCommand.Input(
                output.approvalRequestId(), fieldKey, fieldLabel, null, beforeValue, afterValue,
                "TEXT", displayOrder, ApprovalTabCode.REVENUE.name()));
    }

    private void createRevenuePartyFieldDetail(CreateApprovalRequestByCategoryCommand.Output output,
                                               String fieldKey,
                                               String fieldLabel,
                                               String partyCode,
                                               Integer displayOrder) {

        if (partyCode == null || partyCode.isBlank()) {
            return;
        }

        this.createApprovalRequestFieldDetailCommand.execute(
            new CreateApprovalRequestFieldDetailCommand.Input(
                output.approvalRequestId(), fieldKey, fieldLabel, partyCode, null,
                this.revenuePartyName(partyCode), "TEXT", displayOrder,
                ApprovalTabCode.REVENUE.name()));
    }

    private void createRevenuePartySnapshotFieldDetail(CreateApprovalRequestByCategoryCommand.Output output,
                                                       String fieldKey,
                                                       String fieldLabel,
                                                       String beforePartyCode,
                                                       String afterPartyCode,
                                                       Integer displayOrder) {

        if (afterPartyCode == null || afterPartyCode.isBlank()) {
            return;
        }

        this.createApprovalRequestFieldDetailCommand.execute(
            new CreateApprovalRequestFieldDetailCommand.Input(
                output.approvalRequestId(), fieldKey, fieldLabel, afterPartyCode,
                this.revenuePartyName(beforePartyCode), this.revenuePartyName(afterPartyCode),
                "TEXT", displayOrder, ApprovalTabCode.REVENUE.name()));
    }

    private void createJsonFieldDetail(CreateApprovalRequestByCategoryCommand.Output output,
                                       String fieldKey,
                                       String fieldLabel,
                                       String beforeValue,
                                       String afterValue,
                                       Integer displayOrder) {

        this.createApprovalRequestFieldDetailCommand.execute(
            new CreateApprovalRequestFieldDetailCommand.Input(
                output.approvalRequestId(), fieldKey, fieldLabel, null, beforeValue, afterValue,
                "JSON", displayOrder, ApprovalTabCode.REVENUE.name()));
    }

    private void createChangedJsonFieldDetail(CreateApprovalRequestByCategoryCommand.Output output,
                                              Input input,
                                              RevenueConfigData revenueConfig)
        throws JsonProcessingException {

        Map<String, Object> beforePercentages = this.toPercentageJson(
            input.percentages(), revenueConfig);
        Map<String, Object> afterPercentages = this.toPercentageJson(input.percentages());

        this.createJsonFieldDetail(
            output, PERCENTAGES_FIELD_KEY, PERCENTAGES_FIELD_LABEL,
            this.objectMapper.writeValueAsString(beforePercentages),
            this.objectMapper.writeValueAsString(afterPercentages), 9);
    }

    private RevenueActionType toRequestedAction(String value) {

        return RevenueActionType.valueOf(this.toFieldKey(value).toUpperCase(Locale.ROOT));
    }

    private Map<String, Object> toPercentageJson(Map<String, BigDecimal> percentages) {

        var percentageJson = new LinkedHashMap<String, Object>();
        percentages.forEach((key, value) -> percentageJson.put(key, this.toJsonNumber(value)));
        return percentageJson;
    }

    private Map<String, Object> toPercentageJson(Map<String, BigDecimal> requestedPercentages,
                                                 RevenueConfigData revenueConfig) {

        var percentageJson = new LinkedHashMap<String, Object>();
        List<BigDecimal> existingPercentages = List.of(
            revenueConfig.golPercentage(),
            revenueConfig.ministryPercentage(), revenueConfig.thirdPartyPercentage(),
            revenueConfig.sendingDfspPercentage());
        int index = 0;
        for (String key : requestedPercentages.keySet()) {
            BigDecimal existingPercentage =
                index < existingPercentages.size() ? existingPercentages.get(index) :
                    BigDecimal.ZERO;
            percentageJson.put(key, this.toJsonNumber(existingPercentage));
            index++;
        }
        return percentageJson;
    }

    private List<BigDecimal> percentageValues(Map<String, BigDecimal> percentages) {

        var percentageValues = new ArrayList<>(percentages.values());
        while (percentageValues.size() < 4) {
            percentageValues.add(BigDecimal.ZERO);
        }
        return percentageValues;
    }

    private Object toJsonNumber(BigDecimal value) {

        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() <= 0) {
            return normalized.toBigIntegerExact();
        }
        return new BigDecimal(normalized.toPlainString());
    }

    private Instant toNullableInstant(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.matches("-?\\d+") ? Instant.ofEpochSecond(Long.parseLong(value)) :
                   Instant.parse(value);
    }

    private String toNullableString(Long value) {

        return value == null ? null : String.valueOf(value);
    }

    private String formatEffectiveDateDisplay(Instant effectiveDate,
                                              String effectiveTimezone) {

        if (effectiveDate == null) {
            return null;
        }

        return EFFECTIVE_DATE_DISPLAY_FORMAT.format(
            effectiveDate.atZone(TimeZoneUtil.zoneId(effectiveTimezone)));
    }

    private String revenuePartyName(String partyCode) {

        if (partyCode == null || partyCode.isBlank()) {
            return null;
        }

        return this.revenuePartyQuery
                   .get(partyCode)
                   .map(RevenuePartyData::partyName)
                   .orElse(partyCode);
    }

    private String toNullableString(Instant value) {

        return value == null ? null : value.toString();
    }

    private String toFieldKey(String value) {

        return value
                   .toLowerCase(Locale.ROOT)
                   .replaceAll("[^a-z0-9]+", "_")
                   .replaceAll("^_+|_+$", "");
    }

}
