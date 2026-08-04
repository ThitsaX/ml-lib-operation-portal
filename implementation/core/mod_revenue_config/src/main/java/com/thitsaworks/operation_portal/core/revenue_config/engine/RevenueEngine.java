package com.thitsaworks.operation_portal.core.revenue_config.engine;

import com.thitsaworks.operation_portal.component.common.identifier.RevenueConfigId;
import com.thitsaworks.operation_portal.component.common.type.RevenueConfigCategory;
import com.thitsaworks.operation_portal.core.revenue_config.exception.RevenueConfigException;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;

public interface RevenueEngine {

    String SETTINGS_PATH = "revenue_engine/settings";

    RevenueSplit calculateRevenue(String taxCodeId, BigDecimal amount)
        throws RevenueConfigException;

    void archiveExpiredRevenueConfigs();

    void runStatusLifecycleJob();

    enum ScheduleMode {
        FIXED_RATE,
        FIXED_TIME
    }

    record JobSchedule(ScheduleMode mode,
                       long delay,
                       long period,
                       ZoneId zoneId,
                       LocalTime time) { }

    record Settings(JobSchedule runStatusSchedule, JobSchedule archiveSchedule) { }

    record RevenueSplit(RevenueConfigId revenueConfigId,
                        String taxCodeId,
                        String taxCodeDescription,
                        RevenueConfigCategory revenueConfigCategory,
                        String responsibleMinistryCode,
                        String thirdPartyProviderCode,
                        BigDecimal amount,
                        BigDecimal golPercentage,
                        BigDecimal golAmount,
                        BigDecimal ministryPercentage,
                        BigDecimal ministryAmount,
                        BigDecimal thirdPartyPercentage,
                        BigDecimal thirdPartyAmount,
                        BigDecimal sendingDfspPercentage,
                        BigDecimal sendingDfspAmount,
                        String roundMode,
                        String remainderRecipient) { }

}
