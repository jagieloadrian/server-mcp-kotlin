# Kotlin persistence

Framework-neutral database rules: [../general/04-backend.md](../general/04-backend.md).

## Exposed

- Exposed (`org.jetbrains.exposed.v1.*`) and Flyway on PostgreSQL.
- Never edit a Flyway migration that has been applied. Add a new one.
- pgjdbc timeouts are passed as strings: `connectTimeout=2`, `socketTimeout=60`. Numeric values are ignored
  silently.
- Blocking JDBC runs through `withContext(Dispatchers.IO)` in one place (`Db.tx`), not in every caller.
