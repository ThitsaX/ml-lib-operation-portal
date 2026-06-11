package com.thitsaworks.operation_portal.usecase.operation_portal;

import com.thitsaworks.operation_portal.component.misc.usecase.UseCase;

public interface GenerateFeeAmountSwiftReport
    extends UseCase<GenerateFeeAmountSwiftReport.Input, GenerateFeeAmountSwiftReport.Output> {

    record Input(String settlementId,
                 String currencyId,
                 String timezone,
                 Long userId) { }

    record Output(byte[] reportData) { }

}
