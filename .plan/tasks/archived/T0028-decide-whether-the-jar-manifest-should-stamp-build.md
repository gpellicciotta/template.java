---
id: T0028
owner: '@codex'
needs: []
branch: task/T0028-decide-whether-the-jar-manifest-should-stamp-build
worktree: ./work/T0028-decide-whether-the-jar-manifest-should-stamp-build
status: completed
started: 2026-09-09
ended: 2026-09-09
---

# T0028: Stamp JAR build timestamps

## Goals

Implement the approved default Build-Time manifest attribute.
Document timestamp semantics and the accepted reproducibility tradeoff.
Verify packaged metadata, repeated builds, and the existing build checks before autonomous integration.

## Task Execution Steps

- [x] **[Read]**      Inspect manifest configuration, task precedent, and official Gradle caching documentation.
- [x] **[Decided]**   Stamp an ISO-8601 UTC configuration timestamp in the application JAR by default.
- [x] **[Implement]** Update the manifest, operational guidance, requirements, and changelog.
- [x] **[Verify]**    Validate packaged metadata, repeated packaging, compilation reuse, tests, and formatting.
- [x] **[Doc]**       Record autonomous review and completion, remove the task entry, and integrate.

## Execution Log

- [2026-09-09] **[Decided]**
  Use origin/master, the sole mainline, following existing task precedent.
  Apply the authorized autonomous review tier and preserve the pre-existing primary checkout CHANGELOG.md whitespace edit.

- [2026-09-09] **[Implement]**
  Run `git fetch origin` (6a3c5c), then update the assigned task using apply_patch.
  Run `python C:/Dev-Projects/dev-guidelines/scripts/lint-markdown.py TODO.md` (58edd1) and `git add TODO.md` (51238c).

- [2026-09-09] **[Implement]**
  Run `git commit -m "Claimed T0028 to stamp JAR build timestamps."` (8bb760).
  Run `git push origin master` (9607cd); claim commit 9e96cc1 succeeded.

- [2026-09-09] **[Implement]**
  Run `git worktree add ./work/T0028-decide-whether-the-jar-manifest-should-stamp-build -b task/T0028-decide-whether-the-jar-manifest-should-stamp-build` (ffbba8).
  Apply task changes only within that worktree.

- [2026-09-09] **[Read]**
  Confirm hinolugi-support.java stamps Build-Time; other example projects were unavailable at the expected paths.
  Consult Gradle's [incremental build](https://docs.gradle.org/current/userguide/incremental_build.html) and [build cache](https://docs.gradle.org/current/userguide/build_cache.html) documentation.

- [2026-09-09] **[Decided]**
  Accept changed JAR bytes and packaging inputs; Gradle disables JAR caching by default.
  Use UTC instead of locale-dependent timestamps and preserve existing manifest identity attributes.

- [2026-09-09] **[Verify]**
  Run `.\gradlew.bat build --console=plain --init-script build/t0028-verify.gradle *> build/t0028-build.log` (session 60062, daemon 36720, completion e40053).
  Build passed in 37 seconds, including cached tests, formatting, packaging, and live timestamp and manifest identity assertions.

- [2026-09-09] **[Verify]**
  Run `python build/t0028-inspect.py` (06a3d2); verify three archives and 24 tests with no failures, errors, or skips.
  Confirm source and Javadoc manifests omit Build-Time.

- [2026-09-09] **[Verify]**
  Run `.\gradlew.bat jar --console=plain --init-script build/t0028-verify.gradle *> build/t0028-repeat.log` (session 66784, completion 59ca6c).
  Packaging passed with a newer timestamp and compilation UP-TO-DATE; the manifest change correctly invalidated the JAR.

- [2026-09-09] **[Verify]**
  Run `python build/t0028-inspect.py` again (0e9b1e).
  Confirm changed archive bytes, identical packaged classes, preserved identity attributes, and all test reports passing.

- [2026-09-09] **[Verify]**
  Run `python C:/Dev-Projects/dev-guidelines/scripts/lint-markdown.py CHANGELOG.md docs/devops.md docs/requirements.md tasks/T0028-decide-whether-the-jar-manifest-should-stamp-build.md` (02eda7, completion bfddbc).
  Run `python C:/Dev-Projects/dev-guidelines/scripts/lint-taskfile.py tasks/T0028-decide-whether-the-jar-manifest-should-stamp-build.md` (0581eb) and `git diff --check` (146205); all passed.

- [2026-09-09] **[Decided]**
  Approve autonomous integration after reviewing the scoped diff, packaged metadata, repeated build behavior, and successful checks.
  Keep temporary behavioral assertions and logs as ignored build artifacts; this configuration change introduces no application logic.

- [2026-09-09] **[Complete]**
  Stamp application JARs with UTC Build-Time metadata and document the accepted reproducibility tradeoff.
  Update requirements and changelog, remove T0028 from TODO.md, and retain this task record for authorized mainline integration.
