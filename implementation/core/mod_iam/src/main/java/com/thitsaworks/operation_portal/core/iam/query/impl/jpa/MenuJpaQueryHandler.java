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
import com.thitsaworks.operation_portal.component.common.identifier.MenuId;
import com.thitsaworks.operation_portal.core.iam.data.MenuData;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.iam.model.Menu;
import com.thitsaworks.operation_portal.core.iam.model.QMenu;
import com.thitsaworks.operation_portal.core.iam.model.repository.MenuRepository;
import com.thitsaworks.operation_portal.core.iam.query.MenuQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuJpaQueryHandler implements MenuQuery {

    private final QMenu menu = QMenu.menu;

    private final MenuRepository menuRepository;

    @Override
    public List<MenuData> getAll() throws IAMException {

        BooleanExpression predicate = this.menu.menuId.isNotNull();

        var menus = (List<Menu>) this.menuRepository.findAll(predicate);

        return menus.stream()
                    .map(MenuData::new)
                    .toList();
    }

    @Override
    public MenuData get(MenuId menuId) throws IAMException {

        BooleanExpression predicate = this.menu.menuId.eq(menuId);

        var menu = this.menuRepository.findOne(predicate);

        if (menu.isEmpty()) {
            throw new IAMException(IAMErrors.MENU_NOT_FOUND.format(menuId.getId().toString()));
        }

        return new MenuData(menu.get());
    }

}
