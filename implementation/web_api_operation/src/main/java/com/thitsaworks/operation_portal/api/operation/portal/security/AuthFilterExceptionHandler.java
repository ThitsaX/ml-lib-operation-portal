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
package com.thitsaworks.operation_portal.api.operation.portal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.api.operation.portal.security.exception.ApiSecurityException;
import com.thitsaworks.operation_portal.component.misc.spring.SpringContext;
import com.thitsaworks.operation_portal.component.misc.spring.SpringRestServiceExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthFilterExceptionHandler extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthFilterExceptionHandler.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {

        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);

        try {

            LOG.info("inside Exception Handler");
            filterChain.doFilter(request, response);
            LOG.info("finish : inside Exception Handler");

        } catch (ApiSecurityException e) {

            LOG.error("Error :", e);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            SpringRestServiceExceptionHandler.SpringRestErrorResponseBuilder errorResponseBuilder =
                    SpringContext.getBean(SpringRestServiceExceptionHandler.SpringRestErrorResponseBuilder.class);

            response.getWriter().write(objectMapper.writeValueAsString(errorResponseBuilder.build(e)));

        } catch (Exception e) {

            LOG.error("Error : ", e);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            SpringRestServiceExceptionHandler.SpringRestErrorResponseBuilder errorResponseBuilder =
                    SpringContext.getBean(SpringRestServiceExceptionHandler.SpringRestErrorResponseBuilder.class);

            response.getWriter().write(objectMapper.writeValueAsString(errorResponseBuilder.build(e)));

        }

    }

}