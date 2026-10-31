---
id: T0020
owner: '@codex'
needs: []
branch: task/T0020-decide-whether-build-gradle-and-docs-multi-module
worktree: ./work/T0020-decide-whether-build-gradle-and-docs-multi-module
status: completed
started: 2026-09-08
ended: 2026-09-08
---

# T0020: Enable source and Javadoc archives

## Goals

Generate source and Javadoc archives alongside the application's main jar.
Preserve this behavior when projects adopt the documented shared Java convention.

## Task Execution Steps

- [x] **[Read]**      Inspect the build, shared convention example, and packaging documentation.
- [x] **[Decided]**   Enable both archive types in the template and common convention.
- [x] **[Implement]** Configure the archive methods and synchronize packaging documentation.
- [x] **[Verify]**    Run the full build and inspect generated archive contents.
- [x] **[Doc]**       Record validation, review, and completion before integrating the task.

## Execution Log

- [2026-09-08] **[Decided]**
  Use origin/master, the repository's sole mainline, for the authorized claim and integration workflow.
  Claim e66c457 was pushed before worktree creation; command IDs were 562559 and 3a6363.

- [2026-09-08] **[Implement]**
  Enable withJavadocJar() and withSourcesJar() in both Java blocks.
  Update packaging guidance, requirements, and the active changelog.

- [2026-09-08] **[Verify]**
  Run `.\gradlew.bat build --console=plain` in session 62836: all 24 tests, Spotless checks, and packaging passed.
  Javadoc emitted 32 warnings from existing incomplete API comments.

- [2026-09-08] **[Verify]**
  Inspect archives with Python zipfile; all five source files match originals, and Javadoc includes the index and public class pages.
  Command 63c581 also confirmed zero test failures, errors, or skips.

- [2026-09-08] **[Doc]**
  Correct existing numbered headings in edited guides and align task steps after initial lint failures.
  Preserve finalized changelog sections.

- [2026-09-08] **[Verify]**
  Validate all seven edited Markdown files with lint-markdown.py and the task record with lint-taskfile.py.
  Both checks passed after correcting heading and alignment issues; git diff --check passed.

- [2026-09-08] **[Decided]**
  Review tier: autonomous loop, pre-authorized integration after validation.
  Review the complete diff; this Gradle configuration change requires no additional unit tests.

- [2026-09-08] **[Complete]**
  Enable source and Javadoc archives in the template and shared convention; synchronize documentation and changelog.
  Remove T0020 from TODO.md for the single mainline integration commit.
