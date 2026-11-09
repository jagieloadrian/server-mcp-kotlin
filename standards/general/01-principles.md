# Principles

## Order of choice

Stop at the first step that works:

1. Does it need to exist? Skip speculative needs (YAGNI).
2. Does the codebase already have it? Reuse the helper, type or pattern.
3. Does the standard library do it?
4. Does the platform do it (database constraint, Ktor plugin, Gradle task)?
5. Does an installed dependency do it? Do not add a dependency for a few lines of code.
6. Only then write the minimum code that works.

Understand the problem first. Read the code the change touches and trace the real flow end to end. The smallest
diff in the wrong place is a second bug.

## YAGNI and KISS

- No abstractions without a second implementation or a real seam. `Broker` with Kafka and JetStream behind it
  is justified; an interface with one implementation is not.
- No config for values that never change, no scaffolding "for later", no pagination or caching before a real
  need.
- Boring over clever. Prefer `buildList`, `when` and plain functions over frameworks of your own.

## DRY

- Fold duplicated logic into one place even when each copy is small: one builder for "Unknown user" errors,
  one email lookup, one ingest pipeline shared by all brokers.
- Do not merge code that only looks alike. A shared class that is longer than the copies it replaces is worse.

## SOLID

- Single responsibility: routes route, services hold rules, repositories hold queries, validators validate.
- Depend on the narrowest type that works. Broker implementations only poll and ack; the consumer owns retries,
  metrics and rejections.
- Keep a split proportional to the gain. Splitting a 300-line service only for size is not a goal.

## Bug fixes

- Fix the root cause where all callers meet, not the one path from the report. Grep every caller first.
- A fix that changes behaviour comes with the test that would have caught it.

## Minimal change scope

- Shortest working diff, fewest files. Deletion over addition.
- Keep the existing project structure. Moving packages or renaming modules needs a reason in the commit body.
- Do not change behaviour as a side effect of a refactor. If a refactor would change behaviour (for example
  adding an organisation filter to every query), leave it and record it in PROJECT-STATUS.md.

## No comments

- No `//`, no `/* */`, no `#` comments, no doc comments (KDoc, Javadoc), no `TODO`, no `ponytail:` markers. This
  covers every language, build scripts, YAML, shell, Dockerfiles, workflows and code blocks in Markdown.
- Allowed: `// given`, `// when`, `// then` in tests, shebangs, `<!-- SPECKIT START -->` / `<!-- SPECKIT END -->`
  and the IntelliJ `<!-- Plugin description -->` markers.
- Names explain what code does. The reason for a choice goes into the commit body.

## Language

- Code, identifiers, commit messages, PRs, README and `.docs` are in English.
- homelab-infra documentation is in Polish.
- Explanations live in commit bodies, README, `.docs` or PROJECT-STATUS.md, never in code comments.
