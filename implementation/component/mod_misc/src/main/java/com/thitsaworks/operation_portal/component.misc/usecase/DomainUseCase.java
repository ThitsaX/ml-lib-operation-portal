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
package com.thitsaworks.operation_portal.component.misc.usecase;

import com.thitsaworks.operation_portal.component.misc.exception.DomainException;
import com.thitsaworks.operation_portal.component.misc.exception.SystemException;
import com.thitsaworks.operation_portal.component.misc.persistence.transactional.CoreWriteTransactional;

import java.net.ConnectException;

public abstract class DomainUseCase<I, O> implements UseCase<I, O> {

    public O execute(I input) throws DomainException {

        O output;

        this.beforeExecute(input);

        try {

            output = this.onExecute(input);

            this.afterExecute(output);

        } catch (RuntimeException exception) {

            throw exception;

        } catch (Exception exception) {

            throw this.onException(exception);
        }

        return output;
    }

    public abstract String getName();

    public abstract void onConstruct() throws SystemException;

    protected abstract void afterExecute(O output) throws DomainException;

    protected abstract void beforeExecute(I input) throws DomainException;

    protected abstract DomainException onException(Exception exception);

    protected abstract O onExecute(I input) throws DomainException, ConnectException;

}
