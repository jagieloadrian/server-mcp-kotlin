# Kotlin persistence

Framework-neutral database rules: [../general/04-backend.md](../general/04-backend.md).

## Exposed

- Exposed (`org.jetbrains.exposed.v1.*`) and Flyway on PostgreSQL.
- Never edit a Flyway migration that has been applied. Add a new one.
- pgjdbc timeouts are passed as strings: `connectTimeout=2`, `socketTimeout=60`. Numeric values are ignored
  silently.
- Blocking JDBC runs through `withContext(Dispatchers.IO)` in one place (`Db.tx`), not in every caller.
- The DSL for queries. DAO entities only where the code needs entity lifecycle.
- Map rows in one private `ResultRow.toX()` per table.
- Indexes and constraints live in the Flyway SQL migrations (`V<n>__<description>.sql`), not in table objects
  only.
- `batchInsert` / `upsert` instead of a loop of single statements.
- Escape `%`, `_` and the escape character in user input before it goes into a `LIKE` pattern.
- Pass the isolation level explicitly when a transaction needs more than `READ COMMITTED` (check, then write).
