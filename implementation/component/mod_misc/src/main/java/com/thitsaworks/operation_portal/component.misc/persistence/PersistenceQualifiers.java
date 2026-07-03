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
package com.thitsaworks.operation_portal.component.misc.persistence;

public class PersistenceQualifiers {

    public static class Shared {

        public static final String WRITE_SETTINGS = "sharedWriteSettings";

        public static final String WRITE_POOL_SIZES = "sharedWritePoolSizes";

        public static final String WRITE_DATA_SOURCE = "sharedWriteDataSource";

        public static final String WRITE_JDBC_TEMPLATE = "sharedWriteJdbcTemplate";

        public static final String READ_SETTINGS = "sharedReadSettings";

        public static final String READ_POOL_SIZES = "sharedReadPoolSizes";

        public static final String READ_DATA_SOURCE = "sharedReadDataSource";

        public static final String READ_JDBC_TEMPLATE = "sharedReadJdbcTemplate";

        public static final String ENTITY_MANAGER_FACTORY = "entityManagerFactory";

        public static final String TRANSACTION_MANAGER = "transactionManager";

    }

    public static class Core {

        public static final String WRITE_SETTINGS = "coreWriteSettings";

        public static final String WRITE_POOL_SIZES = "coreWritePoolSizes";

        public static final String WRITE_DATA_SOURCE = "coreWriteDataSource";

        public static final String READ_SETTINGS = "coreReadSettings";

        public static final String READ_POOL_SIZES = "coreReadPoolSizes";

        public static final String READ_DATA_SOURCE = "coreReadDataSource";

        public static final String READ_JDBC_TEMPLATE = "coreReadJdbcTemplate";

        public static final String WRITE_JDBC_TEMPLATE = "coreWriteJdbcTemplate";

        public static final String ENTITY_MANAGER_FACTORY = "coreEntityManagerFactory";

        public static final String TRANSACTION_MANAGER = "coreTransactionManager";

        public static final String QUERYDSL_CONFIGURATION = "coreQuerydslConfiguration";

        public static final String JPA_QUERY_FACTORY = "coreJpaQueryFactory";

        public static final String DATA_SOURCE = "coreDataSource";

    }

    public static class Hub {

        public static final String WRITE_SETTINGS = "reportingWriteSettings";

        public static final String WRITE_POOL_SIZES = "reportingWritePoolSizes";

        public static final String WRITE_DATA_SOURCE = "reportingWriteDataSource";

        public static final String READ_SETTINGS = "reportingReadSettings";

        public static final String READ_POOL_SIZES = "reportingReadPoolSizes";

        public static final String READ_DATA_SOURCE = "reportingReadDataSource";

        public static final String READ_JDBC_TEMPLATE = "reportingReadJdbcTemplate";

        public static final String WRITE_JDBC_TEMPLATE = "reportingWriteJdbcTemplate";

        public static final String ENTITY_MANAGER_FACTORY = "reportingEntityManagerFactory";

        public static final String TRANSACTION_MANAGER = "reportingTransactionManager";

        public static final String QUERYDSL_CONFIGURATION = "reportingQuerydslConfiguration";

        public static final String JPA_QUERY_FACTORY = "reportingJpaQueryFactory";

        public static final String DATA_SOURCE = "reportingDataSource";

        public static final String MONGO_READ_SETTINGS   = "reportingMongoReadSettings";
        public static final String MONGO_WRITE_SETTINGS  = "reportingMongoWriteSettings";
        public static final String MONGO_READ_CLIENT     = "reportingMongoReadClient";
        public static final String MONGO_WRITE_CLIENT    = "reportingMongoWriteClient";
        public static final String MONGO_READ_FACTORY    = "reportingMongoReadFactory";
        public static final String MONGO_WRITE_FACTORY   = "reportingMongoWriteFactory";
        public static final String MONGO_READ_TEMPLATE   = "reportingMongoReadTemplate";
        public static final String MONGO_WRITE_TEMPLATE  = "reportingMongoWriteTemplate";
    }

}
