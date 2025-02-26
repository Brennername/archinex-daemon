# Archinex Configuration

This document describes the configuration options available in `archinex.json`.  This file controls various aspects of Archinex's behavior, including storage, metadata management, journaling, retention policies, caching, and the API.

## Location

The `archinex.json` file should be placed in the following directory:

~/.archinex/config/

Where `~` represents the user's home directory. If the `.archinex` and `config` directories do not exist, Archinex will create them automatically on the first run.

## Format

The `archinex.json` file uses JSON format. It consists of key-value pairs.

## Configuration Options

Here's a detailed explanation of each configuration option:

## Core Components

### `journal.file.path`

*   **Type:** String
*   **Default:** `~/.archinex/data/journal/journal.log`
*   **Description:** Specifies the path to the journal log file. Archinex logs its operations to this file.

### `retention.policy.config.path`

*   **Type:** String
*   **Default:** `~/.archinex/config/retention_policy_config.json`
*   **Description:** Path to the retention policy configuration file. This file defines how long files are kept.

### `cache.config.path`

*   **Type:** String
*   **Default:** `~/.archinex/config/cache_config.json`
*   **Description:** Path to the cache configuration file. This file defines how the cache operates.

## Storage

### `storage.type`

*   **Type:** String
*   **Default:** `local`
*   **Description:** Type of storage system to use. `local` is for local file system storage. Other options (e.g., `s3`, `azure`) could be implemented.

### `storage.local.path`

*   **Type:** String
*   **Default:** `~/.archinex/data/storage`
*   **Description:** Path to the local storage directory. Files will be stored here.

## Metadata Store

### `metadata.store.type`

*   **Type:** String
*   **Default:** `postgres`
*   **Description:** Type of metadata store. `postgres` uses a PostgreSQL database. `memory` is for in-memory storage (for testing only).

### `postgres.jdbcUrl`

*   **Type:** String
*   **Default:** `jdbc:postgresql://localhost:5432/archinex`
*   **Description:** JDBC URL for the PostgreSQL database. **Replace with your actual URL.**

### `postgres.username`

*   **Type:** String
*   **Default:** `archinex_user`
*   **Description:** Username for the PostgreSQL database. **Replace with your actual username.**

### `postgres.password`

*   **Type:** String
*   **Default:** `archinex_password`
*   **Description:** Password for the PostgreSQL database. **Replace with your actual password. Do not hardcode passwords in configuration files in production.**

## Journaling

### `journal.type`

*   **Type:** String
*   **Default:** `file`
*   **Description:** Type of journal to use. `file` writes logs to a file.

## Retention Policy

### `retention.policy.type`

*   **Type:** String
*   **Default:** `basic`
*   **Description:** Type of retention policy. `basic` implements a simple age-based policy.

## Cache

### `cache.type`

*   **Type:** String
*   **Default:** `memory`
*   **Description:** Type of cache to use. `memory` uses an in-memory cache. `redis` is another option.

### `cache.maxSize`

*   **Type:** Integer
*   **Default:** `1024` (MB)
*   **Description:** Maximum cache size in megabytes.

## Planner

### `planner.threadPoolSize`

*   **Type:** Integer
*   **Default:** `4`
*   **Description:** Number of threads in the planner's thread pool.

### `policyEngine.interval`

*   **Type:** Integer
*   **Default:** `3600` (seconds)
*   **Description:** Interval (in seconds) at which the policy engine runs.

## API

### `api.port`

*   **Type:** Integer
*   **Default:** `9876`
*   **Description:** Port for the Archinex API.

### `api.host`

*   **Type:** String
*   **Default:** `localhost`
*   **Description:** Host for the Archinex API.

## Important Notes

*   **Credentials:** Do *not* hardcode sensitive information like database passwords in this file in a production environment. Use environment variables or a secrets management solution.
*   **Placeholders:** The default configuration file contains placeholders. You *must* replace them with your actual values.
*   **Customization:** This file should be customized to match your specific environment and requirements.