---
id: T0002
owner: '@claude'
needs: []
branch: n/a
worktree: n/a
status: completed
started: 2026-08-24
ended: 2026-08-24
---

# T0002: Upgrade deprecated GitHub Actions versions in ci.yml

## Goals

Bump `actions/checkout` and `actions/setup-java` in `ci.yml` past their deprecated majors, and confirm the
newer majors don't change behavior relevant to this template's build-and-test workflow.

## Task Execution Steps

- [x] **[Read]**      Look up actual current major releases via the GitHub API, not memory.
- [x] **[Read]**      Review both changelogs for breaking changes affecting this workflow.
- [x] **[Implement]** Bump checkout to v7 and setup-java to v6 in ci.yml.
- [x] **[Doc]**       Record version research and completion in this task file.

## Execution Log

- [2026-08-24] **[Read]**
  Discovered while aligning `hinolugi-support.java`; its copied CI printed the same deprecation warnings.
  - GitHub API: checkout is at v7.0.1, setup-java at v6.0.0 (released same day).

- [2026-08-24] **[Read]**
  Checked changelogs: checkout v7's fork-PR change doesn't apply (this workflow only uses push/pull_request).
  - setup-java v6's distribution/ESM changes don't affect this workflow's temurin usage.

- [2026-08-24] **[Implement]**
  Bumped `.github/workflows/ci.yml`: `actions/checkout@v4` to `@v7`, `actions/setup-java@v4` to `@v6`.

- [2026-08-24] **[Complete]**
  ci.yml now pins checkout@v7 and setup-java@v6; live green CI run unverified by this session.
