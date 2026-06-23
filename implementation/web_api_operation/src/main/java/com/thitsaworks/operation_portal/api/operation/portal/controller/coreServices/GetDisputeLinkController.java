package com.thitsaworks.operation_portal.api.operation.portal.controller.coreServices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.operation_portal.api.operation.portal.OperationPortalApiConfiguration;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GetDisputeLinkController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetDisputeLinkController.class);

    private final OperationPortalApiConfiguration.SupportCenterSettings supportCenterSettings;

    @GetMapping(value = "/secured/getDisputeLink")
    public ResponseEntity<Response> execute() {

        return ResponseEntity.ok(new Response(supportCenterSettings.DISPUTE_URL()));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("dispute") String ticketUrl) { }

}

