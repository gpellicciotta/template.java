<!-- BEGIN plan (managed by plan init) -->
This project tracks its work with plan in `PLAN.md` and `.plan/tasks/`.
Load the `plan-task-usage-guidelines` skill before creating, starting, or changing tasks.
Change `PLAN.md` and task frontmatter only through `plan` commands.
<!-- END plan -->

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is
**[TODO]** Fill in: what this project does, who/what it's for (personal tool, library, service, ...), and any hard
invariants that must not be silently changed — e.g. "read-only, never sends/writes", "no network calls",
"single-user, no auth". State them as deliberate design decisions, not just current behavior, so a future
change doesn't casually cross them.

## Commands
Use `.\gradlew.bat` in Windows PowerShell; use `./gradlew` in POSIX shells.
The Gradle wrapper pins Gradle; `build.gradle`'s `java.toolchain` pins the JDK, which Gradle can provision automatically.
The cross-platform bootstrap requires Python 3 and Git and runs the wrapper's full build.
See [environment prerequisites](docs/devops.md#prerequisites-and-environment) for required GitHub Packages access.

- One-shot bootstrap: `python scripts/bootstrap-dev-environment.py` initializes Git when needed, installs `scripts/requirements.txt` into `.venv`, and runs the full Gradle build.
- Build + test + jar: `.\gradlew.bat build`
- Run the CLI: `.\gradlew.bat run --args="<command> [args]"` or `java -jar build/libs/<name>-<version>.jar <command> [args]` after building.
- Run the test suite only: `.\gradlew.bat test`, or `python scripts/devops.py test myproject.CoreTest` for a selection.
- Test [devops.py](scripts/devops.py) itself: `.venv/Scripts/python -m unittest discover -s scripts/tests`.
- Run every CI validation step (Spotless, Checkstyle, tests, jars): `python scripts/devops.py check`
- Release: `python scripts/devops.py release` runs the `doFullRelease` pipeline (pushes, creates a GitHub release). **Never run it unless the user explicitly asks.**
- **[TODO]** Add any other project-specific commands here.

Projects that deploy a service add a `production` subcommand to `scripts/devops.py`; this template deploys nothing.
See the [development workflows](docs/devops.md#development-workflows) for bootstrap and build details.

## Architecture
**[TODO]** Describe the packages and what each one owns — one paragraph per package is usually enough. Name it, state
its one job, and flag anything a future change needs to respect (e.g. "the only place that parses X", "must
stay pure/dependency-free so it's testable without live credentials").

## Development Guidelines
These apply to every project scaffolded from this template, not just this one:
- Follow the cross-project development guidelines in `dev-guidelines` (and `GEMINI.md` / `CLAUDE.md`).
- Use semver (`MAJOR.MINOR.PATCH`). Projects scaffolded from this template start pre-1.0 (`0.x.y` — see
  `Scaffold.java`'s version reset), so breaking changes are still expected early on but must be called out
  explicitly in `CHANGELOG.md` rather than reading as routine. Once a project reaches `1.0.0`, a breaking
  change requires a major version bump instead.
- Favour simplicity over ingenuity. Keep things as simple as possible for what's actually needed today — don't
  design for hypothetical future requirements. Stay a single Gradle project as long as that's true; only split
  into a multi-project build (with a `buildSrc` convention plugin sharing the toolchain/test setup across
  subprojects) once there's a real second module — see `docs/multi-module.md` for the minimal shape to reach
  for at that point.
- Minimize third-party dependencies. The JDK and JUnit are enough to start; add a dependency only when it
  earns its ongoing maintenance cost.
- Keep prerequisites documented so cloning and running `python scripts/bootstrap-dev-environment.py` produces a passing build with tests.
- Use a `java.toolchain` block (not `sourceCompatibility`/`targetCompatibility`) to pin the JDK version — it
  decouples the JDK compiling the code from whatever JDK happens to run Gradle, and compiles with `--release`
  semantics so use of APIs newer than the target is caught at build time, not at runtime on an older JDK.

## Conventions
- `gradle.properties` defines the authoritative `version`; keep it aligned with the active `-pre` heading in `CHANGELOG.md`.
  Record user-facing changes under that heading and preserve finalized release sections.
  Release automation finalizes the current section and opens the next patch development version; see [release procedures](docs/devops.md#release-process).
- `PLAN.md` (top-level) is the task index; change it and task frontmatter only through the `plan` CLI (see `AGENTS.md`).
- `CHANGELOG.md`, `PLAN.md`, `LICENSE.md`, and `README.md` live at the repo root.
- Core documentation lives under `docs/` (`docs/index.md`, `docs/requirements.md`, `docs/devops.md`, `docs/multi-module.md`).
- Keep `README.md`'s setup/usage/layout sections in sync with the code as it evolves — treat drift there as a
  bug, not a documentation nice-to-have.
- Non-obvious package design decisions (why it's split this way, what a package deliberately does or doesn't
  own) belong in that package's `package-info.java`, not a README fragment — see
  `src/main/java/myproject/package-info.java` for the pattern.
- `Scaffold.java` (`create` subcommand) copies this tree into a new project: keep its marker files, name placeholder
  (`template.java`), and fresh `PLAN.md` content in sync with the repository layout; `CliTest` covers it.
- Style guide: 2-space indentation (never tabs), UTF-8, LF line endings everywhere. Enforced by `.editorconfig`, Spotless, and Checkstyle (Stroustrup braces, public API Javadoc);
  don't let it drift from these numbers.
- The version is stamped into the jar manifest (`build.gradle`'s `jar { manifest { ... } }`) and read back at
  runtime via `Package.getImplementationVersion()` (`Cli.java`'s `version()`, with a `"0.0.0+unknown"` fallback
  for when running from compiled classes rather than a jar, e.g. `.\gradlew.bat run` or the test suite).
  `gradle.properties` supplies the base version; `build.gradle` appends Git commit metadata dynamically when available.
- **[TODO]** Add project-specific invariants/conventions here as they emerge — things a future change must not
  casually break.
