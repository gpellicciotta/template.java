---
id: T0036
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
# T0036: Fix the active changelog entries and give the scaffolded changelog entry an area prefix

## Goals
Fix the active v3.0.0-pre CHANGELOG.md entries to match the change-management entry style.
Replace `**Breaking:**` with `**[breaking]**` directly after the area, and rewrite present-tense entries in past tense.
Cap each entry at 20 words, and link existing repository files instead of using code spans.
Leave finalized sections untouched.
Make the CHANGELOG.md that `create` writes use an `{{area}}:` prefix on its initial entry.

## Task Execution Steps
- [ ] **[Implement]** Extend CliTest.createScaffoldsRenamedProject to require an area prefix on the scaffolded changelog entry.
- [ ] **[Verify]**    Run CliTest and confirm the new assertion fails, capturing the output.
- [ ] **[Implement]** Add the area prefix in Scaffold.writeFreshChangelogMd and fix the active CHANGELOG.md entries.
- [ ] **[Verify]**    Run the tests and the create integration scenario, recording output in validation-proof.md.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.
