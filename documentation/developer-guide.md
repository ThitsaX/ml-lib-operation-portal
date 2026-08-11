# Operation Portal Developer Guide

This document is a practical setup and architecture guide for developers working on the Mojaloop Operation Portal codebase.

## 1. What this repository contains

The project is a multi-module Java 21 / Spring Boot / Maven application. The main runtime is the web API service, and the rest of the repository is split into reusable domain modules, infrastructure modules, and supporting applications.

The codebase depends on external services such as:

- Vault for configuration and secrets
- MySQL for portal data and central ledger data
- Redis for cache and runtime state
- MongoDB for hub data access in some flows
- S3 for report storage and download flows

## 2. Repository layout

### Top-level folders

- `implementation/` - all Java modules
- `dockers/` - local Docker and Vault bootstrap files
- `documentation/` - request payloads and supporting docs
- `readme.txt` - short reference for initial setup

### Maven module structure

The root Maven project is `implementation/pom.xml`. It aggregates:

- `component/`
- `core/`
- `uc_operation_portal/`
- `web_api_operation/`

### Module responsibilities

#### `implementation/component`

Shared technical building blocks used by the rest of the application:

- `mod_common`
- `mod_infra`
- `mod_misc`
- `mod_fspiop`

`mod_infra` contains infrastructure code and Flyway SQL under:

- `implementation/component/mod_infra/src/main/resources/all/db/core`
- `implementation/component/mod_infra/src/main/resources/all/db/hub`

#### `implementation/core`

Business-domain modules grouped by capability:

- `mod_approval`
- `mod_audit`
- `mod_hub_services`
- `mod_iam`
- `mod_participant`
- `mod_report`
- `mod_scheduler`
- `mod_settlement`

These modules expose domain commands, queries, repositories, models, and handlers.

#### `implementation/uc_operation_portal`

Use-case layer for the operation portal. This module contains the application-facing business flows that the web controllers invoke.

#### `implementation/web_api_operation`

Spring Boot REST API entrypoint. It contains:

- the application main class
- REST controllers
- exception handling
- environment-specific resources

## 3. Runtime architecture

The web application starts from `WebApiOperationPortalApplication`:

- it loads Vault configuration first
- it runs Flyway database migration before the Spring context starts
- it enables scheduling
- it exposes only the `health` actuator endpoint by default

Controllers in `web_api_operation` map HTTP requests to use cases. Those use cases call into `core` modules and shared infrastructure in `component`.

The codebase follows a layered pattern:

1. HTTP controller
2. Use case or command handler
3. Core domain module
4. Repository / query / infrastructure implementation
5. Database, cache, Vault, or external service

## 4. Required external services

### 4.1 Vault

Vault is mandatory. The application reads database, Redis, MongoDB, and S3 settings from Vault paths under the `operation_portal` secret engine.

The Docker setup uses a local Vault dev server on port `8200`.

Important Vault paths:

- `operation_portal/redis/settings`
- `operation_portal/mysql/portal_data/flyway/settings`
- `operation_portal/mysql/portal_data/write_db/settings`
- `operation_portal/mysql/portal_data/read_db/settings`
- `operation_portal/mysql/hub_data/write_db/settings`
- `operation_portal/mysql/hub_data/read_db/settings`
- `operation_portal/mongo/hub_data/write_db/settings`
- `operation_portal/mongo/hub_data/read_db/settings`
- `operation_portal/s3/settings`

Important settings keys:

- Redis: `redisUrl`
- MySQL: `url`, `username`, `password`
- MongoDB: `uri`, `database`

### 4.2 MySQL

Two MySQL databases are required:

- `operation_portal`
- `central_ledger`

The current bootstrap notes expect:

- default charset: `utf8mb4`
- default collation: `utf8mb4_unicode_ci`

The `operation_portal` database user must be able to access both databases because portal code reads both portal data and hub data.

### 4.3 Redis

Redis is used by the application infrastructure. The local Docker compose exposes it on port `6379`.

### 4.4 MongoDB

MongoDB settings are present in Vault and are used by hub-data flows. MongoDB itself is not started by the provided Docker compose, so a real environment or a locally provisioned Mongo instance is required if those code paths are exercised.

### 4.5 S3

S3 configuration is also stored in Vault and is used by report upload/download flows.

## 5. Local development setup

### Step 1: Install prerequisites

You need:

- Java 21
- Maven 3.9+ recommended
- Docker and Docker Compose
- Access to Vault and the required databases, or the local Docker stack from this repository

### Step 2: Start the local infrastructure

Use the compose file at `dockers/docker-compose.yml`.

It starts:

- Vault on `8200`
- a Vault init client container
- Redis on `6379`
- MySQL for Operation Portal on `3307`
- MySQL for Central Ledger on `3308`
- the `operation-portal` application container on `8003`

The compose file assumes the following container hostnames:

- `vault`
- `redis`
- `mysql-operation-portal`
- `mysql-central-ledger`

### Step 3: Initialize Vault

The script `dockers/init-vault.sh`:

1. enables a KV v2 secrets engine at path `operation_portal`
2. writes the Redis, MySQL, MongoDB, and S3 settings
3. verifies the written secrets

If you are using a remote Vault instead of the Docker dev Vault, copy those same paths and secret shapes into the target environment.

### Step 4: Create the MySQL databases

Before the application starts, create both databases:

- `central_ledger`
- `operation_portal`

The Flyway migration runs against the portal database during startup.

### Step 5: Configure environment variables

The main runtime expects Vault settings to be reachable through environment or JVM properties. The Docker compose file sets:

- `VAULT_ADDR=http://vault:8200`
- `VAULT_TOKEN=root-token`
- `ENGINE_PATH=operation_portal`
- `OPERATION_PORTAL_PORT_NO=8003`

### Step 6: Build the project

From `implementation/`, build with Maven:

To generate or refresh license headers after changing the `license-header` file:

```bash
mvn license:format
```

Then run the regular build:

```bash
mvn clean test
```

For a packaged build:

```bash
mvn clean package
```

If you only want the web API module:

```bash
mvn -pl web_api_operation -am clean package
```

## 6. Running the applications

### 6.1 Web API

The main service runs from `implementation/web_api_operation`.

Default local port:

- `8003`

Useful path details:

- public login endpoint: `/public/loginUserAccount`
- secured endpoints generally live under `/secured/...`

### 6.2 Scheduler runtime

Scheduler jobs run inside the main web application. `implementation/web_api_operation`
imports the use-case layer, and `OperationPortalUseCaseConfiguration` bootstraps
`SchedulerEngine` during application startup.

## 7. First-time bootstrap API calls

After Vault and the databases are ready, and after the app is up for the first time, run the one-time bootstrap APIs in this order:

1. `POST /secured/syncHubSettlementModelsToPortal`
2. `POST /secured/grantRoleActionList`
3. `POST /secured/grantMenuActionList`

The settlement-model sync pulls settlement models from Mojaloop's settlement API into the portal. The grant APIs then initialize role/action mapping and menu/action mapping.

Grant menus and actions are described in `documentation/grants/`.

The request payloads for the grant APIs are stored in:

- `documentation/grants/GrantRoleActionList.txt`
- `documentation/grants/GrantMenuActionList.txt`

### 7.1 Adding new actions to role grants

When a new secured action is added, make sure the action is mapped to the
respective role. Apply the updated role/action mapping by running:

1. `POST /secured/grantRoleActionList`

For the RTGS <> Operation Portal integration spec for SISP, grant the
`SubmitParticipantBalance` action to the `HUB-Admin` role:

```json
{
  "roleGrantList": [
    {
      "roleName": "HUB-Admin",
      "actionCodeList": [
        "SubmitParticipantBalance"
      ]
    }
  ]
}
```

### 7.2 Adding new menus to action grants

When a new menu is added, make sure the menu is mapped to the
respective action. Apply the updated menu/action mapping by running:

1. `POST /secured/grantMenuActionList`

For a new menu, add the menu name and bind it with the related actions:

```json
{
  "menuGrantList": [
    {
      "menuName": "New Menu",
      "actionCodeList": [
        "RelatedAction"
      ]
    }
  ]
}
```

## 8. Code structure guide

### 8.1 Naming conventions

The codebase uses a consistent command-style naming pattern:

- `Create...`
- `Modify...`
- `Remove...`
- `Get...`
- `Generate...`
- `Sync...`
- `Grant...`
- `Revoke...`

This makes it easy to find the write path, read path, and initialization path for a feature.

### 8.2 Core module patterns

Inside `core`, most features follow the same structure:

- `command/` or `query/`
- `command/impl/` or `query/impl/`
- `data/`
- `model/`
- `model/repository/`
- `exception/`

For example, `mod_scheduler` includes:

- scheduler configuration
- scheduler commands and handlers
- scheduler config data and persistence models
- query handlers for read paths

### 8.3 Web API pattern

Inside `web_api_operation`, controllers are grouped by capability:

- `controller/coreServices`
- `controller/hubServices`
- `controller/engineServices`

There is also a centralized error handler under:

- `error/ErrorHandlerAdvice.java`

### 8.4 Infrastructure resources

Flyway SQL files live under:

- `implementation/component/mod_infra/src/main/resources/all/db/core`
- `implementation/component/mod_infra/src/main/resources/all/db/hub`

If you change schema behavior, update the Flyway scripts first and then align the code.

## 9. Environment-specific resources

### Web API profiles

`web_api_operation` includes resource folders for:

- `local`
- `dev`
- `stg`
- `live`

Scheduler runtime configuration follows the `web_api_operation` resource profiles.

## 10. Typical developer workflow

1. Pull the repository and open `implementation/`.
2. Make sure Java 21 and Maven are available.
3. Start Vault, Redis, and both MySQL instances.
4. Load the Vault secrets using `dockers/init-vault.sh` or the same secret shape in your target Vault.
5. Create the `operation_portal` and `central_ledger` databases.
6. Build the code with Maven.
7. Start `web_api_operation`.
8. Run the bootstrap endpoints once per environment.
9. Add or modify business logic in the correct layer:
   - controller for HTTP shape
   - use case for orchestration
   - core module for domain behavior
   - component module for shared infrastructure

## 11. Troubleshooting

### Application fails before Spring starts

Check whether Vault is reachable and whether the `operation_portal` secret engine exists.

### Migration fails

Check that the MySQL `operation_portal` database exists and that the Flyway Vault settings point to the correct host, port, and credentials.

### Missing config at runtime

Verify the Vault path and key names exactly match the bootstrap script:

- `operation_portal/mysql/portal_data/write_db/settings`
- `operation_portal/mysql/hub_data/read_db/settings`
- and so on

### Local port conflict

The default web port is `8003`. Change `implementation/web_api_operation/src/main/resources/local/application.properties` if that port is already used.

## 12. Quick reference

- Main API entrypoint: `implementation/web_api_operation/src/main/java/com/thitsaworks/operation_portal/api/operation/portal/WebApiOperationPortalApplication.java`
- Docker compose: `dockers/docker-compose.yml`
- Vault init script: `dockers/init-vault.sh`
- Initial API payloads: `documentation/grants/`
- Main API port: `8003`
- Vault port: `8200`
- Redis port: `6379`
- Operation Portal MySQL port: `3307`
- Central Ledger MySQL port: `3308`
