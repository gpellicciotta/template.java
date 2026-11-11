---
id: T0041
owner: "@claude-code"
needs: []
branch: —
worktree: —
status: completed
started_at: "2026-11-10T18:56:11Z"
ended_at: "2026-11-10T18:58:18Z"
review: none
preview: none
protocol: adhoc
completion-commit: 93eb8260335690f70d9add71bc06936a1930d5ca
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
- [x] **[Doc]**       Move both guides into docs/tutorials/ with git mv.
- [x] **[Doc]**       Update every link and reference, and add a Docs entry to CHANGELOG.md.
- [x] **[Verify]**    Run lint-markdown check, devops.py check, and git grep for stale paths.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.

- [2026-11-10] **[Doc]**
  Moved both guides into docs/tutorials/, fixed their links, and renamed the index section to Tutorials.

- [2026-11-10] **[Verify]**
  Recorded task outcomes.
  - Files touched: 7.
  - Lines touched: 28.
  - Reopenings: 0.

- [2026-11-10] **[Complete]**
  Task completed successfully.

