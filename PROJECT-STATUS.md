# Project status

## Completed (2026-11-04)
- stdio MCP server (Kotlin 2.4.21, `io.modelcontextprotocol:kotlin-sdk-server:0.16.0`, Gradle 9.8.2 wrapper).
- Tools: `list_standards` (name + first line of each `standards/*.md`), `get_standard(name)`.
- Server `instructions` tell the client to check standards before writing/reviewing/committing code.
- Repo sync: `git pull --ff-only` in the standards dir, at most every 5 min, triggered by a tool call. On failure it serves the local copy.
- Seed standards: `standards/engineering.md`, `standards/git.md`.

## Decisions
- stdio, no Ktor/HTTP: one user, local machine. Add HTTP only when a second machine or a team needs it.
- Files are read from disk on every call, so md edits need no rebuild.
- `--ff-only`: never merges and never overwrites local edits. Diverged copy = pull fails, server keeps serving local files.
- `get_standard` matches only existing file names (path traversal impossible).
- `application`/`installDist` instead of the shadow plugin (no extra dependency).
- `kotlin-logging.logStartupMessage=false` set in `main`: otherwise the banner lands on stdout and breaks MCP.

## Usage
```
./gradlew installDist
claude mcp add --scope user standards -- \
  /home/diether18/IdeaProjects/server-mcp-kotlin/build/install/server-mcp-kotlin/bin/server-mcp-kotlin \
  /home/diether18/IdeaProjects/server-mcp-kotlin/standards
```

## Next steps
- Add a git remote (GitHub) and push. Until then the pull logs "no tracking information" and serves local files.
- Write real standards from recurring mistakes (Kotlin style, testing, architecture).
- Register in other clients (IntelliJ AI Assistant/Junie) if needed.
