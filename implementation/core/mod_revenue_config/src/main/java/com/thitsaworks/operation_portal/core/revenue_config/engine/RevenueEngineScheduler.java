package com.thitsaworks.operation_portal.core.revenue_config.engine;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RevenueEngineScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevenueEngineScheduler.class);

    private final RevenueEngine revenueEngine;

    private final RevenueEngine.Settings settings;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
        runnable -> {
            Thread thread = new Thread(runnable, "revenue-engine-lifecycle");
            thread.setDaemon(true);
            return thread;
        });

    @PostConstruct
    public void bootstrap() {

        this.validateSettings();

        LOGGER.info(
            "Bootstrapping RevenueEngine scheduler. runStatusSchedule=[{}], archiveSchedule=[{}]",
            this.settings.runStatusSchedule(), this.settings.archiveSchedule());

        this.runSafely(
            "archive expired revenue configs", this.revenueEngine::archiveExpiredRevenueConfigs);
        this.runSafely("run revenue status lifecycle", this.revenueEngine::runStatusLifecycleJob);

        this.schedule(
            "run revenue status lifecycle",
            this.settings.runStatusSchedule(),
            this.revenueEngine::runStatusLifecycleJob);
        this.schedule(
            "archive expired revenue configs",
            this.settings.archiveSchedule(),
            this.revenueEngine::archiveExpiredRevenueConfigs);
    }

    @PreDestroy
    public void shutdown() {

        this.scheduler.shutdownNow();
    }

    private void validateSettings() {

        this.validateSchedule("runStatusSchedule", this.settings.runStatusSchedule());
        this.validateSchedule("archiveSchedule", this.settings.archiveSchedule());
    }

    private void validateSchedule(String name, RevenueEngine.JobSchedule schedule) {

        if (schedule.delay() < 0) {
            throw new IllegalArgumentException(
                "Revenue engine " + name + " delay must not be negative");
        }
        if (schedule.mode() == RevenueEngine.ScheduleMode.FIXED_RATE && schedule.period() <= 0) {
            throw new IllegalArgumentException(
                "Revenue engine " + name + " period must be greater than zero");
        }
    }

    private void schedule(String operation,
                          RevenueEngine.JobSchedule schedule,
                          Runnable runnable) {

        if (schedule.mode() == RevenueEngine.ScheduleMode.FIXED_RATE) {
            this.scheduler.scheduleAtFixedRate(
                () -> this.runSafely(operation, runnable),
                schedule.delay(), schedule.period(), TimeUnit.MILLISECONDS);
            return;
        }

        this.scheduleAtFixedTime(operation, schedule.zoneId(), schedule.time(), runnable);
    }

    private void scheduleAtFixedTime(String operation,
                                     ZoneId zoneId,
                                     LocalTime time,
                                     Runnable runnable) {

        long delayMs = this.millisUntilNext(zoneId, time);
        this.scheduler.schedule(
            () -> {
                this.runSafely(operation, runnable);
                this.scheduleAtFixedTime(operation, zoneId, time, runnable);
            }, delayMs, TimeUnit.MILLISECONDS);
    }

    private long millisUntilNext(ZoneId zoneId, LocalTime time) {

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime next = now.toLocalDate().atTime(time).atZone(zoneId);

        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }

        return Duration.between(now, next).toMillis();
    }

    private void runSafely(String operation, Runnable runnable) {

        try {
            runnable.run();
        } catch (Exception exception) {
            LOGGER.error("RevenueEngine [{}] failed", operation, exception);
        }
    }

}
