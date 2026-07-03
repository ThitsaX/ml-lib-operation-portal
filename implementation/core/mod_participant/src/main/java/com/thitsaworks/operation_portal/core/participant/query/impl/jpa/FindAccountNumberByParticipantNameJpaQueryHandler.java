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
package com.thitsaworks.operation_portal.core.participant.query.impl.jpa;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thitsaworks.operation_portal.component.common.type.ParticipantName;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreReadTransactional;
import com.thitsaworks.operation_portal.core.participant.model.QLiquidityProfile;
import com.thitsaworks.operation_portal.core.participant.model.QParticipant;
import com.thitsaworks.operation_portal.core.participant.query.FindAccountNumberByParticipantNameQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@CoreReadTransactional
@RequiredArgsConstructor
public class FindAccountNumberByParticipantNameJpaQueryHandler implements FindAccountNumberByParticipantNameQuery {

    private static final Logger LOG = LoggerFactory.getLogger(FindAccountNumberByParticipantNameJpaQueryHandler.class);

    private final QLiquidityProfile liquidityProfile = QLiquidityProfile.liquidityProfile;

    private final QParticipant participant = QParticipant.participant;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Output execute(Input input) {

        ParticipantName participantName = new ParticipantName(input.participantName());
        String currencyId = input.currencyId();

        JPAQuery<String> tupleSQLQuery =
            this.jpaQueryFactory.select(liquidityProfile.accountNumber)
                                .from(liquidityProfile)
                                .join(participant)
                                .on(participant.participantId.eq(liquidityProfile.participant.participantId))
                                .where(liquidityProfile.currency.eq(currencyId)
                                                                .and(participant.participantName.eq(participantName)));

        String tuple = tupleSQLQuery.fetchOne();

        return new FindAccountNumberByParticipantNameQuery.Output(
            tuple == null ? tuple = "000000000000" : ((tuple)));
    }

}
