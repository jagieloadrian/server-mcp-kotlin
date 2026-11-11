## Spec-kit flow

`specify` → `clarify` → `plan` → `tasks` → `analyze` → `implement`.

- `implement` runs on its own: no confirmation prompts, one commit per task, a report at the end. It stops only
  for destructive git operations.
- `clarify` and `analyze` ask about every decision and finding.
- Constitution changes bump its version with a Sync Impact Report.

Required whole flow: 
1. /speckit-plan
2. /speckit-tasks
3. in loop 5x
   a. /speckit-analyze
   b. fix all
4. /speckit-implement
5. in loop 5x
   a. /speckit-analyze
   b. fix all
6. /speckit-converge
