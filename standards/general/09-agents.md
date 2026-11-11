# Agents

How to delegate to coding agents, how to set a project up for them, and how to build apps on LLM agents. Rules
for the agent's own behaviour are in [07-ai-assistants.md](07-ai-assistants.md). Examples name Claude Code and
the Claude API; other tools have their own equivalents.

## Delegating to subagents

- The main agent keeps the plan, the decisions and the edits. It delegates only work with a large input and a
  short result:
  - broad search across many files or repos (Claude Code: the `Explore` agent);
  - review of a diff or branch (Claude Code: `/code-review` or a reviewer agent);
  - git operations: status, commit, branch, and push when asked (Claude Code: a `git-committer` agent).
- Everything else runs inline: a known file, one grep, an edit in one or two files.
- A brief stands on its own, because the subagent does not see the conversation: the goal, the paths, what to
  return and what not to touch.
- Ask for the conclusion with `file:line` references, not file contents.
- Independent briefs run in parallel in one turn. Parallel edits in one repo use one git worktree per agent.
- One agent definition per narrow job, with only the tools it needs (Claude Code: `.claude/agents/<name>.md` in
  the project, `~/.claude/agents/` for every project).

## Checking agent output

- A subagent's report is a claim. Read the cited lines and run the tests before acting on it.
- After git work by an agent, check `git log --oneline -5` and `git status`. Report any commit you did not ask
  for.
- A subagent never pushes, merges, deletes or rewrites history unless the brief says so.

## Context

- Keep the main context below 40 %. Compact before that (Claude Code: `/compact`), not at the limit.
- State that must outlive the session goes into PROJECT-STATUS.md, not into the conversation.
- A new session starts from PROJECT-STATUS.md and the code.

## Project setup

- One instructions file in the repo root holds what the agent cannot infer from the code: build and gate
  commands, project-specific bans, unusual structure. Claude Code reads `CLAUDE.md`, most other agents read
  `AGENTS.md`. Keep one and import it from the other (`@AGENTS.md` in `CLAUDE.md`).
- The file links to these standards instead of copying them and holds only what differs.
- Rules that must hold every time are enforced by the tool, not by text (Claude Code: `permissions.deny` for
  destructive commands, a `PostToolUse` hook that runs the formatter).
- Shared agent settings are committed (Claude Code: `.claude/settings.json`), personal ones are not
  (`.claude/settings.local.json`).
- Agent attribution in commits is off, see [02-git-workflow.md](02-git-workflow.md).
- A repeated multi-step workflow becomes a skill (Claude Code: `.claude/skills/<name>/SKILL.md`). No skill for a
  one-off task.
- MCP servers give agents shared knowledge and live systems, for example these standards. Servers for every
  project go in the user scope, project servers in `.mcp.json`. No secrets in `.mcp.json`; pass them through env
  vars.

## Building agent applications

Start with the simplest shape that works: one model call, then a fixed workflow, and an agent loop only when
the steps cannot be known in advance.

### Tools

- One tool per action. Names are `verb_noun` (`list_standards`, `get_standard`).
- The description says when to use the tool and what it returns. Inputs use required fields and enums instead of
  free text (Claude API: `strict: true` with `additionalProperties: false`).
- A failed call returns an error the model can act on: what was wrong and the valid values. It never breaks the
  loop (Claude API: `tool_result` with `is_error: true`; MCP: a result with `isError: true`).
- Results are short text. Large results are paginated or cut with a note that says so.
- Tools are read-only by default. A tool that writes, deletes or spends money has a narrow scope and a human
  approval step.

### MCP servers

- stdio for a local client, Streamable HTTP for a shared server.
- In stdio mode stdout carries only JSON-RPC. Logs go to stderr.
- Over HTTP, check `Host` against an allow-list (DNS rebinding) and require auth outside a trusted network.
- Server `instructions` tell the client when to call the tools.

### Prompts and models

- System prompts live in versioned files, not in strings spread over the code.
- The model ID comes from config.
- Input from users, web pages, files and tool results is data, never instructions. A tool never has more rights
  than the user who started the run.
- Every loop has limits: turns, tokens and wall-clock time.
- Stable content goes first so prompt caching works (Claude API: tools, then system, then messages; check
  `usage.cache_read_input_tokens`).
- Log per call: model, input and output tokens, latency, tool calls. Never log prompts that hold secrets or
  personal data.

### Evals

- A prompt, tool or model change runs the eval set: real inputs with expected results, kept in the repo.
- Deterministic checks first (schema, exact values). An LLM judge only for what code cannot check.
- Unit tests fake the model client and run in the project gate. Evals call the real model and run outside it,
  because they cost money and are not deterministic.
