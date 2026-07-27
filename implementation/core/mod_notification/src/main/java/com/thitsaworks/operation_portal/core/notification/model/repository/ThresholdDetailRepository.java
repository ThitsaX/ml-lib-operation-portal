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
package com.thitsaworks.operation_portal.core.notification.model.repository;

import com.thitsaworks.operation_portal.component.common.identifier.ThresholdConfigurationId;
import com.thitsaworks.operation_portal.component.common.identifier.ThresholdDetailId;
import com.thitsaworks.operation_portal.component.common.type.NdcConfigurationStatus;
import com.thitsaworks.operation_portal.component.common.type.ThresholdScopeType;
import com.thitsaworks.operation_portal.core.notification.model.ThresholdDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThresholdDetailRepository
    extends JpaRepository<ThresholdDetail, ThresholdDetailId>,
            QuerydslPredicateExecutor<ThresholdDetail> {

    List<ThresholdDetail> findAllByStatusTrueOrderByCurrencyAsc();

    List<ThresholdDetail> findAllByThresholdConfigurationIdOrderByCurrencyAsc(
        ThresholdConfigurationId thresholdConfigurationId);

    List<ThresholdDetail> findAllByThresholdConfigurationIdAndStatusOrderByCurrencyAsc(
        ThresholdConfigurationId thresholdConfigurationId, boolean status);

    List<ThresholdDetail> findAllByStatusOrderByThresholdConfigurationIdAscCurrencyAsc(boolean status);

    List<ThresholdDetail> findAllByOrderByThresholdConfigurationIdAscCurrencyAsc();

    Optional<ThresholdDetail> findFirstByThresholdConfigurationIdAndCurrencyAndStatusTrue(
        ThresholdConfigurationId thresholdConfigurationId, String currency);

    boolean existsByThresholdConfigurationIdAndCurrency(
        ThresholdConfigurationId thresholdConfigurationId, String currency);

    boolean existsByThresholdConfigurationIdAndCurrencyAndThresholdDetailIdNot(
        ThresholdConfigurationId thresholdConfigurationId,
        String currency,
        ThresholdDetailId thresholdDetailId);

    @Query("SELECT td FROM ThresholdDetail td " +
           "JOIN ThresholdConfiguration tc ON td.thresholdConfigurationId = tc.thresholdConfigurationId " +
           "WHERE tc.scopeType = :scopeType " +
           "AND tc.dfspId = :dfspId " +
           "AND tc.thresholdEnabled = :thresholdEnabled " +
           "AND tc.status = :configStatus " +
           "AND td.status = :detailStatus")
    List<ThresholdDetail> findDfspThresholdDetails(
        @Param("scopeType") ThresholdScopeType scopeType,
        @Param("dfspId") String dfspId,
        @Param("thresholdEnabled") boolean thresholdEnabled,
        @Param("configStatus") NdcConfigurationStatus configStatus,
        @Param("detailStatus") boolean detailStatus);

    @Query("SELECT td, tc.dfspId FROM ThresholdDetail td " +
           "JOIN ThresholdConfiguration tc ON td.thresholdConfigurationId = tc.thresholdConfigurationId " +
           "WHERE tc.scopeType = :scopeType " +
           "AND tc.thresholdEnabled = :thresholdEnabled " +
           "AND tc.status = :configStatus " +
           "AND td.status = :detailStatus")
    List<Object[]> findAllDfspThresholdDetails(
        @Param("scopeType") ThresholdScopeType scopeType,
        @Param("thresholdEnabled") boolean thresholdEnabled,
        @Param("configStatus") NdcConfigurationStatus configStatus,
        @Param("detailStatus") boolean detailStatus);

}
