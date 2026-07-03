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
package com.thitsaworks.operation_portal.core.iam.data;

import com.thitsaworks.operation_portal.component.common.identifier.ActionId;
import com.thitsaworks.operation_portal.component.common.type.ActionCode;
import com.thitsaworks.operation_portal.core.iam.model.Action;

import java.io.Serializable;
import java.util.Objects;

public record ActionData(ActionId actionId,
                         ActionCode actionCode,
                         String scope,
                         String category,
                         boolean isMandatory,
                         String description) implements Serializable {

    public ActionData(Action action) {

        this(
            action.getActionId(), action.getActionCode(), action.getScope(), action.getCategory(),
            action.getIsMandatory(), action.getDescription());
    }

    @Override
    public boolean equals(Object o) {

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActionData that = (ActionData) o;
        return Objects.equals(actionId, that.actionId);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(actionId);
    }

}
