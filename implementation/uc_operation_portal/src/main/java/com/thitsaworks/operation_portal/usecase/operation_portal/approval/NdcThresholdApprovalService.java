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
package com.thitsaworks.operation_portal.usecase.operation_portal.approval;

import com.thitsaworks.operation_portal.component.common.identifier.ApprovalRequestId;
import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.common.identifier.ThresholdConfigurationId;
import com.thitsaworks.operation_portal.component.common.identifier.ThresholdDetailId;
import com.thitsaworks.operation_portal.component.common.identifier.UserId;
import com.thitsaworks.operation_portal.component.common.type.ApprovalActionType;
import com.thitsaworks.operation_portal.component.common.type.ApprovalTabCode;
import com.thitsaworks.operation_portal.component.common.type.NdcThresholdApprovalOperation;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.exception.ErrorMessage;
import com.thitsaworks.operation_portal.component.misc.exception.InputException;
import com.thitsaworks.operation_portal.core.approval.command.ModifyApprovalActionCommand;
import com.thitsaworks.operation_portal.core.approval.model.ApprovalRequest;
import com.thitsaworks.operation_portal.core.approval.model.ApprovalRequestFieldDetail;
import com.thitsaworks.operation_portal.core.approval.model.repository.ApprovalRequestFieldDetailRepository;
import com.thitsaworks.operation_portal.core.approval.model.repository.ApprovalRequestRepository;
import com.thitsaworks.operation_portal.core.notification.command.CreateThresholdDetailCommand;
import com.thitsaworks.operation_portal.core.notification.command.ModifyThresholdDetailCommand;
import com.thitsaworks.operation_portal.core.notification.data.ThresholdConfigurationData;
import com.thitsaworks.operation_portal.core.notification.data.ThresholdDetailData;
import com.thitsaworks.operation_portal.core.notification.model.ThresholdDetail;
import com.thitsaworks.operation_portal.core.notification.query.ThresholdConfigurationQuery;
import com.thitsaworks.operation_portal.core.notification.query.ThresholdDetailQuery;
import com.thitsaworks.operation_portal.core.participant.data.ParticipantData;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantQuery;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetNdcThresholdApprovalList;
import com.thitsaworks.operation_portal.usecase.operation_portal.validation.ThresholdDetailCurrencyValidator;
import com.thitsaworks.operation_portal.usecase.util.UserPermissionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NdcThresholdApprovalService {

    public static final String REQUEST_CATEGORY = "NDC_ALERT_THRESHOLD";

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private static final String FIELD_THRESHOLD_DETAIL_ID = "thresholdDetailId";

    private static final String FIELD_THRESHOLD_CONFIGURATION_ID = "thresholdConfigurationId";

    private static final String FIELD_VISUAL_CONFIG = "visualConfig";

    private static final String FIELD_NOTIFICATION_CONFIG = "notificationConfig";

    private static final String LEGACY_FIELD_NDC_CONFIG = "ndcConfig";

    private static final String FIELD_STATUS = "status";

    private final ApprovalRequestRepository approvalRequestRepository;

    private final ApprovalRequestFieldDetailRepository fieldDetailRepository;

    private final ModifyApprovalActionCommand modifyApprovalActionCommand;

    private final ThresholdConfigurationQuery thresholdConfigurationQuery;

    private final ThresholdDetailQuery thresholdDetailQuery;

    private final ThresholdDetailCurrencyValidator currencyValidator;

    private final CreateThresholdDetailCommand createThresholdDetailCommand;

    private final ModifyThresholdDetailCommand modifyThresholdDetailCommand;

    private final ParticipantQuery participantQuery;

    private final UserPermissionManager userPermissionManager;

    public ApprovalRequestId submit(NdcThresholdApprovalOperation operation,
                                    String requestedParticipantName,
                                    Long requestedThresholdDetailId,
                                    String requestedCurrency,
                                    BigDecimal requestedVisualConfig,
                                    BigDecimal requestedNotificationConfig,
                                    UserId requestedBy) throws DomainException {

        requireAuthenticatedActor(requestedBy);

        String participantName = authorizeParticipant(requestedParticipantName);
        ThresholdConfigurationData configuration = getDfspConfiguration(participantName);
        PreparedChange change = prepareChange(
            operation,
            configuration,
            requestedThresholdDetailId,
            requestedCurrency,
            requestedVisualConfig,
            requestedNotificationConfig);

        if (this.approvalRequestRepository
            .existsByRequestCategoryAndParticipantNameAndParticipantCurrencyAndAction(
                REQUEST_CATEGORY,
                participantName,
                change.currency(),
                ApprovalActionType.PENDING)) {

            throw error(
                "NDC_THRESHOLD_APPROVAL_ALREADY_PENDING",
                "A pending NDC threshold request already exists for this DFSP and currency.");
        }

        ApprovalRequest request = new ApprovalRequest(
            operation.requestedAction(),
            requestedBy,
            REQUEST_CATEGORY);

        request.participantName(participantName);
        request.participantCurrency(change.currency());
        this.approvalRequestRepository.save(request);

        this.fieldDetailRepository.saveAll(createFieldDetails(request.getApprovalRequestId(), change));

        return request.getApprovalRequestId();
    }

    public List<GetNdcThresholdApprovalList.Output.Approval> getApprovals(
        ApprovalActionType status) throws DomainException {

        ApprovalActionType requestedStatus =
            status == null ? ApprovalActionType.PENDING : status;

        String participantName = currentDfspParticipantName();
        List<ApprovalRequest> requests = participantName == null
                                           ? this.approvalRequestRepository
                                                 .findAllByRequestCategoryAndActionOrderByRequestedDtmDesc(
                                                     REQUEST_CATEGORY,
                                                     requestedStatus)
                                           : this.approvalRequestRepository
                                                 .findAllByRequestCategoryAndParticipantNameAndActionOrderByRequestedDtmDesc(
                                                     REQUEST_CATEGORY,
                                                     participantName,
                                                     requestedStatus);

        return requests.stream()
                       .map(this::toApproval)
                       .toList();
    }

    public ApprovalRequestId decide(ApprovalRequestId approvalRequestId,
                                    ApprovalActionType action,
                                    UserId respondedBy) throws DomainException {

        requireAuthenticatedActor(respondedBy);
        validateDecision(action);

        ApprovalRequest request = this.approvalRequestRepository
            .findPendingByIdAndCategoryForUpdate(
                approvalRequestId,
                REQUEST_CATEGORY,
                ApprovalActionType.PENDING)
            .orElseThrow(() -> error(
                "NDC_THRESHOLD_APPROVAL_NOT_FOUND",
                "Pending NDC threshold approval request was not found."));

        authorizeParticipant(request.getParticipantName());

        if (request.getRequestedBy().equals(respondedBy)) {
            throw error(
                "NDC_THRESHOLD_SELF_APPROVAL_NOT_ALLOWED",
                "The request maker cannot approve or reject the same request.");
        }

        if (action == ApprovalActionType.APPROVED) {
            applyApprovedChange(request, respondedBy);
        }

        this.modifyApprovalActionCommand.execute(
            new ModifyApprovalActionCommand.Input(
                request.getApprovalRequestId(),
                action,
                respondedBy));

        return request.getApprovalRequestId();
    }

    private PreparedChange prepareChange(NdcThresholdApprovalOperation operation,
                                         ThresholdConfigurationData configuration,
                                         Long requestedThresholdDetailId,
                                         String requestedCurrency,
                                         BigDecimal requestedVisualConfig,
                                         BigDecimal requestedNotificationConfig)
        throws DomainException {

        Objects.requireNonNull(operation, "operation is required");

        if (operation == NdcThresholdApprovalOperation.CREATE_NDC_ALERT_THRESHOLD) {
            String currency = this.currencyValidator.validateForConfiguration(
                configuration.thresholdConfigurationId(),
                requestedCurrency);
            validatePercentages(requestedVisualConfig, requestedNotificationConfig);

            boolean exists = this.thresholdDetailQuery
                .getAll(configuration.thresholdConfigurationId(), null)
                .stream()
                .anyMatch(detail -> detail.currency().equalsIgnoreCase(currency));

            if (exists) {
                throw error(
                    "NDC_THRESHOLD_DETAIL_ALREADY_EXISTS",
                    "A threshold detail already exists for this DFSP and currency.");
            }

            return new PreparedChange(
                configuration.thresholdConfigurationId(),
                null,
                currency,
                null,
                requestedVisualConfig,
                null,
                requestedNotificationConfig,
                null,
                true);
        }

        if (requestedThresholdDetailId == null) {
            throw error(
                "NDC_THRESHOLD_DETAIL_ID_REQUIRED",
                "thresholdDetailId is required for update and delete operations.");
        }

        ThresholdDetailData current = getOwnedDetail(
            configuration,
            new ThresholdDetailId(requestedThresholdDetailId));

        if (operation == NdcThresholdApprovalOperation.ENABLE_NDC_ALERT
            || operation == NdcThresholdApprovalOperation.DISABLE_NDC_ALERT) {

            boolean requestedStatus = operation == NdcThresholdApprovalOperation.ENABLE_NDC_ALERT;

            if (current.status() == requestedStatus) {
                throw error(
                    "NDC_THRESHOLD_STATUS_UNCHANGED",
                    "The NDC alert is already in the requested status.");
            }

            return new PreparedChange(
                configuration.thresholdConfigurationId(),
                current.thresholdDetailId(),
                current.currency(),
                current.visualConfig(),
                current.visualConfig(),
                current.ndcConfig(),
                current.ndcConfig(),
                current.status(),
                requestedStatus);
        }

        String currency = this.currencyValidator.validateForDetail(
            current.thresholdDetailId(),
            requestedCurrency);

        if (!current.currency().equalsIgnoreCase(currency)) {
            throw error(
                "NDC_THRESHOLD_CURRENCY_CHANGE_NOT_ALLOWED",
                "Currency cannot be changed when updating a threshold detail.");
        }

        boolean visualUpdate = operation == NdcThresholdApprovalOperation.UPDATE_NDC_VISUAL_ALERT
                                || operation == NdcThresholdApprovalOperation.UPDATE_NDC_VISUAL_AND_NOTIFICATION_ALERT;
        boolean notificationUpdate = operation == NdcThresholdApprovalOperation.UPDATE_NDC_NOTIFICATION_ALERT
                                     || operation == NdcThresholdApprovalOperation.UPDATE_NDC_VISUAL_AND_NOTIFICATION_ALERT;

        BigDecimal visualConfig = visualUpdate
                                  ? requiredPercentage(requestedVisualConfig, "visualConfig")
                                  : current.visualConfig();
        BigDecimal notificationConfig = notificationUpdate
                                        ? requiredPercentage(requestedNotificationConfig, "notificationConfig")
                                        : current.ndcConfig();

        if (!visualUpdate && !notificationUpdate) {

            throw error(
                "NDC_THRESHOLD_OPERATION_INVALID",
                "Only visual or notification alert updates are allowed for this operation.");
        }

        if (visualConfig.compareTo(current.visualConfig()) == 0
            && notificationConfig.compareTo(current.ndcConfig()) == 0) {

            throw error(
                "NDC_THRESHOLD_VALUE_UNCHANGED",
                "The requested threshold value did not change.");
        }

        validatePercentages(visualConfig, notificationConfig);

        return new PreparedChange(
            configuration.thresholdConfigurationId(),
            current.thresholdDetailId(),
            currency,
            current.visualConfig(),
            visualConfig,
            current.ndcConfig(),
            notificationConfig,
            current.status(),
            current.status());
    }

    private void applyApprovedChange(ApprovalRequest request,
                                     UserId respondedBy) throws DomainException {

        Map<String, ApprovalRequestFieldDetail> fields = getFields(request.getApprovalRequestId());
        NdcThresholdApprovalOperation operation =
            NdcThresholdApprovalOperation.fromRequestedAction(request.getRequestedAction());

        ThresholdConfigurationData configuration =
            getDfspConfiguration(request.getParticipantName());

        ThresholdConfigurationId storedConfigurationId = new ThresholdConfigurationId(
            parseRequiredLong(after(fields, FIELD_THRESHOLD_CONFIGURATION_ID),
                              FIELD_THRESHOLD_CONFIGURATION_ID));

        if (!configuration.thresholdConfigurationId().equals(storedConfigurationId)) {
            throw error(
                "NDC_THRESHOLD_CONFIGURATION_CHANGED",
                "The DFSP threshold configuration changed while the request was pending.");
        }

        String actor = respondedBy.getId().toString();

        if (operation == NdcThresholdApprovalOperation.CREATE_NDC_ALERT_THRESHOLD) {
            BigDecimal visualConfig = parseDecimal(after(fields, FIELD_VISUAL_CONFIG));
            BigDecimal notificationConfig = parseDecimal(after(fields, FIELD_NOTIFICATION_CONFIG));

            this.currencyValidator.validateForConfiguration(
                storedConfigurationId,
                request.getParticipantCurrency());

            this.createThresholdDetailCommand.execute(
                new CreateThresholdDetailCommand.Input(
                    storedConfigurationId,
                    request.getParticipantCurrency(),
                    visualConfig,
                    notificationConfig,
                    actor));
            return;
        }

        ThresholdDetailId thresholdDetailId = new ThresholdDetailId(
            parseRequiredLong(after(fields, FIELD_THRESHOLD_DETAIL_ID),
                              FIELD_THRESHOLD_DETAIL_ID));

        ThresholdDetailData current = getOwnedDetail(configuration, thresholdDetailId);
        verifyNotStale(current, fields);

        BigDecimal visualConfig = parseDecimal(after(fields, FIELD_VISUAL_CONFIG));
        BigDecimal notificationConfig = parseDecimal(after(fields, FIELD_NOTIFICATION_CONFIG));
        boolean status = Boolean.parseBoolean(after(fields, FIELD_STATUS));

        this.modifyThresholdDetailCommand.execute(
            new ModifyThresholdDetailCommand.Input(
                thresholdDetailId,
                request.getParticipantCurrency(),
                visualConfig,
                notificationConfig,
                status,
                actor));
    }

    private void verifyNotStale(ThresholdDetailData current,
                                Map<String, ApprovalRequestFieldDetail> fields) {

        BigDecimal previousVisual = parseDecimal(before(fields, FIELD_VISUAL_CONFIG));
        BigDecimal previousNotification = parseDecimal(before(fields, FIELD_NOTIFICATION_CONFIG));
        boolean previousStatus = Boolean.parseBoolean(before(fields, FIELD_STATUS));

        if (current.visualConfig().compareTo(previousVisual) != 0
            || current.ndcConfig().compareTo(previousNotification) != 0
            || current.status() != previousStatus) {

            throw error(
                "NDC_THRESHOLD_APPROVAL_STALE",
                "The threshold changed after this approval request was submitted.");
        }
    }

    private List<ApprovalRequestFieldDetail> createFieldDetails(ApprovalRequestId requestId,
                                                                 PreparedChange change) {

        List<ApprovalRequestFieldDetail> details = new ArrayList<>();
        int order = 1;

        details.add(field(
            requestId,
            FIELD_THRESHOLD_CONFIGURATION_ID,
            "Threshold Configuration ID",
            null,
            change.thresholdConfigurationId().getId().toString(),
            "ID",
            order++));
        details.add(field(
            requestId,
            FIELD_THRESHOLD_DETAIL_ID,
            "Threshold Detail ID",
            change.thresholdDetailId() == null ? null : change.thresholdDetailId().getId().toString(),
            change.thresholdDetailId() == null ? null : change.thresholdDetailId().getId().toString(),
            "ID",
            order++));
        details.add(field(
            requestId,
            FIELD_VISUAL_CONFIG,
            "Visual Alert Percentage",
            text(change.previousVisualConfig()),
            text(change.requestedVisualConfig()),
            "PERCENT",
            order++));
        details.add(field(
            requestId,
            FIELD_NOTIFICATION_CONFIG,
            "Notification Alert Percentage",
            text(change.previousNotificationConfig()),
            text(change.requestedNotificationConfig()),
            "PERCENT",
            order++));
        details.add(field(
            requestId,
            FIELD_STATUS,
            "Active Status",
            change.previousStatus() == null ? null : change.previousStatus().toString(),
            change.requestedStatus().toString(),
            "BOOLEAN",
            order));

        return details;
    }

    private ApprovalRequestFieldDetail field(ApprovalRequestId requestId,
                                             String key,
                                             String label,
                                             String beforeValue,
                                             String afterValue,
                                             String valueType,
                                             int order) {

        return new ApprovalRequestFieldDetail(
            requestId,
            key,
            label,
            afterValue,
            beforeValue,
            afterValue,
            valueType,
            order,
            ApprovalTabCode.NDC_ALERT.name());
    }

    private GetNdcThresholdApprovalList.Output.Approval toApproval(ApprovalRequest request) {

        Map<String, ApprovalRequestFieldDetail> fields = getFields(request.getApprovalRequestId());

        return new GetNdcThresholdApprovalList.Output.Approval(
            request.getApprovalRequestId().getId().toString(),
            NdcThresholdApprovalOperation.fromRequestedAction(request.getRequestedAction()).requestedAction(),
            request.getParticipantName(),
            request.getParticipantCurrency(),
            after(fields, FIELD_THRESHOLD_DETAIL_ID),
            nullableDecimal(before(fields, FIELD_VISUAL_CONFIG)),
            nullableDecimal(after(fields, FIELD_VISUAL_CONFIG)),
            nullableDecimal(before(fields, FIELD_NOTIFICATION_CONFIG)),
            nullableDecimal(after(fields, FIELD_NOTIFICATION_CONFIG)),
            request.getRequestedBy().getId().toString(),
            request.getRequestedDtm(),
            request.getRespondedBy() == null ? null : request.getRespondedBy().getId().toString(),
            request.getRespondedDtm(),
            request.getAction().name());
    }

    private Map<String, ApprovalRequestFieldDetail> getFields(ApprovalRequestId requestId) {

        Map<String, ApprovalRequestFieldDetail> fields = new LinkedHashMap<>();

        this.fieldDetailRepository
            .findByApprovalRequestIdAndTabCodeOrderByDisplayOrderAsc(
                requestId,
                ApprovalTabCode.NDC_ALERT.name(),
                false)
            .forEach(detail -> {
                String fieldKey = LEGACY_FIELD_NDC_CONFIG.equals(detail.getFieldKey())
                                  ? FIELD_NOTIFICATION_CONFIG
                                  : detail.getFieldKey();
                fields.put(fieldKey, detail);
            });

        return fields;
    }

    private String authorizeParticipant(String requestedParticipantName)
        throws DomainException {

        ParticipantData requestedParticipant = this.participantQuery
            .get(requireText(requestedParticipantName, "participantName"))
            .orElseThrow(() -> error(
                "NDC_THRESHOLD_DFSP_NOT_FOUND",
                "The requested DFSP participant was not found."));

        String normalizedParticipantName =
            requestedParticipant.participantName().getValue();

        String ownParticipantName = currentDfspParticipantName();

        if (ownParticipantName != null
            && !ownParticipantName.equals(normalizedParticipantName)) {

            throw error(
                "NDC_THRESHOLD_CROSS_DFSP_ACCESS_DENIED",
                "A DFSP user can manage only their own DFSP threshold.");
        }

        return normalizedParticipantName;
    }

    private String currentDfspParticipantName() throws DomainException {

        var currentUser = this.userPermissionManager.getCurrentUser();

        if (!this.userPermissionManager.isDfsp(currentUser.principalId())) {
            return null;
        }

        ParticipantData participant = this.participantQuery.get(
            new ParticipantId(currentUser.realmId().getId()));

        return participant.participantName().getValue();
    }

    private void requireAuthenticatedActor(UserId actor) throws DomainException {

        if (actor == null) {
            throw error("NDC_THRESHOLD_ACTOR_REQUIRED", "Authenticated user is required.");
        }

        var currentUser = this.userPermissionManager.getCurrentUser();

        if (!currentUser.principalId().getId().equals(actor.getId())) {
            throw error(
                "NDC_THRESHOLD_ACTOR_MISMATCH",
                "Authenticated user does not match the request actor.");
        }
    }

    private ThresholdConfigurationData getDfspConfiguration(String participantName) {

        return this.thresholdConfigurationQuery
                   .getDfspConfiguration(participantName)
                   .orElseThrow(() -> error(
                       "NDC_THRESHOLD_DFSP_CONFIGURATION_NOT_FOUND",
                       "DFSP threshold configuration was not found."));
    }

    private ThresholdDetailData getOwnedDetail(ThresholdConfigurationData configuration,
                                               ThresholdDetailId thresholdDetailId) {

        ThresholdDetailData detail = this.thresholdDetailQuery
            .get(thresholdDetailId)
            .orElseThrow(() -> error(
                "NDC_THRESHOLD_DETAIL_NOT_FOUND",
                "Threshold detail was not found."));

        if (!detail.thresholdConfigurationId().equals(
            configuration.thresholdConfigurationId())) {

            throw error(
                "NDC_THRESHOLD_DETAIL_OWNERSHIP_MISMATCH",
                "Threshold detail does not belong to the requested DFSP.");
        }

        return detail;
    }

    private static void validateDecision(ApprovalActionType action) {

        if (action != ApprovalActionType.APPROVED
            && action != ApprovalActionType.REJECTED) {

            throw error(
                "NDC_THRESHOLD_APPROVAL_ACTION_INVALID",
                "Approval action must be APPROVED or REJECTED.");
        }
    }

    private static BigDecimal requiredPercentage(BigDecimal value, String fieldName) {

        if (value == null) {
            throw error(
                "NDC_THRESHOLD_PERCENTAGE_REQUIRED",
                fieldName + " is required.");
        }

        return value;
    }

    private static void validatePercentages(BigDecimal visualConfig,
                                            BigDecimal notificationConfig) {

        if (visualConfig == null || notificationConfig == null) {
            throw error(
                "NDC_THRESHOLD_PERCENTAGE_REQUIRED",
                "Both visualConfig and notificationConfig are required.");
        }

        if (visualConfig.compareTo(BigDecimal.ZERO) < 0
            || visualConfig.compareTo(ONE_HUNDRED) > 0
            || notificationConfig.compareTo(BigDecimal.ZERO) < 0
            || notificationConfig.compareTo(ONE_HUNDRED) > 0) {

            throw error(
                "NDC_THRESHOLD_PERCENTAGE_INVALID",
                "Threshold percentages must be between 0 and 100.");
        }

        if (visualConfig.compareTo(notificationConfig) > 0) {
            throw error(
                "NDC_THRESHOLD_ORDER_INVALID",
                "visualConfig cannot be greater than notificationConfig.");
        }
    }

    private static String after(Map<String, ApprovalRequestFieldDetail> fields,
                                String key) {

        ApprovalRequestFieldDetail detail = fields.get(key);
        return detail == null ? null : detail.getAfterValue();
    }

    private static String before(Map<String, ApprovalRequestFieldDetail> fields,
                                 String key) {

        ApprovalRequestFieldDetail detail = fields.get(key);
        return detail == null ? null : detail.getBeforeValue();
    }

    private static BigDecimal parseDecimal(String value) {

        if (value == null || value.isBlank()) {
            throw error(
                "NDC_THRESHOLD_APPROVAL_PAYLOAD_INVALID",
                "Approval request percentage data is missing.");
        }
        return new BigDecimal(value);
    }

    private static BigDecimal nullableDecimal(String value) {

        return value == null || value.isBlank() ? null : new BigDecimal(value);
    }

    private static long parseRequiredLong(String value, String fieldName) {

        if (value == null || value.isBlank()) {
            throw error(
                "NDC_THRESHOLD_APPROVAL_PAYLOAD_INVALID",
                fieldName + " is missing from the approval request.");
        }
        return Long.parseLong(value);
    }

    private static String text(BigDecimal value) {

        return value == null ? null : value.toPlainString();
    }

    private static String requireText(String value, String name) {

        if (value == null || value.isBlank()) {
            throw error(
                "NDC_THRESHOLD_REQUIRED_FIELD",
                name + " is required.");
        }
        return value.trim();
    }

    private static InputException error(String code, String message) {

        return new InputException(new ErrorMessage(code, message));
    }

    private record PreparedChange(
        ThresholdConfigurationId thresholdConfigurationId,
        ThresholdDetailId thresholdDetailId,
        String currency,
        BigDecimal previousVisualConfig,
        BigDecimal requestedVisualConfig,
        BigDecimal previousNotificationConfig,
        BigDecimal requestedNotificationConfig,
        Boolean previousStatus,
        Boolean requestedStatus
    ) { }
}
