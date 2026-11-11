# Java to Kotlin

Applies when converting Java files.

## Workflow

1. Scan the imports. Frameworks found (Spring, Lombok, JPA / Hibernate, Jackson, Dagger / Hilt, RxJava, JUnit,
   Mockito, Retrofit) decide which pitfalls below apply.
2. Convert one file at a time, leaves of the dependency graph first, and fix the imports of its callers.
3. Convert in four passes:
   1. Faithful 1:1 translation.
   2. Nullability and mutability: `?` only where null is possible, `val` unless reassigned.
   3. Collections: Java mutable collections become `MutableList` / `MutableSet` / `MutableMap`; read-only types
      only where Java used immutable ones.
   4. Idioms: properties instead of getters and setters, string templates, `when`, smart casts, elvis.
4. After every pass, behaviour, the public API (names, parameter and return types) and the thrown exceptions stay
   the same. If a pass breaks one of them, redo it from the previous pass.
5. Rename and convert in one commit. A separate rename commit would not compile, and the squash merge joins them
   anyway.
6. Javadoc is not converted to KDoc ([../general/01-principles.md](../general/01-principles.md), No comments).
   A reason worth keeping goes into the commit body.

## Checklist

- [ ] Compiles without new warnings; existing tests pass.
- [ ] Public API, thrown exceptions and side effects unchanged.
- [ ] Every annotation kept, with the right use-site target (`@field:`, `@get:`, `@set:`, `@param:`).
- [ ] Non-null types only where provably non-null. No `!!`.
- [ ] No explicit casts after `is` checks.

## Pitfalls

- Java names that are Kotlin keywords (`when`, `in`, `is`, `object`, `fun`, `val`) need backticks.
- Values from Java that stays Java are platform types: nullable until proven otherwise.
- Java callers of the new Kotlin code: `@JvmStatic` for companion functions called statically, `@JvmField` for
  fields they read, `@JvmOverloads` for default arguments, `@Throws` for checked exceptions they catch.
- Static members become top-level declarations, or a `companion object` when Java callers need `Foo.bar()`.
- Wildcard generics become variance: `? extends T` → `out T`, `? super T` → `in T`.
- An overloaded Java method that takes different functional interfaces makes a lambda ambiguous. Name the
  interface (`Runnable { }`).
- `synchronized` blocks become `synchronized(lock) { }`; inside coroutines use `Mutex` instead.
- try-with-resources → `use { }`, ternary → `if` expression, `instanceof` + cast → `is` with a smart cast,
  anonymous inner classes → lambdas or `object :`.
- `int[]` → `IntArray`, not `Array<Int>`.
- Lombok: `@Data` / `@Value` → a class with properties (a `data class` only for DTOs), `@Builder` → named and
  default arguments, `@Slf4j` → kotlin-logging.
- Jackson needs `jackson-module-kotlin` registered.
- Mockito tests move to MockK when they are converted ([07-testing.md](07-testing.md)).
