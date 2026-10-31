# Versioned Changes

A summarized overview of all changes, per version of this project.

> Entries will be added in reverse chronological order, so with the most recent at the top.
>
> An active in-development version carries a `-pre` suffix (e.g. `v2.0.0-pre`) on both this heading and
> `gradle.properties`'s `version`. On release, the suffix is replaced with a status tag:
> - `[{{date}}]` - frozen/finalized on {{date}}
> - `[released: {{date}}]` - released to package manager or production on {{date}}
> - `[broken]` - considered broken and not be used

---

## v2.0.0 [released: 2026-10-31]
- Build: **Breaking:** Renamed the project to `template.java` (jar `template.java-<version>.jar`) and changed the group to `com.pellicciotta`.
- Build: **Breaking:** Replaced `com.hinolugi:hinolugi-support:5.1.0` with `com.pellicciotta:support.java:1.0.0`; CI now reads the `PACKAGES_TOKEN` secret.
- Build: Centralized dependency and plugin versions in `gradle/libs.versions.toml`.
- Build: Added Checkstyle for Stroustrup braces and public API Javadoc, and reformatted and documented the sources accordingly.
- DevEx: Added `scripts/devops.py` and ran CI through it; removed `create-github-release.py` in favor of `devops.py release`.
- DevEx: Replaced the release pipeline with the confirmed, CI-gated `doFullRelease` from `support.java`; mainline is now `main`.
- CLI: Fixed `create` failing to find the template root and writing a stale `TODO.md`; it now uses `PLAN.md` and skips the template's task files.
- Docs: Replaced `TODO.md` coordination guidance with `plan` guidance and updated the docs for the renames.

## v1.2.0 [released: 2026-09-11]
- DevEx: Recognize linked worktree Git files in bootstrap repository detection to avoid redundant repository initialization.
- Docs: Correct stale version metadata references in README.md and CLI scaffolding help.
- DevEx: Add GitHub release script to automate quality checks, tagging, publishing, and next-version bump.
- Docs: Update Claude guidance for Python bootstrap, deployment placeholder behavior, and version ownership in Gradle properties.
- Build: Stamp application JAR manifests with UTC build timestamps and document archive reproducibility implications.
- DevEx: Ignore environment secret files and bin compiler output throughout generated projects.
- CLI: Initialize scaffolded projects at `0.1.0-pre` with matching changelogs, task counters, and standard empty task sections.
- DevEx: Normalize Windows and Unix script line endings and protect common binary assets from Git text conversion.
- Docs: Explain common, library, and application Gradle conventions with explicit project mapping and module migration examples.
- Build: Enable caching, all warnings, rich console output, and info logging; provide copyright metadata in Gradle properties.
- DevEx: Share portable VS Code Java build synchronization settings while keeping other editor-local files ignored.
- Build: Configure HTML5 Javadoc with disabled doclint and quiet generation, including shared Java conventions.
- Build: Generate source and Javadoc archives by default and preserve them in shared multi-project conventions.
- Build: `doFullRelease` now atomically bumps `gradle.properties` and opens the next `-pre` CHANGELOG heading after tagging.

## v1.1.0 [released: 2026-09-07]
- Build: Add `com.hinolugi:hinolugi-support` as a default GitHub Packages dependency; requires `read:packages` credentials to build.
- Build: Fix scaffolding to reset the new project's version in `gradle.properties` instead of stale `build.gradle`.
- DevEx: Replace `setup.ps1` with cross-platform `scripts/bootstrap-dev-environment.py` and a scaffolded `scripts/deploy-to-production.py` example.
- Build: Add a `gradlew doFullRelease` task pipeline that verifies, publishes, tags, pushes, and creates the GitHub release.
- Docs: Document an optional maven-publish/GitHub Packages library-publishing pattern in `docs/library-publishing.md`.
- Build: Move `group`, `version`, and `author` out of `build.gradle` into `gradle.properties`.
- Build: Ignore `*.log` files and `log/` directories, and switch `.idea/` to a selective ignore pattern.
- CLI: Add a minimal reference logging implementation (`Log.java`) with `--debug` and `--log-file` options.
- Build: Append `+<git-commit-hash>` build metadata to the jar's `Implementation-Version` by default.
- Build: Stream test output to console during `gradlew test`/`build` via `testLogging.showStandardStreams`.
- Build: Add a Spotless formatting gate so `check` now enforces whitespace and indentation rules by default.
- Build: Fix `gradlew` missing its executable bit, which broke `./gradlew build` on Linux CI runners.
- Build: Upgrade `actions/checkout` and `actions/setup-java` in CI to their current major versions.

## v1.0.0 [2026-08-24]
- Aligned template structure, documentation, and tooling with cross-project development guidelines:
  - Renamed `LICENSE` to `LICENSE.md` and `RELEASES.md` to `CHANGELOG.md` with version status tags.
  - Formatted `TODO.md` to follow milestone-based task tracking (`## Next Milestone`, `### Backlog`) per coordinating work guidelines.
  - Added `work/` to `.gitignore` to support parallel task worktrees.
  - Added mandatory documentation in `docs/`: `docs/requirements.md` (goals, functional and technical requirements), `docs/devops.md` (workflows, task protocol, release procedures), and updated `docs/index.md`.
  - Updated CLI (`Cli.java`) to adhere to CLI guidelines: formatted version output as `{name} v{version} - {copyright}`, added structured multi-line help output with exit codes, and supported `-h`, `--help`, `--version`, and `--verbose`.
  - Updated scaffolding engine (`Scaffold.java`) to emit `CHANGELOG.md`, `LICENSE.md`, and standard `TODO.md` when creating new projects.
  - Expanded test suite (`CliTest.java`) covering all CLI options, exit codes, and scaffolded project validation.

## v0.3.0 [2026-08-19]
- Added `docs/multi-module.md`, documenting the minimal `buildSrc` convention-plugin shape to reach for once a second Gradle module is needed.
- Added a `package-info.java` convention: non-obvious package design decisions are documented in the package's `package-info.java` rather than a README fragment (`src/main/java/myproject/package-info.java` worked example).
- The `version` command's copyright name is no longer hardcoded in `Cli.java`: it's read from the jar manifest's `Implementation-Vendor` stamped from `ext.vendor` in `build.gradle`.

## v0.2.0 [2026-08-19]
- `version` command prints `{project name} {version} - Copyright Giovanni Pellicciotta` instead of just the bare version number. The project name is read from the jar manifest's `Implementation-Title`.

## v0.1.0 [2026-08-19]
- First working version of the template: a minimal Gradle/Java project (`src/main/java` + `src/test/java` layout, JUnit 5, Gradle wrapper with pinned toolchain JDK).
- `Cli.java` gained `help`/`version`/`greet <name>`/`create <project-name> [-o <output-dir>]` subcommands.
- Added `Scaffold.java` to scaffold a new project as a renamed copy of this template.
- The version is stamped into the jar manifest and read back at runtime via `Package.getImplementationVersion()`.
- Added `.github/workflows/ci.yml`, `LICENSE.md`, and `docs/index.md`.
- Fixed self-referential package name assertion in `CliTest`.
