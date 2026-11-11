---
id: T0036
owner: "@claude-code"
needs: []
branch: —
worktree: —
status: completed
started_at: "2026-11-10T16:33:11Z"
ended_at: "2026-11-10T16:35:59Z"
review: none
preview: none
protocol: adhoc
completion-commit: 68d118e695a9145fcd5a464c432a2f35fed0ba05
---
# T0036: Fix the active changelog entries and give the scaffolded changelog entry an area prefix

## Goals
Fix the active v3.0.0-pre CHANGELOG.md entries to match the change-management entry style.
Replace `**Breaking:**` with `**[breaking]**` directly after the area, and rewrite present-tense entries in past tense.
Cap each entry at 20 words, and link existing repository files instead of using code spans.
Leave finalized sections untouched.
Make the CHANGELOG.md that `create` writes use an `{{area}}:` prefix on its initial entry.

## Task Execution Steps
- [x] **[Implement]** Extend CliTest.createScaffoldsRenamedProject to require an area prefix on the scaffolded changelog entry.
- [x] **[Verify]**    Run CliTest and confirm the new assertion fails, capturing the output.
- [x] **[Implement]** Add the area prefix in Scaffold.writeFreshChangelogMd and fix the active CHANGELOG.md entries.
- [x] **[Verify]**    Run the tests and the create integration scenario, recording output in validation-proof.md.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.

- [2026-11-10] **[Verify]**
  Recorded task outcomes; tests, Checkstyle, Spotless, and the create scenario passed.
  - Files touched: 3.
  - Lines touched: 22.
  - Reopenings: 0.

- [2026-11-10] **[Implement]**
  Extended CliTest.createScaffoldsRenamedProject to require one area-prefixed initial entry; it failed against the old wording.

- [2026-11-10] **[Implement]**
  Made Scaffold write a DevEx-prefixed past-tense entry and rewrote the active CHANGELOG.md entries to the entry style.

- [2026-11-10] **[Complete]**
  Task completed successfully.

