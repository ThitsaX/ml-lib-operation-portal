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
package com.thitsaworks.operation_portal.reporting.report.command;

import com.thitsaworks.operation_portal.reporting.report.ReportConfiguration;
import com.thitsaworks.operation_portal.reporting.report.TestSettings;
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateFeeSettlementReportCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@ContextConfiguration(classes = {
        ReportConfiguration.class, TestSettings.class})
public class FeeReportUnitTest {

    @Autowired
    private GenerateFeeSettlementReportCommand generateFeeSettlementReportCommand;

    @Test
    public void testGenerateReportSuccessfully() throws Exception {

        FileOutputStream foutdetail = new FileOutputStream(new File("C:\\Workspace\\Development\\abc.xlsx"));

        Instant startDate = LocalDate.of(2022, 9, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        Instant endDate = LocalDate.of(2022, 9, 9).atStartOfDay(ZoneId.systemDefault()).toInstant();

        generateFeeSettlementReportCommand.execute(new GenerateFeeSettlementReportCommand.Input(startDate,
                                                                                                endDate,
                                                                                                "all",
                                                                                                "all",
                                                                                                "all",
                                                                                                "0630",
                                                                                                "xlsx"));
    }

}
