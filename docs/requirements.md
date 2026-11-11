# Requirements

Functional and technical requirements for the `template.java` project.

---

## High-Level Goals
- Provide a standardized, minimal, and fully configured starter project template for Java development using Gradle.
- Enable developers and AI assistants to bootstrap clean, production-ready Java projects adhering to cross-project development guidelines.
- Provide out-of-the-box support for unit testing with JUnit 5, JDK toolchain pinning, manifest-based versioning, and GitHub Actions CI.
- Supply an automated scaffolding tool (`create` subcommand) that clones and renames the template cleanly into new project repositories.

---

## Functional Requirements

### CLI Entry Point
- Provide a subcommand-driven CLI supporting standard commands:
  - `help` (and `--help`, `-h`, `help --verbose`): displays version, description, usage, options, details, and exit codes.
  - `version` (and `--version`): displays `{{name}} v{{version}} - {{copyright}}` in a single line.
  - `greet [{{name}}] [--format text|json]`: runs sample greeting logic and prints text or one JSON document.
  - `create {{project-name}} [-o {{output-dir}}] [--dry-run] [--format text|json]`: scaffolds a renamed project copy or previews planned changes.
- Exit with code 0 on success, code 1 on runtime errors, and code 2 on invalid arguments or options.

### Core Logic Separation
- Maintain clear separation between CLI parsing/IO (`myproject.Cli`), business logic (`myproject.Core`), and template scaffolding machinery (`myproject.Scaffold`).
- Ensure core business logic remains independent of console streams, exit calls, or CLI frameworks for seamless unit testing.

### Project Scaffolding
- Automate repository renaming, package restructuring, and fresh metadata in [CHANGELOG.md](../CHANGELOG.md) and [gradle.properties](../gradle.properties).
- Initialize [gradle.properties](../gradle.properties) at `0.1.0-pre`, with a matching `## v0.1.0-pre` changelog heading.
- Leave out the template's plan store (`PLAN.md`, `.plan/`, `plan.code-workspace`); new projects run `plan init --store branch`.
- Find the template root from files tracked on `main`, so `create` works in checkouts without plan data.
- Prevent accidental overwrite when target directories already exist.

### Logging
- Provide a minimal reference logger in [Log.java](../src/main/java/myproject/Log.java) with formatted lines, level filtering, and optional file output.
- Support `-v`/`--verbose`, `--debug` (implying `--verbose`), `--log-level {{level}}` (overriding `--debug`), and `--log-file {{path}}` as global CLI options.
- Filter standard console streams and `--log-file` output identically against the active log level threshold.
- Route all log entries to `stderr` when `--format json` is selected, keeping `stdout` strictly valid JSON.
- Log startup and completion summaries around operational commands (`greet`, `create`), keeping `help` and `version` unlogged.

---

## Technical Requirements
- Target and compile with the Java 25 toolchain (`java.toolchain`).
- Build with Gradle through the pinned wrapper ([gradlew](../gradlew) / [gradlew.bat](../gradlew.bat)).
- Enable build caching, all warnings, rich console output, and info logging through root Gradle properties.
- Declare author and a literal copyright notice in [gradle.properties](../gradle.properties) for derived projects.
- Test with the JUnit 6 Jupiter framework and the JUnit platform launcher.
- Stamp `Main-Class`, `Implementation-Title`, `Implementation-Version`, `Implementation-Vendor`, `Copyright`, and `Build-Time` into the runnable jar manifest.
- Read the manifest `Copyright` notice at runtime in [Cli.java](../src/main/java/myproject/Cli.java), using a neutral fallback when running from classes.
- Accept that `Build-Time` changes application archives and repeats packaging, while other eligible tasks keep caching.
- Generate source and Javadoc jars during `build`, including subprojects using the documented common convention.
- Generate HTML5 Javadoc with doclint disabled and quiet output; retain failure on Javadoc errors.
- Run Spotless (whitespace) and Checkstyle (Stroustrup braces, braces on every control block, public API Javadoc, [package-info.java](../src/main/java/myproject/package-info.java)) as part of `build`.
- Declare all dependency and plugin versions centrally in [libs.versions.toml](../gradle/libs.versions.toml).
- Use 2-space indentation, UTF-8 encoding, US English, and LF line endings except CRLF for `.bat` and `.cmd` scripts.
- Declare common image, document, Java archive, compiled class, and font formats as binary in [.gitattributes](../.gitattributes).
