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
package com.thitsaworks.operation_portal.component.misc.http;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CachedBodyServletInputStream extends ServletInputStream {

    private static final Logger LOG = LoggerFactory.getLogger(CachedBodyServletInputStream.class);

    private InputStream cachedBodyInputStream;

    public CachedBodyServletInputStream(byte[] cachedBody) {

        this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);

    }

    @Override
    public boolean isFinished() {

        try {

            return cachedBodyInputStream.available() == 0;

        } catch (IOException e) {

            LOG.error("Error : ", e);

        }

        return false;

    }

    @Override
    public boolean isReady() {

        return true;

    }

    @Override
    public int read() throws IOException {

        return cachedBodyInputStream.read();

    }

    @Override
    public void setReadListener(ReadListener readListener) {

        throw new UnsupportedOperationException();

    }

}