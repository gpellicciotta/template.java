---
id: T0031
owner: "@claude-code"
needs: []
branch: —
worktree: —
status: completed
started_at: "2026-11-10T15:11:57Z"
ended_at: "2026-11-10T15:24:47Z"
review: agent
preview: none
protocol: standard
review_status: approved
review_round: 3
reviewed_by: "@claude-code-reviewer"
completion-commit: 0fe93bce97eaf46a54740272dc937a6b3db4552f
---
# T0031: Make create work in checkouts without tracked plan data, so the scaffold test passes in CI again

## Goals
Since commit 0b429e3 moved plan data to the `plan` branch, CI checkouts lack `PLAN.md` and `.plan/`.
`Scaffold.findTemplateRoot` requires `PLAN.md`, so `create` and `CliTest.createScaffoldsRenamedProject` fail in CI.
**[Decided]** Use `build.gradle`, `settings.gradle`, and `CHANGELOG.md` as root markers.
**[Decided]** Skip `PLAN.md`, `.plan/`, and `plan.code-workspace`; new projects run `plan init --store branch`, matching the copied `.gitignore`.

Specifications: [technical-spec.md](T0031-make-create-work-in-checkouts-without-tracked-plan/technical-spec.md).
Completion check: `python scripts/devops.py check` locally and green CI on main.
Size estimate: 6 files, 60 lines

## Task Execution Steps
- [x] **[Implement]** Test that root discovery needs no `PLAN.md` and that `create` skips `.plan/`.
- [x] **[Verify]**    Run the scaffold test and record its failure output.
- [x] **[Implement]** Change the `Scaffold` root markers and copy exclusions.
- [x] **[Verify]**    Run `python scripts/devops.py check` and confirm CI passes on main.
- [x] **[Doc]**       Update README, docs/devops.md, CLAUDE.md, and CHANGELOG.md.
- [x] **[Review]**    Obtain review approval of the finished work before completion.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.

- [2026-11-10] **[Review]**
  Round 1 requested (mode: agent).

- [2026-11-10] **[Review]**
  Round 1 rejected by @claude-code-reviewer: plan init --store branch refuses the regular PLAN.md that create writes; skip PLAN.md and align docs.

- [2026-11-10] **[Review]**
  Round 2 requested (mode: agent).

- [2026-11-10] **[Review]**
  Round 2 rejected by @claude-code-reviewer: Stale create help text, requirements, and README preferred path still describe PLAN.md.

- [2026-11-10] **[Review]**
  Round 3 requested (mode: agent).

- [2026-11-10] **[Review]**
  Round 3 approved by @claude-code-reviewer.

- [2026-11-10] **[Verify]**
  Recorded the CI failure and failing ScaffoldTest compile, then green check, fresh-clone tests without plan data, and green CI.

- [2026-11-10] **[Implement]**
  Root markers are build.gradle, settings.gradle, CHANGELOG.md; create leaves out PLAN.md, .plan/, and plan.code-workspace.

- [2026-11-10] **[Doc]**
  Updated README, docs, requirements, CLAUDE.md, create help, and CHANGELOG.md; bumped to 3.0.0-pre for breaking changes.

- [2026-11-10] **[Review]**
  Round 1 and 2 rejected the PLAN.md mismatch and stale text; round 3 approved after fixes.

- [2026-11-10] **[Verify]**
  Recorded task outcomes; size note: two undeclared files, Cli.java and requirements.md, added in review rework.
  - Files touched: 10.
  - Lines touched: 116.
  - Reopenings: 0.

- [2026-11-10] **[Complete]**
  Task completed successfully.

