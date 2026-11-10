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
  - `help` (and `--help`, `-h`): displays tool version, description, usage, and exit codes.
  - `version` (and `--version`): displays `{{name}} v{{version}} - {{copyright}}` in a single line.
  - `greet [name]`: sample business logic action demonstrating CLI-to-core dispatch.
  - `create {{project-name}} [-o {{output-dir}}]`: scaffolds a new project as a renamed copy of the template.
- Exit with code 0 on successful command completion or help/version invocation; exit with non-zero on errors.

### Core Logic Separation
- Maintain clear separation between CLI parsing/IO (`myproject.Cli`), business logic (`myproject.Core`), and template scaffolding machinery (`myproject.Scaffold`).
- Ensure core business logic remains independent of console streams, exit calls, or CLI frameworks for seamless unit testing.

### Project Scaffolding
- Automate repository renaming, package restructuring, and fresh metadata in `CHANGELOG.md` and `gradle.properties`.
- Initialize `gradle.properties` at `0.1.0-pre`, with a matching `## v0.1.0-pre` changelog heading.
- Leave out the template's plan store (`PLAN.md`, `.plan/`, `plan.code-workspace`); new projects run `plan init --store branch`.
- Find the template root from files tracked on `main`, so `create` works in checkouts without plan data.
- Prevent accidental overwrite when target directories already exist.

### Logging
- Provide a minimal reference logging implementation (`myproject.Log`) satisfying the cross-project logging
  guideline: formatted, timestamped log lines, level gating, and an optional append-only log file.
- Support `--debug` (enables `DEBUG`-level messages, discarded by default) and `--log-file {{path}}` (appends
  formatted log lines to `{{path}}` in addition to the console) as global CLI options.
- Log a multi-line startup message (name, version, command, config) and a completion summary (duration, exit
  code) around operational commands (`greet`, `create`); `help`/`version` stay single-line and unlogged.

---

## Technical Requirements
- **Java Version**: Explicitly targeted and compiled with Java 25 toolchain (`java.toolchain`).
- **Build System**: Gradle with pinned Gradle wrapper (`gradlew` / `gradlew.bat`).
- **Build Defaults**: Enable build caching, all warnings, rich console output, and info logging through root Gradle properties.
- **Ownership Metadata**: Declare author and a literal copyright notice in `gradle.properties` for derived projects.
- **Testing**: JUnit 6 Jupiter test framework with JUnit platform launcher.
- **Packaging**: Standard application plugin configuration generating runnable jar archives with manifest attributes (`Main-Class`, `Implementation-Title`, `Implementation-Version`, `Implementation-Vendor`).
- **Build Timestamp**: Stamp the application JAR with `Build-Time` as an ISO-8601 UTC configuration timestamp by default.
- **Reproducibility Tradeoff**: Accept changing application archives and repeated packaging while preserving caching for other eligible tasks.
- **Companion Archives**: Generate source and Javadoc jars during `build`, including subprojects using the documented common convention.
- **Javadoc**: Generate HTML5 documentation with doclint disabled and quiet output; retain failure on Javadoc errors.
- **Static Checks**: Spotless (whitespace) and Checkstyle (Stroustrup braces, braces on every control block, public API Javadoc, `package-info.java`) run as part of `build`.
- **Dependency Versions**: Declare all dependency and plugin versions centrally in [libs.versions.toml](../gradle/libs.versions.toml).
- **Code Standards**: 2-space indentation, UTF-8 encoding, US English, and LF line endings except CRLF for `.bat` and `.cmd` scripts.
- **Binary Assets**: Declare common image, document, Java archive, compiled class, and font formats as binary in Git attributes.
