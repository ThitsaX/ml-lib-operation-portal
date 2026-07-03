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

import com.thitsaworks.operation_portal.component.common.identifier.AccessKey;
import com.thitsaworks.operation_portal.component.common.identifier.PrincipalId;
import com.thitsaworks.operation_portal.component.common.identifier.RealmId;
import com.thitsaworks.operation_portal.component.common.type.PrincipalStatus;
import com.thitsaworks.operation_portal.core.iam.model.Principal;

import java.io.Serializable;
import java.util.Objects;

public record PrincipalData(PrincipalId principalId,
                            AccessKey accessKey,
                            String secretKey,
                            RealmId realmId,
                            PrincipalStatus principalStatus) implements Serializable {

    public PrincipalData(Principal principal) {

        this(principal.getPrincipalId(),
             principal.getAccessKey(),
             principal.getSecretKey(),
             principal.getRealmId(),
             principal.getPrincipalStatus());
    }

    @Override
    public boolean equals(Object o) {

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrincipalData that = (PrincipalData) o;
        return Objects.equals(principalId, that.principalId);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(principalId);
    }


}
