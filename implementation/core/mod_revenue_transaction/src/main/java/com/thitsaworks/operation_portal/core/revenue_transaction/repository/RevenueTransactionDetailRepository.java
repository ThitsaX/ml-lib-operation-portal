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
package com.thitsaworks.operation_portal.core.revenue_transaction.repository;

import com.thitsaworks.operation_portal.component.common.identifier.RevenueTransactionDetailId;
import com.thitsaworks.operation_portal.core.revenue_transaction.model.RevenueTransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevenueTransactionDetailRepository
        extends JpaRepository<RevenueTransactionDetail, RevenueTransactionDetailId> {
}
