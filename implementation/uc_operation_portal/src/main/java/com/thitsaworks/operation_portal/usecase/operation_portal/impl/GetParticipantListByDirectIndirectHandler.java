package com.thitsaworks.operation_portal.usecase.operation_portal.impl;

import com.thitsaworks.operation_portal.component.common.identifier.ParticipantId;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.participant.data.ParticipantData;
import com.thitsaworks.operation_portal.core.participant.query.ParticipantQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetParticipantListByDirectIndirect;
import com.thitsaworks.operation_portal.usecase.util.UserPermissionManager;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

@Service
@ActionMetadata(category = ActionCategory.PARTICIPANT_MANAGEMENT)
public class GetParticipantListByDirectIndirectHandler
    extends OperationPortalUseCase<GetParticipantListByDirectIndirect.Input, GetParticipantListByDirectIndirect.Output>
    implements GetParticipantListByDirectIndirect {

    private final ParticipantQuery participantQuery;

    private final UserPermissionManager userPermissionManager;

    public GetParticipantListByDirectIndirectHandler(PrincipalCache principalCache,
                                                     ActionAuthorizationManager actionAuthorizationManager,
                                                     ParticipantQuery participantQuery,
                                                     UserPermissionManager userPermissionManager) {

        super(principalCache, actionAuthorizationManager);

        this.participantQuery = participantQuery;
        this.userPermissionManager = userPermissionManager;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException, ConnectException {

        var currentUser = this.userPermissionManager.getCurrentUser();

        List<Output.ParticipantInfo> participantInfoList = new ArrayList<>();

        if (this.userPermissionManager.isDfsp(currentUser.principalId())) {

            var participantId = new ParticipantId(currentUser.realmId().getId());

            if (this.participantQuery.isDirectParticipant(participantId)) {

                List<ParticipantData> participantDataList = this.participantQuery.getParticipantListIncludingSponsoredParticipants(
                    participantId);
                for (ParticipantData participantData : participantDataList) {
                    this.addParticipantInfo(participantInfoList, participantData);
                }
            } else {
                var participantData = this.participantQuery.get(participantId);

                this.addParticipantInfo(participantInfoList, participantData);
            }

        } else {

            List<ParticipantData> participantDataList = this.participantQuery.getAllParticipants();

            for (ParticipantData participantData : participantDataList) {
                this.addParticipantInfo(participantInfoList, participantData);
            }

        }
        return new Output(participantInfoList);

    }

    private void addParticipantInfo(List<Output.ParticipantInfo> participantInfoList,
                                    ParticipantData participantData) {

        participantInfoList.add(new Output.ParticipantInfo(
            participantData.participantId(),
            participantData.participantName()
                           .getValue(),
            participantData.description(),
            participantData.logoFileType(),
            participantData.logo()));

    }

}
