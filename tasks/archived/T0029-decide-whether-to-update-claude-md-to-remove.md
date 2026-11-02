---
id: T0029
owner: '@codex'
needs: []
branch: task/T0029-decide-whether-to-update-claude-md-to-remove
worktree: ./work/T0029-decide-whether-to-update-claude-md-to-remove
status: completed
started: 2026-09-09
ended: 2026-09-09
---

# T0029: Refresh Claude development guidance

## Goals

Replace stale setup and version instructions with the current Python scripts and Gradle properties metadata.
Explain the deployment placeholder's behavior and link existing operations guidance.
Validate documentation against implementation and preserve an autonomous review record.

## Task Execution Steps

- [x] **[Read]**      Compare Claude guidance with bootstrap, deployment, build, and release implementations.
- [x] **[Decided]**   Use the existing Python scripts and Gradle properties as authoritative command and version references.
- [x] **[Implement]** Update Claude guidance and changelog, and record adjacent findings in the backlog.
- [x] **[Verify]**    Validate documentation links, script behavior, Markdown formatting, and the existing Gradle build.
- [x] **[Doc]**       Record autonomous review and completion, remove the task entry, and integrate the change.

## Execution Log

- [2026-09-09] **[Decided]**
  Use origin/master, the confirmed remote default and sole mainline, for the authorized claim and integration.
  Review tier: autonomous loop with pre-authorized integration after successful validation.

- [2026-09-09] **[Read]**
  Root cause: earlier script and metadata migrations left Claude guidance describing removed setup and obsolete version ownership.
  Confirm bootstrap calls the Gradle build, deployment exits 1, and release automation updates gradle.properties.

- [2026-09-09] **[Implement]**
  Run `git fetch origin --prune` (900d6f), apply the task claim, and run `git add TODO.md` (8b7249).
  Validate the claim using `python C:\Dev-Projects\dev-guidelines\scripts\lint-markdown.py TODO.md` (adb304).

- [2026-09-09] **[Implement]**
  Run `git commit -m "Claimed T0029 to refresh Claude development guidance."` (4d041e), producing c7dfe0c.
  Run `git push origin master` (d6b0d1).

- [2026-09-09] **[Implement]**
  Run `git worktree add ./work/T0029-decide-whether-to-update-claude-md-to-remove -b task/T0029-decide-whether-to-update-claude-md-to-remove` (ffe4de).
  Apply documentation changes exclusively within that worktree using `apply_patch`.

- [2026-09-09] **[Decided]**
  Record remaining README and CLI version drift as A0030, and bootstrap worktree detection as A0031.
  Keep implementation scoped to documentation; validate existing behavior without adding tests that mirror prose.

- [2026-09-09] **[Verify]**
  Run `python scripts/deploy-to-production.py` (cc10ec); confirm placeholder guidance and the documented exit code 1.
  Run `.\gradlew.bat build --console=plain *> t0029-build.log` in session 6971; completion 8e7c90 passed.

- [2026-09-09] **[Verify]**
  Inspect cached test XML using Python assertions (955795): 24 tests, zero failures, errors, or skips.
  Validate all three local documentation links and anchors using Python assertions (e2a8f5), and review the diff (bce5ba).

- [2026-09-09] **[Verify]**
  Run `python C:\Dev-Projects\dev-guidelines\scripts\lint-markdown.py CLAUDE.md CHANGELOG.md TODO.md tasks/T0029-decide-whether-to-update-claude-md-to-remove.md` (ef726e).
  Run `python C:\Dev-Projects\dev-guidelines\scripts\lint-taskfile.py tasks/T0029-decide-whether-to-update-claude-md-to-remove.md` (c34e39); both passed.

- [2026-09-09] **[Decided]**
  Approve integration through the autonomous review tier after successful build, formatting, link, script, and documentation checks.
  Retain adjacent findings as unclaimed backlog tasks; no UI verification applies to this documentation change.

- [2026-09-09] **[Complete]**
  Correct Claude bootstrap, deployment, and version guidance, synchronize the changelog, and remove T0029 from the task index.
  Preserve this completed record with the documentation in one mainline integration commit.
