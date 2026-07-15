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
package com.thitsaworks.operation_portal.api.operation.portal.controller.coreServices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetUserListByParticipant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetUserListByParticipantController {

    private static final Logger LOG = LoggerFactory.getLogger(GetUserListByParticipantController.class);

    private final GetUserListByParticipant getUserListByParticipant;

    private final ObjectMapper objectMapper;

    @GetMapping(value = "/secured/getUserListByParticipant")
    public ResponseEntity<Response> execute()
        throws DomainException, JsonProcessingException {

        GetUserListByParticipant.Output output = this.getUserListByParticipant.execute(
            new GetUserListByParticipant.Input());

        List<Response.UserInfo> userInfoList = new ArrayList<>();

        for (var user : output.userInfoList()) {
            userInfoList.add(new Response.UserInfo(user.userId()
                                                       .getId()
                                                       .toString(),
                                                   user.name(),
                                                   user.email()
                                                       .getValue(),
                                                   user.firstName(),
                                                   user.lastName(),
                                                   user.jobTitle(),
                                                   user.allowNotification(),
                                                   user.roleList(),
                                                   user.participantId()
                                                       .getEntityId()
                                                       .toString(),
                                                   user.participantName(),
                                                   user.participantDescription(),
                                                   user.status(),
                                                   user.createdDate()
                                                       .getEpochSecond()));
        }

        userInfoList.sort(Comparator.comparing(Response.UserInfo::email));

        var response = new GetUserListByParticipantController.Response(userInfoList);

        LOG.info("Get User List By Participant Response : [{}]", this.objectMapper.writeValueAsString(response));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(@JsonProperty("userInfoList") List<UserInfo> userInfoList) implements Serializable {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record UserInfo(@JsonProperty("userId") String userId,
                               @JsonProperty("name") String name,
                               @JsonProperty("email") String email,
                               @JsonProperty("firstName") String firstName,
                               @JsonProperty("lastName") String lastName,
                               @JsonProperty("jobTitle") String jobTitle,
                               @JsonProperty("allowNotification") boolean allowNotification,
                               @JsonProperty("roleList") List<String> roleList,
                               @JsonProperty("participantId") String participantId,
                               @JsonProperty("participantName") String participantName,
                               @JsonProperty("participantDescription") String participantDescription,
                               @JsonProperty("status") String status,
                               @JsonProperty("createdDate") long createdDate) implements Serializable { }

    }

}
