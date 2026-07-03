package com.thitsaworks.operation_portal.api.operation.portal.controller.coreServices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetParticipantListByDirectIndirect;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetParticipantListByDirectIndirectController {

    private static final Logger LOG = LoggerFactory.getLogger(GetParticipantListByDirectIndirectController.class);

    private final GetParticipantListByDirectIndirect getParticipantListByDirectIndirect;

    private final ObjectMapper objectMapper;

    @GetMapping(value = "/secured/getParticipantListByDirectIndirect")
    public ResponseEntity<Response> execute() throws DomainException, JsonProcessingException {

        var output = this.getParticipantListByDirectIndirect.execute(new GetParticipantListByDirectIndirect.Input());

        List<Response.ParticipantInfo> participantInfoList = new ArrayList<>();

        for (var participantInfo : output.participantInfoList()) {
            participantInfoList.add(new Response.ParticipantInfo(participantInfo.participantId()
                                                                                .getId()
                                                                                .toString(),
                                                                 participantInfo.participantName(),
                                                                 participantInfo.participantDescription(),
                                                                 participantInfo.logoFileType(),
                                                                 participantInfo.logo() == null ? null :
                                                                     Base64.getEncoder()
                                                                           .encodeToString(
                                                                               participantInfo.logo())));
        }

        var response = new Response(participantInfoList);

        LOG.info("Get Participant List By Direct Indirect Response : [{}]",
                 this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("participantInfoList") List<ParticipantInfo> participantInfoList)
        implements Serializable {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ParticipantInfo(@JsonProperty("participantId") String participantId,
                                      @JsonProperty("participantName") String participantName,
                                      @JsonProperty("participantDescription") String participantDescription,
                                      @JsonProperty("logoFileType") String logoFileType,
                                      @JsonProperty("logo") String logoBase64) implements Serializable { }

    }

}
