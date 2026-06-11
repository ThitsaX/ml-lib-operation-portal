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
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateFeeAmountSwiftReportCommand;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GenerateFeeAmountSwiftReport;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.ConnectException;

@Service
@ActionMetadata(category = ActionCategory.REPORTING)
public class GenerateFeeAmountSwiftReportHandler
    extends OperationPortalAuditableUseCase<GenerateFeeAmountSwiftReport.Input, GenerateFeeAmountSwiftReport.Output>
    implements GenerateFeeAmountSwiftReport {

    private static final Logger LOG = LoggerFactory.getLogger(
        GenerateFeeAmountSwiftReportHandler.class);

    private final GenerateFeeAmountSwiftReportCommand GenerateFeeAmountSwiftReportCommand;

    public GenerateFeeAmountSwiftReportHandler(CreateInputAuditCommand createInputAuditCommand,
                                               CreateOutputAuditCommand createOutputAuditCommand,
                                               CreateExceptionAuditCommand createExceptionAuditCommand,
                                               ObjectMapper objectMapper,
                                               PrincipalCache principalCache,
                                               ActionAuthorizationManager actionAuthorizationManager,
                                               GenerateFeeAmountSwiftReportCommand GenerateFeeAmountSwiftReportCommand) {

        super(
            createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
            objectMapper, principalCache, actionAuthorizationManager);

        this.GenerateFeeAmountSwiftReportCommand = GenerateFeeAmountSwiftReportCommand;
    }

    @Override
    protected Output onExecute(Input input)
        throws DomainException, ConnectException, JsonProcessingException {

        GenerateFeeAmountSwiftReportCommand.Output output = this.GenerateFeeAmountSwiftReportCommand.execute(
            new GenerateFeeAmountSwiftReportCommand.Input(
                input.settlementId(),
                input.currencyId(),
                input.timezone()));

        return new GenerateFeeAmountSwiftReport.Output(output.feeSettlementRptByte());
    }

}
