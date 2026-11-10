---
id: T0038
owner: ~
needs: []
branch: —
worktree: —
status: ready
started_at: —
ended_at: —
review: none
preview: none
protocol: adhoc
---
# T0038: Write log file timestamps in UTC

## Goals
Make Log.java write log file timestamps in UTC, as the logging guidelines require, instead of local time from `LocalDateTime.now()`.
Keep the `[YYYY-MM-DD HH:MM:SS]` format.
Make the clock injectable or otherwise testable, so a test asserts the UTC value deterministically.

## Task Execution Steps
- [ ] **[Implement]** Add a LogTest case using a fixed non-UTC clock that expects the UTC timestamp.
- [ ] **[Verify]**    Run LogTest and confirm the new case fails, capturing the output.
- [ ] **[Implement]** Format log file timestamps in UTC through an injectable clock.
- [ ] **[Verify]**    Run the tests and the jar integration scenario, recording output in validation-proof.md.
- [ ] **[Doc]**       Add a CHANGELOG.md entry under the active heading.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.
