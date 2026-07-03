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
package com.thitsaworks.operation_portal.core.iam.command.impl;

import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.iam.command.CreateOrUpdateActionCommand;
import com.thitsaworks.operation_portal.core.iam.model.Action;
import com.thitsaworks.operation_portal.core.iam.model.repository.ActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateOrUpdateActionCommandHandler implements CreateOrUpdateActionCommand {

    private final ActionRepository actionRepository;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) {

        Optional<Action> optAction = this.actionRepository.findOne(
            ActionRepository.Filters.withActionCode(input.actionCode()));

        Action action;

        if (optAction.isPresent()) {

            action = optAction.get();
            action.scope(input.scope()).category(input.category()).mandatory(input.isMandatory()).description(input.description());

        } else {

            action = new Action(
                input.actionCode(), input.scope(), input.category(), input.isMandatory(),
                input.description());
        }

        this.actionRepository.save(action);

        return new Output(action.getActionId());
    }

}
