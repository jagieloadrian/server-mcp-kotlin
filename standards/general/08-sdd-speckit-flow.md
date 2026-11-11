# Spec-kit flow

Spec-driven development with [spec-kit](https://github.com/github/spec-kit). Branches follow
[02-git-workflow.md](02-git-workflow.md): `feature-<n>/<short-name>`.

## Order

1. `/speckit-specify`
2. `/speckit-clarify`
3. `/speckit-plan`
4. `/speckit-tasks`
5. Up to five rounds of `/speckit-analyze` followed by fixing every finding. Stop early when a round finds
   nothing to fix.
6. `/speckit-implement`
7. Up to five rounds of `/speckit-analyze` followed by fixing every finding, as in step 5.
8. `/speckit-converge`

## Rules

- `clarify` asks the user about every open decision.
- Findings from `analyze` are fixed without asking. A finding that needs a product decision goes to the user.
- `implement` runs on its own: no confirmation prompts, one commit per task, a report at the end. It stops only
  for destructive git operations.
- Constitution changes bump its version with a Sync Impact Report.
