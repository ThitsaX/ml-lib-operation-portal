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
package com.thitsaworks.operation_portal.usecase;

import com.thitsaworks.operation_portal.core.approval.ApprovalConfiguration;
import com.thitsaworks.operation_portal.core.audit.AuditConfiguration;
import com.thitsaworks.operation_portal.core.hub_services.HubServicesConfiguration;
import com.thitsaworks.operation_portal.core.iam.IAMConfiguration;
import com.thitsaworks.operation_portal.core.participant.ParticipantConfiguration;
import com.thitsaworks.operation_portal.core.revenue_config.RevenueConfigConfiguration;
import com.thitsaworks.operation_portal.core.revenue_party.RevenuePartyConfiguration;
import com.thitsaworks.operation_portal.core.revenue_transaction.RevenueTransactionConfiguration;
import com.thitsaworks.operation_portal.core.scheduler.SchedulerConfiguration;
import com.thitsaworks.operation_portal.core.settlement.SettlementConfiguration;
import com.thitsaworks.operation_portal.reporting.report.ReportConfiguration;
import com.thitsaworks.operation_portal.usecase.operation_portal.scheduler.SchedulerEngine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan("com.thitsaworks.operation_portal.usecase")
@Import(
    value = {
        ParticipantConfiguration.class, AuditConfiguration.class, IAMConfiguration.class,
        HubServicesConfiguration.class, ApprovalConfiguration.class,
        ReportConfiguration.class, SchedulerConfiguration.class, SettlementConfiguration.class,
        RevenuePartyConfiguration.class, RevenueTransactionConfiguration.class,
        RevenueConfigConfiguration.class,RevenuePartyConfiguration.class
    })
@RequiredArgsConstructor
public class OperationPortalUseCaseConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(OperationPortalUseCaseConfiguration.class);

    private final SchedulerEngine schedulerEngine;

    @PostConstruct
    public void bootstrapSchedulerEngine() {

        try {

            LOG.info("Starting SchedulerEngine bootstrap...");
            this.schedulerEngine.bootstrap();
            LOG.info("SchedulerEngine bootstrap completed successfully");

        } catch (Exception e) {

            LOG.error("Failed to bootstrap SchedulerEngine", e);
            throw new IllegalStateException("Failed to bootstrap SchedulerEngine", e);
        }
    }



}
