# Engineering principles: YAGNI, stdlib first, minimal code

- Follow YAGNI. Build only what the current task needs.
- Prefer standard library and native platform features over new dependencies.
- Keep implementations minimal. Shortest working diff wins.
- No premature abstractions: no interface with one implementation, no factory for one product, no config for a value that never changes.
- Bug fix = root cause in the shared function, not a patch at one caller.
- Non-trivial logic leaves one runnable check behind (one small test).
