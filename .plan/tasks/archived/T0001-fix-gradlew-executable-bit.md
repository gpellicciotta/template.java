---
id: T0001
owner: '@claude'
needs: []
branch: n/a
worktree: n/a
status: completed
started: 2026-08-24
ended: 2026-08-24
---

# T0001: Fix gradlew missing its executable bit

## Goals

Fix `gradlew`'s missing executable bit, which breaks `./gradlew build` with `Permission denied` on Linux CI
runners, and confirm `Scaffold.java`'s `create` command preserves the bit for scaffolded projects too.

## Task Execution Steps

- [x] **[Read]**      Confirm gradlew's git file mode causes Permission denied on Linux CI.
- [x] **[Implement]** Set gradlew's git file mode to 100755 via update-index --chmod=+x.
- [x] **[Verify]**    Check Scaffold.java's copyTree preserves POSIX permissions on copy.
- [x] **[Doc]**       Record findings and completion in this task file.

## Execution Log

- [2026-08-24] **[Read]**
  Discovered while aligning `hinolugi-support.java` with this template; its CI hit the identical error.
  - Root cause: gradlew's git mode was 100644, not 100755.

- [2026-08-24] **[Implement]**
  Ran `git update-index --chmod=+x gradlew`, fixing its tracked mode to 100755.

- [2026-08-24] **[Verify]**
  `Scaffold.java`'s `copyTree` uses `COPY_ATTRIBUTES`, which preserves the executable bit on POSIX copies.
  - No `Scaffold.java` change needed.

- [2026-08-24] **[Complete]**
  `gradlew` mode fixed to 100755, confirmed via `git ls-files -s gradlew`; live CI run unverified by this session.
