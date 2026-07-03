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

import com.thitsaworks.operation_portal.component.test.EnvAwareUnitTest;
import com.thitsaworks.operation_portal.reporting.report.ReportConfiguration;
import com.thitsaworks.operation_portal.reporting.report.TestSettings;
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateSettlementReportCommand;
import com.thitsaworks.operation_portal.reporting.report.domain.impl.GenerateSettlementReportCommandHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.io.FileOutputStream;

@ContextConfiguration(classes = {
        ReportConfiguration.class, TestSettings.class})
public class GenerateSettlementReportCommandHandlerUnitTest extends EnvAwareUnitTest {

    @Autowired
    private GenerateSettlementReportCommandHandler generateSettlementReportCommandHandler;

    @Test
    public void testGenerateReportSuccessfully() throws Exception {

        FileOutputStream fout = new FileOutputStream(new File("C:\\settlement_report.xlsx"));

        generateSettlementReportCommandHandler.execute(new GenerateSettlementReportCommand.Input("mmdokdollar",
                                                                                                 "1",
                                                                                                 "1",
                                                                                                 ".xlsx",
                                                                                                 "0630",
                                                                                                 "",
                                                                                                 null,
                                                                                                 null));
    }
}
