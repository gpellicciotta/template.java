# Java Template Project

A minimal starter project to get Java and Gradle development started quickly using the standard `src/main/java` + `src/test/java` layout.

Includes JUnit 6 Jupiter for testing, the Gradle wrapper (pinned Gradle + JDK toolchain versions), a GitHub Actions CI workflow, and full adherence to cross-project development guidelines ([CHANGELOG.md](CHANGELOG.md), `PLAN.md`, [LICENSE.md](LICENSE.md), and [docs/](docs/)).

## Directory Layout
```text
template.java/
  src/
    main/java/myproject/
      Core.java             # business logic
      Cli.java              # entry point (registered in build.gradle's application {mainClass})
      Log.java              # minimal reference logging implementation (console + --log-file)
      Scaffold.java         # `create` subcommand: copies + renames this template into a new project
      package-info.java     # package design notes
    test/java/myproject/
      CoreTest.java
      CliTest.java
      LogTest.java
      ScaffoldTest.java
  docs/                     # project documentation
    index.md                # documentation index
    requirements.md         # functional and technical requirements
    devops.md               # environment, versioning and releasing, workflows, and CI
    tutorials/              # step-by-step how-to guides
      multi-module.md       # multi-module Gradle builds
      library-publishing.md # optional maven-publish setup for GitHub Packages
  build/                    # generated Gradle output; ignored by Git
    classes/                # compiled main and test classes
    generated/              # generated source and header files
    libs/                   # built jar and distribution archives
    reports/                # HTML test and Gradle reports
    resources/              # processed main and test resources
    scripts/                # generated start scripts
    test-results/           # machine-readable test results
    tmp/                    # temporary build state
  .github/
    workflows/
      ci.yml                # devops.py tests, then devops.py check, on push/PR
  gradle/
    libs.versions.toml      # central dependency and plugin versions
    release.gradle          # doFullRelease pipeline
    wrapper/                # Gradle wrapper jar + properties (pinned Gradle version)
    gradle-daemon-jvm.properties  # pinned toolchain JDK version for the Gradle daemon itself
  gradlew / gradlew.bat
  LICENSE.md                # project license
  CHANGELOG.md              # versioned changes in reverse chronological order
  PLAN.md                   # task index link into .plan/, managed through the `plan` CLI
  AGENTS.md                 # instructions for AI coding agents
  .plan/                    # plan store: a worktree of the orphan `plan` branch (not on main)
  build.gradle              # JDK toolchain, JUnit 6, Checkstyle, Spotless, application plugin, jar manifest
  config/checkstyle/        # Checkstyle rules (Stroustrup braces, public API Javadoc, package-info)
  gradle.properties         # group, version, author, copyright, and build settings
  settings.gradle           # rootProject.name
  .gitignore
  .gitattributes            # LF normalization, CRLF for .bat/.cmd, binary file types
  .editorconfig             # indent/charset/line-ending settings
  .lint-markdown.json       # lint-markdown excludes (CHANGELOG.md)
  .vscode/
    settings.json           # shared, portable VS Code Java settings
  scripts/
    bootstrap-dev-environment.py  # one-shot bootstrap: git init, .venv with support-python, gradlew build
    devops.py                     # thin DevopsCli runner: check, lint, test, build, versions, release
    requirements.txt              # pins support-python for devops.py
    tests/                        # unittest tests of devops.py
  CLAUDE.md                 # imports AGENTS.md for Claude Code
```

`build/` is created by Gradle and should not be committed. Delete it with `Remove-Item -Recurse -Force build` when a clean rebuild is needed; the next Gradle command recreates the required directories.

Java uses the standard `src/main/java/{{package-name}}` + `src/test/java/{{package-name}}` layout (the Maven/Gradle default, and what every Java IDE expects out of the box).

## Quick Start
[build.gradle](build.gradle) depends on `com.pellicciotta:support.java`, hosted on the `support.java` GitHub
Packages repo. Set a GitHub PAT with `read:packages` as `github.token`/`github.username` in
`{{user-home}}/.gradle/gradle.properties`, or as the `GITHUB_USERNAME`/`GITHUB_TOKEN` env vars, before building
(see the [prerequisites](docs/devops.md#prerequisites)).

Open a shell in the project root and run:

```shell
python scripts/bootstrap-dev-environment.py
```

This initializes a git repo (if one doesn't already exist), installs [requirements.txt](scripts/requirements.txt) into `.venv`, and runs `gradlew build`, which compiles the code, checks formatting and style, runs the test suite, and produces a jar in `build/libs/`.

## Running Tests
```powershell
.\gradlew.bat test
```

`python scripts/devops.py check` runs every CI validation step (Markdown lint, Spotless, Checkstyle, tests, jar assembly).
`python scripts/devops.py test myproject.CoreTest` narrows the run to one class or method pattern.

## CLI
```powershell
java -jar build/libs/template.java-{{version}}.jar help
java -jar build/libs/template.java-{{version}}.jar version
java -jar build/libs/template.java-{{version}}.jar greet {{name}}
java -jar build/libs/template.java-{{version}}.jar create {{project-name}} [-o {{output-dir}}]
```

`create` scaffolds a new project as a copy of this template at `{{output-dir}}/{{project-name}}` (current directory if `-o` is omitted), automating the renames described below in [Starting a new project from this template](#starting-a-new-project-from-this-template). Run it from within a checkout of this template (i.e. after `python scripts/bootstrap-dev-environment.py` or `gradlew build` in this repo). `create` locates the template root by walking up from wherever its own compiled class was loaded from.

For a development run without building or naming the jar, use `.\gradlew.bat run --args="{{command}} {{args}}"`.

## Logging
`greet` and `create` log a startup message (name, version, command, config) and a completion summary (duration,
exit code) via [Log.java](src/main/java/myproject/Log.java), a minimal reference implementation of the cross-project logging guideline:

```powershell
java -jar build/libs/template.java-{{version}}.jar --debug --log-file app.log greet Gio
```

- `--debug` enables `DEBUG`-level messages (discarded by default).
- `--log-file {{path}}` appends timestamped, formatted log lines to `{{path}}` in addition to the console.
- Console output omits timestamps and the `INFO` severity indicator; `--log-file` entries always carry both.

`help` and `version` stay single-line and scriptable and never emit log lines. Call `Log.error`/`Log.warn`/
`Log.info`/`Log.debug` directly from your own business logic to log more than the CLI's own start/stop events.

## Building the Jar
```powershell
.\gradlew.bat build
```

The jar (with its version stamped into the manifest) lands in `build/libs/`.

## Releasing a Version
Releases use the Gradle `doFullRelease` pipeline, which publishes (when configured), tags, pushes, and creates the
GitHub release, then opens the next `-pre` version. Preview it, then run it:

```shell
python scripts/devops.py release --dry-run
python scripts/devops.py release
```

It needs a clean `main` pushed to `origin` with green CI, and an authenticated `gh` CLI. See
[Making a Release](docs/devops.md#making-a-release).

## Starting a new project from this template
Preferred: run `gradlew run --args="create {{project-name}} [-o {{output-dir}}]"` (see [CLI](#cli) above) — it copies the tree and performs all required renaming automatically.
Then bootstrap the new project and run `plan init --store branch` there to create its plan store.

To do it by hand instead: copy this folder, then rename every occurrence of the placeholder names below:
- [src/main/java/myproject/](src/main/java/myproject/) and [src/test/java/myproject/](src/test/java/myproject/) → `src/main/java/{{package-name}}/` and `src/test/java/{{package-name}}/` (and every `package myproject;`/`import myproject...` line inside them)
- [settings.gradle](settings.gradle)'s `rootProject.name = 'template.java'`
- This README's title, and the `[TODO]` sections of [AGENTS.md](AGENTS.md)
- [CHANGELOG.md](CHANGELOG.md) — replace the version history with your project's initial `## v0.1.0-pre` entry
- [gradle.properties](gradle.properties)'s `version` — reset to `0.1.0-pre`
- `PLAN.md`, `.plan/`, and `plan.code-workspace` — leave them out, then run `plan init --store branch` in the new project

Then follow [Quick Start](#quick-start) above to verify the rename didn't break anything before writing real code.
