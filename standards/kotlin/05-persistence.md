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

## JPA and Hibernate

- Never a `data class` for an entity. Use a regular `class`; keep `data class` for DTOs, and keep DTOs separate
  from entities.
- `kotlin("plugin.jpa")` for no-arg constructors, and `allOpen` for `@Entity`, `@MappedSuperclass` and
  `@Embeddable` so lazy proxies work.
- A column is non-null in Kotlin only when construction and the persistence lifecycle make that safe.
- Identity: follow the project's existing convention. Without one, equality by ID with a constant `hashCode`, and a
  nullable generated ID instead of a `0L` sentinel:

```kotlin
@Entity
@Table(name = "orders")
class Order(
    @Column(nullable = false)
    var status: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Order) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String = "Order(id=$id, status=$status)"
}
```

- `toString`, logging and JSON serialization never touch lazy associations.
- Uniqueness needs a database constraint (`@Table(uniqueConstraints = ...)` and the migration). An application
  check before the insert only gives a clean error; the constraint catches the race.
- N+1: prove it with SQL logs or query counts, then fix it with `@EntityGraph`, `JOIN FETCH`, batch fetching or a
  DTO projection, and say how the queries change. No global `FetchType.EAGER`. A collection fetch join does not
  page in the database.
- Keep both sides of a bidirectional association in sync. `orphanRemoval` and cascade remove are different.
- Bulk updates and deletes bypass the persistence context and entity callbacks. Two bag fetches in one query give
  a Cartesian product.
- `@Version` for optimistic locking.
