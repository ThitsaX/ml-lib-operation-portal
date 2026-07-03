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
package com.thitsaworks.operation_portal.component.common.type;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ActionCode implements Serializable {

    @EqualsAndHashCode.Include
    private String value;

    public ActionCode(String value) {

        this.value = value;
    }

    @Converter
    public static class JpaConverter implements AttributeConverter<ActionCode, String> {

        @Override
        public String convertToDatabaseColumn(ActionCode attribute) {

            return attribute.value;
        }

        @Override
        public ActionCode convertToEntityAttribute(String dbData) {

            return new ActionCode(dbData);
        }

    }

    @Override
    public String toString() {

        return this.value;
    }

}