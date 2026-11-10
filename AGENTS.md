<!-- BEGIN plan (managed by plan init) -->
This project tracks its work with plan in `PLAN.md` and `.plan/tasks/`.
Load the `plan-task-usage-guidelines` skill before creating, starting, or changing tasks.
Change `PLAN.md` and task frontmatter only through `plan` commands.
<!-- END plan -->

# Java Template Project

A minimal Java and Gradle starter project with a sample CLI, reference logging, and a `create` command that scaffolds new projects.
**[TODO]** In a scaffolded project, replace this summary with what the project does, who it serves, and its hard invariants.
State invariants such as "read-only" or "no network calls" as deliberate design decisions, so no change crosses them casually.
See [README.md](README.md), [requirements.md](docs/requirements.md), and [devops.md](docs/devops.md).

## Commands
Use `.\gradlew.bat` in Windows PowerShell and `./gradlew` in POSIX shells.
Builds need GitHub Packages access; see the [environment prerequisites](docs/devops.md#prerequisites).

- Bootstrap: `python scripts/bootstrap-dev-environment.py` initializes Git, fills `.venv` from [requirements.txt](scripts/requirements.txt), and builds.
- Fast tests: `.\gradlew.bat test`, or `python scripts/devops.py test myproject.CoreTest` for a selection.
- Script tests: `.venv/Scripts/python -m unittest discover -s scripts/tests`.
- All checks (Markdown lint, Spotless, Checkstyle, tests, jars): `python scripts/devops.py check`.
- Start the CLI: `.\gradlew.bat run --args="{{command}} {{args}}"`, or `java -jar build/libs/template.java-{{version}}.jar {{command}}` after a build.
- **[TODO]** Add project-specific commands here.

## Architecture
- [myproject](src/main/java/myproject/): the application package; [package-info.java](src/main/java/myproject/package-info.java) records its design.
  - `Cli`: argument parsing, streams, and exit codes; the only class that touches them.
  - `Core`: business logic, free of CLI and IO concerns.
  - `Log`: minimal reference logging sink for console and `--log-file`.
  - `Scaffold`: the template-only `create` command; derived projects may delete it.
- [scripts/](scripts/): the bootstrap and the thin [devops.py](scripts/devops.py) runner on support-python.
- [gradle/](gradle/): the version catalog, the wrapper, and the [release.gradle](gradle/release.gradle) `doFullRelease` pipeline.
- [config/checkstyle/](config/checkstyle/): Checkstyle rules for braces, Javadoc, and [package-info.java](src/main/java/myproject/package-info.java).
- [docs/](docs/): requirements, devops procedures, and the [multi-module](docs/tutorials/multi-module.md) and [library publishing](docs/tutorials/library-publishing.md) guides.
- **[TODO]** Describe each package a scaffolded project adds, one line each, naming what it must keep true.

## Project Conventions
- Style: 2-space indentation, UTF-8, and LF line endings, except CRLF for `.bat` and `.cmd` files.
  [.editorconfig](.editorconfig), Spotless, and Checkstyle enforce it.
- Versions: [gradle.properties](gradle.properties) holds the authoritative `version`, matching the top [CHANGELOG.md](CHANGELOG.md) heading.
  [build.gradle](build.gradle) stamps it into the jar manifest with `+{{commit}}` metadata, and `Cli.version()` reads it back.
- Deployment: this template deploys nothing; derived projects enable `deploy` with `DevopsCli.set_deploy` in [devops.py](scripts/devops.py).
- Scaffolding: keep `Scaffold`'s marker files, the `template.java` name placeholder, and its skipped paths in sync with the layout.
  Markers must be files tracked on `main`, because plan data lives on the `plan` branch and CI lacks it.
  `CliTest` and `ScaffoldTest` cover `create`.
- **[TODO]** Add project-specific invariants as they emerge.

## Boundaries
- Never run `python scripts/devops.py release` or `doFullRelease` unless the user explicitly asks; both push and publish.
- Never edit finalized [CHANGELOG.md](CHANGELOG.md) sections; only their heading status tags may change.
- Never commit GitHub Packages credentials; they belong in the user's Gradle properties or environment.
- Never add `.plan/` or `PLAN.md` to `main`; plan data lives on the `plan` branch.
