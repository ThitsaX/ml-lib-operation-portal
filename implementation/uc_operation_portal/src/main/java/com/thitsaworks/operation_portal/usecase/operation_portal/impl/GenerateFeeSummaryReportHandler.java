package com.thitsaworks.operation_portal.usecase.operation_portal.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateFeeSummaryReportCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GenerateFeeSummaryReport;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.ConnectException;

@Service
@ActionMetadata(category = ActionCategory.REPORTING)
public class GenerateFeeSummaryReportHandler
    extends OperationPortalAuditableUseCase<GenerateFeeSummaryReport.Input, GenerateFeeSummaryReport.Output>
    implements GenerateFeeSummaryReport {

    private static final Logger LOG = LoggerFactory.getLogger(
        GenerateFeeSummaryReportHandler.class);

    private final GenerateFeeSummaryReportCommand generateFeeSummaryReportCommand;

    public GenerateFeeSummaryReportHandler(CreateInputAuditCommand createInputAuditCommand,
                                           CreateOutputAuditCommand createOutputAuditCommand,
                                           CreateExceptionAuditCommand createExceptionAuditCommand,
                                           ObjectMapper objectMapper,
                                           PrincipalCache principalCache,
                                           ActionAuthorizationManager actionAuthorizationManager,
                                           GenerateFeeSummaryReportCommand generateFeeSummaryReportCommand) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.generateFeeSummaryReportCommand = generateFeeSummaryReportCommand;
    }

    @Override
    protected Output onExecute(Input input)
        throws DomainException, ConnectException, JsonProcessingException {

        GenerateFeeSummaryReportCommand.Output output = this.generateFeeSummaryReportCommand.execute(
            new GenerateFeeSummaryReportCommand.Input(
                input.settlementId(),
                input.currencyId(),
                input.timezone()));

        return new GenerateFeeSummaryReport.Output(output.feeSummaryRptByte());
    }

}
