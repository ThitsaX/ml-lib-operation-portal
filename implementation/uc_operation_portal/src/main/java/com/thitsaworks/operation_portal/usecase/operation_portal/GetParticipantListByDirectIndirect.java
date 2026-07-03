package com.thitsaworks.operation_portal.usecase.operation_portal;

import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.misc.usecase.UseCase;

import java.util.List;

public interface GetParticipantListByDirectIndirect
    extends UseCase<GetParticipantListByDirectIndirect.Input, GetParticipantListByDirectIndirect.Output> {

    record Input() { }

    record Output(List<ParticipantInfo> participantInfoList) {

        public record ParticipantInfo(ParticipantId participantId,
                                      String participantName,
                                      String participantDescription,
                                      String logoFileType,
                                      byte[] logo) { }

    }

}
