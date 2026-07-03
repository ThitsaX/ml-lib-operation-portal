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
package com.thitsaworks.operation_portal.core.settlement.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.thitsaworks.operation_portal.component.common.identifier.SchedulerConfigId;
import com.thitsaworks.operation_portal.component.common.identifier.SettlementModelId;
import com.thitsaworks.operation_portal.core.settlement.model.QSettlementModel;
import com.thitsaworks.operation_portal.core.settlement.model.SettlementModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementModelRepository
        extends JpaRepository<SettlementModel, SettlementModelId>, QuerydslPredicateExecutor<SettlementModel> {

    class Filters {

        private static final QSettlementModel settlementModel = QSettlementModel.settlementModel;

        public static BooleanExpression findByName(String name) {

            return settlementModel.name.eq(name);
        }

        public static BooleanExpression findBySchedulerConfigId(SchedulerConfigId schedulerConfigId) {

            return settlementModel.settlementSchedulerConfigs.any().settlementSchedulerConfigId.schedulerConfigId.eq(
                    schedulerConfigId.getId());
        }

    }

}
