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
import com.thitsaworks.operation_portal.component.common.type.ActionCode;
import com.thitsaworks.operation_portal.core.iam.data.ActionData;
import com.thitsaworks.operation_portal.core.iam.engine.IAMEngine;
import com.thitsaworks.operation_portal.core.iam.exception.IAMErrors;
import com.thitsaworks.operation_portal.core.iam.exception.IAMException;
import com.thitsaworks.operation_portal.core.iam.model.QAction;
import com.thitsaworks.operation_portal.core.iam.model.repository.ActionRepository;
import com.thitsaworks.operation_portal.core.iam.query.ActionQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActionJpaQueryHandler implements ActionQuery {

    private static final Logger LOG = LoggerFactory.getLogger(ActionJpaQueryHandler.class);

    private final QAction iamAction = QAction.action;

    private final ActionRepository actionRepository;

    private final IAMEngine iamEngine;

    @Override
    public ActionData get(ActionCode actionCode) throws IAMException {

        BooleanExpression predicate = this.iamAction.actionCode.eq(actionCode);

        var actionAvailable = this.iamEngine.getAction(actionCode);

        if (actionAvailable == null) {

            var optFetchAction = this.actionRepository.findOne(predicate);

            if (optFetchAction.isEmpty()) {
                throw new IAMException(IAMErrors.ACTION_NOT_FOUND.format(actionCode.getValue()));

            } else {

                var fetchAction = optFetchAction.get();

                this.iamEngine.addAction(fetchAction.getActionId(),
                                         fetchAction.getActionCode(),
                                         new ActionData(fetchAction));

                return new ActionData(fetchAction);

            }

        }

        return actionAvailable;

    }

}
