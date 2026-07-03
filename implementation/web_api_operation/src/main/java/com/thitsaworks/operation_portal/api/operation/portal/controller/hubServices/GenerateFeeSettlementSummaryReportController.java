package com.thitsaworks.operation_portal.api.operation.portal.controller.hubServices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.api.operation.portal.security.UserContext;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.TimeZoneOffsetFormater;
import com.thitsaworks.operation_portal.usecase.operation_portal.GenerateFeeSettlementSummaryReport;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
@RequiredArgsConstructor
public class GenerateFeeSettlementSummaryReportController {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateFeeSettlementSummaryReportController.class);

    private final GenerateFeeSettlementSummaryReport generateFeeSettlementSummaryReport;

    private final ObjectMapper objectMapper;

    @PostMapping("/secured/generateFeeSettlementSummaryReport")
    public ResponseEntity<Response> execute(@RequestParam("settlementId") String settlementId,
                                            @RequestParam("dfspId") String dfspId,
                                            @RequestParam("timezoneOffset") String timezoneOffset)
        throws DomainException, JsonProcessingException {

        LOG.info(
            "Generate Fee Settlement Summary Report : settlementId = [{}], dfspId = [{}], timezoneOffset = [{}]",
            settlementId, dfspId, timezoneOffset);

        String timezone = TimeZoneOffsetFormater.normalizeOffsetFormat(timezoneOffset);

        UserContext userContext = (UserContext) SecurityContextHolder
                                                    .getContext()
                                                    .getAuthentication()
                                                    .getDetails();

        GenerateFeeSettlementSummaryReport.Output output = this.generateFeeSettlementSummaryReport.execute(
            new GenerateFeeSettlementSummaryReport.Input(
                settlementId, dfspId, timezone,
                userContext.userId().getId()));

        var response = new Response(
            output.requestId().getEntityId().toString(),
            output.status().name(),
            output.fileUrl(),
            output.paramsSignature());

        LOG.info("Generate Fee Settlement Summary Report Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("requestId") String requestId,
                           @JsonProperty("status") String status,
                           @JsonProperty("fileUrl") String fileUrl,
                           @JsonProperty("paramsSignature") String paramsSignature)
        implements Serializable { }
}
