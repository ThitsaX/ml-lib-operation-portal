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
import com.thitsaworks.operation_portal.reporting.report.domain.GenerateSettlementDetailReportCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ReportConfiguration.class, TestSettings.class})
public class DFSPGenerateSettlementDetailReportCommandHandlerUnitTest {

    @Autowired
    private GenerateSettlementDetailReportCommand generateSettlementDetailReportCommand;

    @Test
    public void testGenerateReportSuccessfully() throws Exception {

        GenerateSettlementDetailReportCommand.Output output = this.generateSettlementDetailReportCommand.execute(new GenerateSettlementDetailReportCommand.Input("1",
                                                                                                                                                                 "all",
                                                                                                                                                                 "All",
                                                                                                                                                                 ".xlsx",
                                                                                                                                                                 "0630",
                                                                                                                                                                 null,
                                                                                                                                                                 null));

        System.out.println(Arrays.toString(output.settlementDetailRptByte()));
    }

}
