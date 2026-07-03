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
package com.thitsaworks.operation_portal.core.iam.query.impl.jpa;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.thitsaworks.operation_portal.component.common.identifier.RoleId;
import com.thitsaworks.operation_portal.core.iam.data.RoleData;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.iam.model.QRole;
import com.thitsaworks.operation_portal.core.iam.model.Role;
import com.thitsaworks.operation_portal.core.iam.model.repository.RoleRepository;
import com.thitsaworks.operation_portal.core.iam.query.RoleQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleJpaQueryHandler implements RoleQuery {

    private static final Logger LOG = LoggerFactory.getLogger(RoleJpaQueryHandler.class);

    private final QRole role = QRole.role;

    private final RoleRepository roleRepository;

    @Override
    public RoleData get(String name) throws IAMException {

        BooleanExpression predicate = this.role.name.eq(name);
        var role = this.roleRepository.findOne(predicate);

        if (role.isEmpty()) {
            throw new IAMException(IAMErrors.ROLE_NOT_FOUND.format(name));
        }

        return new RoleData(role.get());
    }

    @Override
    public List<RoleData> getAll() throws IAMException {

        BooleanExpression predicate = this.role.active.isTrue();

        var roles = (List<Role>) this.roleRepository.findAll(predicate);

        return roles.stream()
                    .map(RoleData::new)
                    .toList();
    }

    @Override
    public RoleData get(RoleId roleId) throws IAMException {

        BooleanExpression predicate = this.role.roleId.eq(roleId);
        var role = this.roleRepository.findOne(predicate);

        if (role.isEmpty()) {
            throw new IAMException(IAMErrors.ROLE_NOT_FOUND.format(roleId.getId().toString()));
        }

        return new RoleData(role.get());
    }

}
