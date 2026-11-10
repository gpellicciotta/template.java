---
id: T0031
owner: ~
needs: []
branch: —
worktree: —
status: ready
started_at: —
ended_at: —
review: agent
preview: none
protocol: standard
---
# T0031: Make create work in checkouts without tracked plan data, so the scaffold test passes in CI again

## Goals
Since commit 0b429e3 moved plan data to the `plan` branch, CI checkouts lack `PLAN.md` and `.plan/`.
`Scaffold.findTemplateRoot` requires `PLAN.md`, so `create` and `CliTest.createScaffoldsRenamedProject` fail in CI.
**[Decided]** Use `build.gradle`, `settings.gradle`, and `CHANGELOG.md` as root markers.
**[Decided]** Skip `.plan/` and `plan.code-workspace` when copying; new projects run `plan init` on the fresh `PLAN.md`.

Specifications: [technical-spec.md](T0031-make-create-work-in-checkouts-without-tracked-plan/technical-spec.md).
Completion check: `python scripts/devops.py check` locally and green CI on main.
Size estimate: 6 files, 60 lines

## Task Execution Steps
- [ ] **[Implement]** Test that root discovery needs no `PLAN.md` and that `create` skips `.plan/`.
- [ ] **[Verify]**    Run the scaffold test and record its failure output.
- [ ] **[Implement]** Change the `Scaffold` root markers and copy exclusions.
- [ ] **[Verify]**    Run `python scripts/devops.py check` and confirm CI passes on main.
- [ ] **[Doc]**       Update README, docs/devops.md, CLAUDE.md, and CHANGELOG.md.
- [ ] **[Review]**    Obtain review approval of the finished work before completion.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.
