---
id: T0024
owner: '@codex'
needs: []
branch: task/T0024-decide-whether-to-update-docs-multi-module-md
worktree: ./work/T0024-decide-whether-to-update-docs-multi-module-md
status: completed
started: 2026-09-09
ended: 2026-09-09
---

# T0024: Document layered Gradle conventions and project mapping

## Goals

Expand the multi-module guide with the approved common, library, and application convention layout.
Explain logical project names, physical directory mapping, module dependencies, and migration from the single-module template.
Validate the documented snippets using the pinned Gradle wrapper before autonomous integration.

## Task Execution Steps

- [x] **[Read]**      Inspect both real project layouts, current documentation, and official Gradle references.
- [x] **[Decided]**   Document sibling library and application conventions inheriting common defaults and explicit client directory mapping.
- [x] **[Implement]** Expand the guide and synchronize its documentation index and changelog.
- [x] **[Verify]**    Build the documented example, run its application and tests, and validate Markdown and task structure.
- [x] **[Doc]**       Record autonomous review and completion, remove T0024 from TODO.md, and integrate the change.

## Execution Log

- [2026-09-09] **[Decided]**
  Use origin/master, the sole remote branch and documented mainline, for the authorized claim and integration.
  Review tier: autonomous loop, pre-authorized after successful validation.

- [2026-09-09] **[Implement]**
  Run `git fetch origin --prune` (eadc43), update T0024 using `apply_patch`, and lint TODO.md (dc9e73).
  Run `git add TODO.md` (1f2453) and `git commit -m "Claimed T0024 to document layered Gradle conventions."` (537274).

- [2026-09-09] **[Implement]**
  Run `git push origin master` (a8e7e5), publishing claim 5aa1756.
  Run `git worktree add ./work/T0024-decide-whether-to-update-docs-multi-module-md -b task/T0024-decide-whether-to-update-docs-multi-module-md` (a0569b).

- [2026-09-09] **[Read]**
  Confirm both hinolugi-auth and hinolugi-counters define all three conventions and map `:clients.java` to `clients/java`.
  Verify mapping and plugin syntax against the official Gradle references linked in the guide.

- [2026-09-09] **[Implement]**
  Update the guide, index, changelog, and task record with `apply_patch` within the assigned worktree.
  Preserve template defaults and distinguish application distributions from direct jar execution.

- [2026-09-09] **[Verify]**
  Create ignored `build/t0024-verify.py` using `apply_patch` and run `python build/t0024-verify.py` (session 63739, Gradle PID 9440).
  Extract all seven documented Gradle snippets into an isolated sample with library and application tests.

- [2026-09-09] **[Verify]**
  Run `.\gradlew.bat build --console=plain --quiet *> build/t0024-template.log` (session 57607, completion 37ce19); the template build passed.
  Markdown lint (5e824b), task-file lint (d2107b), and `git diff --check` (50ccfe) passed.

- [2026-09-09] **[Verify]**
  Sample validation passed (921663): seven snippets, two JUnit tests, six archives with expected contents, correct project mapping, and application startup.
  Template XML assertions (d1d21e) confirmed 24 tests with zero failures, errors, or skips.

- [2026-09-09] **[Verify]**
  Run `python C:\Dev-Projects\dev-guidelines\scripts\lint-markdown.py docs/multi-module.md docs/index.md CHANGELOG.md tasks/T0024-decide-whether-to-update-docs-multi-module-md.md` (5e824b).
  Run `python C:\Dev-Projects\dev-guidelines\scripts\lint-taskfile.py tasks/T0024-decide-whether-to-update-docs-multi-module-md.md` (d2107b); both passed.

- [2026-09-09] **[Decided]**
  Approve autonomous integration after reviewing the final diff (944c48), successful behavioral validation, local links, and LF encoding (5d84de).
  This documentation-only change requires no UI captures; validation fixtures remain ignored build artifacts.

- [2026-09-09] **[Complete]**
  Document common, library, and application conventions, explicit settings mapping, module examples, and migration verification.
  Update the index and changelog, retain this completed task record, and remove T0024 from TODO.md for single-commit integration.
