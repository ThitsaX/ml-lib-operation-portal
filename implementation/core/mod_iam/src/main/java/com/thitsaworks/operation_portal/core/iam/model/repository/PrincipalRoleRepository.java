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
package com.thitsaworks.operation_portal.core.iam.model.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.thitsaworks.operation_portal.component.common.identifier.PrincipalId;
import com.thitsaworks.operation_portal.component.common.identifier.PrincipalRoleId;
import com.thitsaworks.operation_portal.core.iam.model.PrincipalRole;
import com.thitsaworks.operation_portal.core.iam.model.QPrincipalRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface PrincipalRoleRepository extends JpaRepository<PrincipalRole, PrincipalRoleId>,
                                                 QuerydslPredicateExecutor<PrincipalRole> {

    class Filters {

        public static BooleanExpression byPrincipalId(PrincipalId principalId) {
            return  QPrincipalRole.principalRole.principal.principalId.eq(principalId);
        }

    }

}
