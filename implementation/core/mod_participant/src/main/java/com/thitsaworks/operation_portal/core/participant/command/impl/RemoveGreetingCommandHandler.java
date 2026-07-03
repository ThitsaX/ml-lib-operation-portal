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
package com.thitsaworks.operation_portal.core.participant.command.impl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thitsaworks.operation_portal.component.common.identifier.GreetingId;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;
import com.thitsaworks.operation_portal.core.participant.command.RemoveGreetingCommand;
import com.thitsaworks.operation_portal.core.participant.model.GreetingMessage;
import com.thitsaworks.operation_portal.core.participant.model.QGreetingMessage;
import com.thitsaworks.operation_portal.core.participant.model.repository.GreetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RemoveGreetingCommandHandler implements RemoveGreetingCommand {
    private final GreetingRepository greetingRepository;

    private final QGreetingMessage greetingMessage= QGreetingMessage.greetingMessage;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    @CoreWriteTransactional
    public Output execute(Input input) {
        LocalDate currentDate = LocalDate.now();
        LocalDate currentDateMinus6Months = currentDate.minusMonths(6);
        Instant
            removeDate = currentDateMinus6Months.atStartOfDay(ZoneId.systemDefault()).toInstant();

        JPAQuery<Long> sqlGreetingQuery =
            this.jpaQueryFactory.select(greetingMessage.greetingId.id).from(greetingMessage)
                                .where(greetingMessage.greetingDate.lt(removeDate));

        QueryResults<Long> greetingResults = sqlGreetingQuery.fetchResults();

        Set<GreetingId>greetingIds = new HashSet<>();

        if (greetingResults != null && !greetingResults.isEmpty()) {

            for (Long id : greetingResults.getResults()) {
                greetingIds.add(new GreetingId(id));
            }
        }

        List<GreetingMessage> greetingMessagesList = this.greetingRepository.findAllById(greetingIds);

        for (GreetingMessage greetingMessage : greetingMessagesList) {
            this.greetingRepository.save(greetingMessage.isDeleted(true));
        }

        return new RemoveGreetingCommand.Output(true);
    }

}
