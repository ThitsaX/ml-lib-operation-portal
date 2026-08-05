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

package com.thitsaworks.operation_portal.core.iam.model;

import com.thitsaworks.operation_portal.component.common.identifier.RoleId;
import com.thitsaworks.operation_portal.component.misc.exception.InputException;
import com.thitsaworks.operation_portal.component.misc.persistence.jpa.JpaEntity;
import com.thitsaworks.operation_portal.component.misc.util.Snowflake;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.listener.RoleListener;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@EntityListeners(RoleListener.class)
@Table(name = "tbl_role")
@NoArgsConstructor
@Getter
public class Role extends JpaEntity<RoleId> {

    @EmbeddedId
    protected RoleId roleId;

    @Column(name = "name")
    protected String name;

    @Column(name = "active")
    protected Boolean active = true;

    @Column(name = "role_type")
    protected String roleType;

    @Getter(AccessLevel.NONE)
    @OneToMany(
        mappedBy = "role",
        cascade = ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER)
    protected Set<RoleGrant> grants = new HashSet<>();

    public Role(String name, String roleType) {

        assert name != null : "name is required!";

        this.roleId = new RoleId(Snowflake.get().nextId());
        this.name(name);
        this.roleType = roleType;
    }

    @Override
    public RoleId getId() {

        return this.roleId;
    }

    public Role name(String name) {

        if (name == null || name.isBlank()) {

            throw new InputException(IAMErrors.INVALID_ROLE_NAME);
        }

        this.name = name;

        return this;
    }

    public boolean isGranted(Action Action) {

        return this.grants.stream().anyMatch(granted -> granted.Action.equals(Action));
    }

    public void grantAction(Action granting) {

        Optional<RoleGrant> optRoleGrant = this.grants
                                               .stream()
                                               .filter(
                                                   roleGrant -> roleGrant.Action.equals(granting))
                                               .findFirst();

        if (optRoleGrant.isEmpty()) {

            this.grants.add(new RoleGrant(this, granting));
        }
    }

    public void grantActions(List<Action> grantingActions) {

        Set<Action> requestedActions =
            grantingActions == null ? Set.of() : new LinkedHashSet<>(grantingActions);

        this.grants.removeIf(existingGrant -> !requestedActions.contains(existingGrant.Action));

        requestedActions.forEach(this::grantAction);
    }

    public Set<Action> getGrantedActions() {

        return this.grants.stream().map(RoleGrant::getAction).collect(Collectors.toSet());
    }

    public boolean revokeAction(Action revoking) {

        Optional<RoleGrant> optRoleGrant = this.grants
                                               .stream()
                                               .filter(
                                                   roleGrant -> roleGrant.Action.equals(revoking))
                                               .findFirst();

        if (optRoleGrant.isPresent()) {

            this.grants.remove(optRoleGrant.get());
            return true;
        }

        return false;
    }

    public void toggle() {

        this.active = !this.active;
    }

}
