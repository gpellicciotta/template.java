---
id: T0041
owner: ~
needs: [T0034]
branch: —
worktree: —
status: ready
started_at: —
ended_at: —
review: none
preview: none
protocol: adhoc
---
# T0041: Move multi-module.md and library-publishing.md into docs/tutorials/ and update every link to them

## Goals
Move docs/multi-module.md and docs/library-publishing.md into docs/tutorials/ with `git mv`, as the project-setup guidelines place how-to guides there.
Update every link and reference to them: docs/index.md, docs/devops.md, AGENTS.md, README.md, gradle/release.gradle comments and log message, and the cross-link between the two guides.
Leave finalized CHANGELOG.md entries untouched.

Completion check: `lint-markdown check` and `python scripts/devops.py check` exit 0, and `git grep` finds no stale paths outside CHANGELOG.md.

Size estimate: 7 files, 30 lines

### Declared Files
- docs/index.md
- docs/devops.md
- docs/tutorials/multi-module.md
- AGENTS.md
- README.md
- gradle/release.gradle
- CHANGELOG.md

## Task Execution Steps
- [ ] **[Doc]**       Move both guides into docs/tutorials/ with git mv.
- [ ] **[Doc]**       Update every link and reference, and add a Docs entry to CHANGELOG.md.
- [ ] **[Verify]**    Run lint-markdown check, devops.py check, and git grep for stale paths.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.
