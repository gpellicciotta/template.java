---
id: T0007
owner: '@claude'
needs: []
branch: task/T0007-the-jar-manifest-should-append-a-git-commit
worktree: ./work/T0007-the-jar-manifest-should-append-a-git-commit
status: completed
started: 2026-09-06
ended: 2026-09-06
---

# T0007: Append a git-commit-hash build-metadata suffix to Implementation-Version

## Goals

Stamp `+<short-commit-hash>` build metadata onto the jar's `Implementation-Version` by default, matching the
versioning guideline and the pattern already used by `hinolugi-support.java`.

## Task Execution Steps

- [x] **[Read]**      Review how hinolugi-support.java computes and appends the git commit hash.
- [x] **[Implement]** Add a `gitCommitHash` closure and `implementationVersion` value to build.gradle.
- [x] **[Verify]**    Run `gradlew build` and inspect the built jar's manifest for the suffix.
- [x] **[Doc]**       Record validation and completion in this task file and CHANGELOG.md.

## Execution Log

- [2026-09-06] **[Read]**
  Found the pattern in `hinolugi-support.java/build.gradle`: a `gitCommitHash` closure running
  `git rev-parse --short HEAD`, falling back to `null` outside a git checkout.

- [2026-09-06] **[Implement]**
  Added the same closure and an `implementationVersion` value to build.gradle; wired it into the
  `jar { manifest { ... } }` block in place of the raw `project.version`.

- [2026-09-06] **[Verify]**
  `gradlew build` passed (compile, spotless, tests, jar). Extracted `META-INF/MANIFEST.MF` from the built
  jar and confirmed `Implementation-Version: 1.0.1-pre+a7984d1`, matching HEAD's short hash.

- [2026-09-06] **[Complete]**
  build.gradle now appends `+<short-commit-hash>` to `Implementation-Version` by default; no other
  files needed changes since `Cli.java` reads the manifest value back verbatim.
