---
id: T0016
owner: '@claude'
needs: []
branch: task/T0016-add-a-gradlew-dofullrelease-that-make-sure-the
worktree: ./work/T0016-add-a-gradlew-dofullrelease-that-make-sure-the
status: completed
started: 2026-09-06
ended: 2026-09-06
---

# T0016: Add a `gradlew doFullRelease` end-to-end release pipeline

## Goals

Add a `doFullRelease` Gradle task that verifies the CHANGELOG is committed and un-released, publishes to
GitHub Packages if configured, marks the CHANGELOG released, tags, pushes, and creates the GitHub release.

## Task Execution Steps

- [x] **[Read]**      Review `docs/library-publishing.md` and `docs/devops.md`'s manual release process.
- [x] **[Decided]**    Make the publish step conditional so it no-ops on this template's default application shape.
- [x] **[Implement]** Add `gradle/release.gradle` with `checkChangelogUpToDate`/`publishReleaseArtifacts`/`markChangelogReleased`/`tagAndPushRelease`/`createGithubRelease`/`doFullRelease`, applied from `build.gradle`.
- [x] **[Verify]**    Validate task wiring and the changelog-check/regex logic without executing real git push or `gh` side effects.
- [x] **[Doc]**       Update `docs/devops.md` section 4 and `CHANGELOG.md`; record a pre-existing test failure found during validation.

## Execution Log

- [2026-09-06] **[Read]**
  `docs/library-publishing.md` documents `maven-publish`/GitHub Packages as opt-in per derived project; this
  template itself stays an application with no publication configured by default.

- [2026-09-06] **[Decided]**
  `publishReleaseArtifacts` depends on `publishMavenPublicationToGithubPackagesRepository` only if that task
  exists (`tasks.findByName(...)`), else logs a skip message, so `doFullRelease` works unmodified for both
  application and library-shaped derived projects.

- [2026-09-06] **[Implement]**
  Added `gradle/release.gradle` (6 chained tasks) and one `apply from` line in `build.gradle`. Kept steps
  separately runnable so a failed release can resume at the right step.

- [2026-09-06] **[Verify]**
  `gradlew tasks --group release` lists all 6 tasks; `gradlew doFullRelease -m` (dry run) confirms the full
  dependency graph (`build` → `checkChangelogUpToDate` → `publishReleaseArtifacts` → `markChangelogReleased` →
  `tagAndPushRelease` → `createGithubRelease`) with no `publish*` task pulled in, since none is configured here.
  Ran `checkChangelogUpToDate` for real (safe/read-only-equivalent): correctly fails on an uncommitted tree,
  and passes once committed with the `v1.0.1-pre` heading present. Validated the CHANGELOG rewrite/section-
  extraction regexes against a sample string via a throwaway task (removed before committing) rather than
  running `markChangelogReleased`/`tagAndPushRelease`/`createGithubRelease` for real, since those would tag,
  push, and cut an actual GitHub release of this template from a feature branch — well outside this task's
  scope. `gradlew build` surfaced a **pre-existing, unrelated** failure:
  `CliTest.createScaffoldsRenamedProject` still asserts scaffolded `build.gradle` contains
  `version = '0.0.1'`, but `f2ca82e` moved `version`/`group`/`author` into scaffolded `gradle.properties`
  instead. Reproduced identically on `master` outside this worktree, confirming it predates this task. Logged
  as `A0018` in `TODO.md` rather than fixed here (out of scope for T0016). `gradlew build -x test` and
  `spotlessCheck --rerun-tasks` both pass, confirming no other regression from this change.

- [2026-09-06] **[Doc]**
  Rewrote `docs/devops.md` section 4 to document `doFullRelease` and its sub-tasks in place of the old manual
  steps; added a `CHANGELOG.md` bullet under `v1.0.1-pre`.

- [2026-09-06] **[Complete]**
  `gradlew doFullRelease` (via `gradle/release.gradle`) now runs the full verify → publish-if-configured →
  mark-released → tag → push → `gh release create` pipeline; each step also runs standalone. Full end-to-end
  execution against the real repo was deliberately not performed (would cut a real release) - recommend
  dogfooding it for real during A0014's v1.1.0 release.
