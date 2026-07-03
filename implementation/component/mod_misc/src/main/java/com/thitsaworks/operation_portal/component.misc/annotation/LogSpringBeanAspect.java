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
package com.thitsaworks.operation_portal.component.misc.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.misc.util.MaskPassword;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogSpringBeanAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogSpringBeanAspect.class);

    private final ObjectMapper objectMapper;

    private static final String POINTCUT = "(" +
                                               "@within(org.springframework.stereotype.Service) || " +
                                               "@within(org.springframework.stereotype.Component) || " +
                                               "execution(* com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase+.execute(..)) || " +
                                               "execution(* com.thitsaworks.operation_portal.usecase.OperationPortalUseCase+.execute(..)) " +
                                               ") && (" +
                                               "execution(* com.thitsaworks.operation_portal..*.execute(..)) || " +
                                               "execution(* com.thitsaworks.operation_portal..*.onExecute(..))" +
                                               ") && (" +
                                               "!@within(com.thitsaworks.operation_portal.component.misc.logging.NoLogging) && " +
                                               "!@annotation(com.thitsaworks.operation_portal.component.misc.logging.NoLogging)" +
                                               ")";

    public LogSpringBeanAspect(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    @Pointcut(POINTCUT)
    public void loggingPointcut() { }

    @Around("loggingPointcut()")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature()
                                     .toShortString();
        Object[] args = joinPoint.getArgs();

        String safeArgs = "[]";
        if (args != null && args.length > 0) {
            try {

                String rawArgs = this.objectMapper.writeValueAsString(args);
                safeArgs = MaskPassword.maskPassword(this.objectMapper, rawArgs);

            } catch (Exception e) {

                safeArgs = "[Arguments could not be serialized]";
            }
        }

        LOGGER.info("Entering method: {} with arguments: {}", methodName, safeArgs);

        Object result;

        try {

            result = joinPoint.proceed();

        } catch (Throwable throwable) {

            LOGGER.error("Exception in method: {} with message: {}", methodName, throwable.getMessage(),
                         throwable);

            throw throwable;
        }

        String safeResult = "{}";
        if (result != null) {
            try {

                String rawResult = this.objectMapper.writeValueAsString(result);
                safeResult = MaskPassword.maskPassword(this.objectMapper, rawResult);

            } catch (Exception e) {

                safeResult = "[Result could not be serialized]";
            }
        }
        LOGGER.info("Exiting method: {} with result: {}", methodName, safeResult);

        return result;
    }

}
