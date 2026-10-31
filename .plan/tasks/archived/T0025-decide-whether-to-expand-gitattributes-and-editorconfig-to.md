---
id: T0025
owner: '@codex'
needs: []
branch: task/T0025-decide-whether-to-expand-gitattributes-and-editorconfig-to
worktree: ./work/T0025-decide-whether-to-expand-gitattributes-and-editorconfig-to
status: completed
started: 2026-09-09
ended: 2026-09-09
---

# T0025: Normalize scripts and declare binary file types

## Goals

Expand Git attributes and EditorConfig with matching Windows and Unix script line endings.
Protect common binary formats using the existing hinolugi-counters configuration as a reference.
Verify Git conversion behavior and preserve Gradle wrapper handling.

## Task Execution Steps

- [x] **[Read]**      Inspect existing configuration, the reference project, and repository workflow instructions.
- [x] **[Decided]**   Adopt the reference's 14 binary patterns and explicitly configure Windows and Unix scripts.
- [x] **[Implement]** Update Git attributes, EditorConfig, operational documentation, requirements, and changelog.
- [x] **[Verify]**    Validate script normalization, binary preservation, EditorConfig consistency, the build, and Markdown formatting.
- [x] **[Doc]**       Record autonomous review and completion, remove T0025 from TODO.md, and integrate the change.

## Execution Log

- [2026-09-09] **[Decided]**
  Use origin/master, the sole remote branch and documented mainline, for authorized claim and integration.
  Review tier: autonomous loop, pre-authorized after successful validation.

- [2026-09-09] **[Read]**
  The reference declares 14 binary extensions; the task description's count of 15 is inaccurate.
  Preserve wrapper script rules and replace the specific wrapper JAR rule with the general JAR rule.

- [2026-09-09] **[Implement]**
  Run `git fetch origin --prune` (d5d6bc), claim T0025 with `apply_patch`, and lint TODO.md (5bbbde).
  Run `git add TODO.md` (e9e671) and `git commit -m "Claimed T0025 to normalize scripts and declare binary file types."` (aedd4d).

- [2026-09-09] **[Implement]**
  Run `git push origin master` (ba56cc), publishing claim 501dfd1.
  Run `git worktree add ./work/T0025-decide-whether-to-expand-gitattributes-and-editorconfig-to -b task/T0025-decide-whether-to-expand-gitattributes-and-editorconfig-to` (400866).

- [2026-09-09] **[Implement]**
  Update configuration, documentation, changelog, and this task record using `apply_patch` within the assigned worktree.
  Add CRLF for CMD scripts, explicit LF for shell scripts, and binary rules covering images, documents, Java artifacts, and fonts.

- [2026-09-09] **[Verify]**
  Create ignored `build/t0025-verify.py` using `apply_patch` and run `python build/t0025-verify.py` (0b909e, PID 15708).
  Initial checkout validation failed because Git retained existing fixture files; correct the fixture to check out into a fresh directory.

- [2026-09-09] **[Verify]**
  Run `python build/t0025-verify.py *> build/t0025-verify.log` (4f3895); all six script/text and 15 binary fixture round trips passed.
  Verify index normalization, checkout bytes, disabled text/diff/merge attributes, wrapper handling, and EditorConfig settings with `core.autocrlf=true`.

- [2026-09-09] **[Verify]**
  Run `.\gradlew.bat build --console=plain --quiet *> build/t0025-build.log` (bb5ce0); compilation, formatting, tests, and packaging passed.
  XML result inspection (b2e950) confirmed 24 tests with zero failures, errors, or skips.

- [2026-09-09] **[Verify]**
  Run `python C:\Dev-Projects\dev-guidelines\scripts\lint-markdown.py docs/devops.md docs/requirements.md CHANGELOG.md tasks/T0025-decide-whether-to-expand-gitattributes-and-editorconfig-to.md` (7c1718).
  Run `python C:\Dev-Projects\dev-guidelines\scripts\lint-taskfile.py tasks/T0025-decide-whether-to-expand-gitattributes-and-editorconfig-to.md` (ac998a); both passed, along with `git diff --check` (acc9cc).

- [2026-09-09] **[Decided]**
  Approve autonomous integration after reviewing the final configuration and documentation diff (81a9c0) and successful validation.
  This configuration change has no UI; disposable validation fixtures remain ignored build artifacts.

- [2026-09-09] **[Complete]**
  Normalize Windows and Unix scripts and protect 14 common binary formats while preserving Gradle wrapper behavior.
  Update operational documentation, requirements, and changelog; retain this completed task record and remove T0025 from TODO.md.
