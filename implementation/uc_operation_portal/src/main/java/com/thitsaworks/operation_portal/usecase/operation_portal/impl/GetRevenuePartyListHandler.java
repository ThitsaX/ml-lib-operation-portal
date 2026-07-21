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
package com.thitsaworks.operation_portal.usecase.operation_portal.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.operation_portal.component.misc.annotation.ActionMetadata;
import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.util.ActionCategory;
import com.thitsaworks.operation_portal.core.audit.command.CreateExceptionAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateInputAuditCommand;
import com.thitsaworks.operation_portal.core.audit.command.CreateOutputAuditCommand;
import com.thitsaworks.operation_portal.core.iam.cache.PrincipalCache;
import com.thitsaworks.operation_portal.core.revenue_party.data.RevenuePartyData;
import com.thitsaworks.operation_portal.core.revenue_party.query.RevenuePartyQuery;
import com.thitsaworks.operation_portal.usecase.OperationPortalAuditableUseCase;
import com.thitsaworks.operation_portal.usecase.operation_portal.GetRevenuePartyList;
import com.thitsaworks.operation_portal.usecase.util.action.ActionAuthorizationManager;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@ActionMetadata(category = ActionCategory.REVENUE_PARTY)
public class GetRevenuePartyListHandler
    extends OperationPortalAuditableUseCase<GetRevenuePartyList.Input, GetRevenuePartyList.Output>
    implements GetRevenuePartyList {

    private final RevenuePartyQuery revenuePartyQuery;

    public GetRevenuePartyListHandler(CreateInputAuditCommand createInputAuditCommand,
                                      CreateOutputAuditCommand createOutputAuditCommand,
                                      CreateExceptionAuditCommand createExceptionAuditCommand,
                                      ObjectMapper objectMapper,
                                      PrincipalCache principalCache,
                                      ActionAuthorizationManager actionAuthorizationManager,
                                      RevenuePartyQuery revenuePartyQuery) {

        super(createInputAuditCommand, createOutputAuditCommand, createExceptionAuditCommand,
              objectMapper, principalCache, actionAuthorizationManager);

        this.revenuePartyQuery = revenuePartyQuery;
    }

    @Override
    protected Output onExecute(Input input) throws DomainException {

        int page = input.page() == null || input.page() < 0 ? 0 : input.page();
        int size = input.size() == null || input.size() < 1 ? 20 : Math.min(input.size(), 100);
        String searchText = input.searchText() == null ? "" : input.searchText().trim().toLowerCase(Locale.ROOT);

        List<RevenuePartyData> filtered = this.revenuePartyQuery.getRevenueParties().stream()
                                                      .filter(data -> input.partyType() == null ||
                                                          input.partyType().equalsIgnoreCase(data.partyType()))
                                                      .filter(data -> input.isActive() == null ||
                                                          input.isActive() == data.isActive())
                                                      .filter(data -> searchText.isEmpty() ||
                                                          this.contains(data.partyCode(), searchText) ||
                                                          this.contains(data.partyName(), searchText) ||
                                                          this.contains(data.description(), searchText))
                                                      .sorted(this.comparator(input.sortBy(), input.sortDirection()))
                                                      .toList();

        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

        return new Output(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, page, size);
    }

    private boolean contains(String value, String searchText) {

        return value != null && value.toLowerCase(Locale.ROOT).contains(searchText);
    }

    private Comparator<RevenuePartyData> comparator(String sortBy, Sort.Direction direction) {

        Comparator<RevenuePartyData> comparator = switch (sortBy == null ? "partyName" : sortBy) {
            case "partyCode" -> Comparator.comparing(RevenuePartyData::partyCode, String.CASE_INSENSITIVE_ORDER);
            case "partyType" -> Comparator.comparing(RevenuePartyData::partyType, String.CASE_INSENSITIVE_ORDER);
            case "createdDate" -> Comparator.comparing(RevenuePartyData::createdDate);
            case "updatedDate" -> Comparator.comparing(RevenuePartyData::updatedDate);
            default -> Comparator.comparing(RevenuePartyData::partyName, String.CASE_INSENSITIVE_ORDER);
        };

        return direction == Sort.Direction.DESC ? comparator.reversed() : comparator;
    }

}
