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
package com.thitsaworks.operation_portal.component.misc;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.thitsaworks.operation_portal.component.misc.spring.SpringContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

@ComponentScan("com.thitsaworks.operation_portal.component.misc")
public class MiscConfiguration {

    @InitBinder
    public void initBinder(WebDataBinder binder) {

        binder.registerCustomEditor(String.class, new java.beans.PropertyEditorSupport() {

            @Override
            public void setAsText(String text) {

                setValue(text == null ? null : text.trim());
            }
        });
    }

    @Bean
    public ObjectMapper objectMapper() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        objectMapper.getFactory()
                    .setStreamReadConstraints(
                            StreamReadConstraints.builder()
                                                 .maxStringLength(50_000_000)
                                                 .maxNestingDepth(500)
                                                 .build());

        return objectMapper;
    }

    @Bean
    public SpringContext springContext() {

        return new SpringContext();
    }

}
