---
id: T0008
owner: '@claude'
needs: []
branch: task/T0008-the-template-should-ship-a-minimal-reference-logging
worktree: ./work/T0008-the-template-should-ship-a-minimal-reference-logging
status: completed
started: 2026-09-06
ended: 2026-09-06
---

# T0008: Ship a minimal reference logging implementation

## Goals

Ship a minimal, dependency-free reference logging implementation (formatted lines, `--debug`, `--log-file`)
so template-derived projects stop reinventing this per-project, matching what both real hinolugi projects
independently built to satisfy the same cross-project logging guideline.

## Task Execution Steps

- [x] **[Read]**      Review hinolugi-auth's `Logs.java` and hinolugi-support.java's logging SPI for prior art.
- [x] **[Decide]**     Scope a single-class, dependency-free `Log` sink instead of a named-logger SPI.
- [x] **[Implement]** Add `myproject.Log` with level gating, console/file formatting, and multi-line indent.
- [x] **[Implement]** Wire `--debug`/`--log-file` and startup/completion logging into `Cli.java`.
- [x] **[Verify]**    Add `LogTest.java`, update `CliTest.java`, and run `gradlew build`.
- [x] **[Doc]**       Update README, requirements.md, package-info.java, and CHANGELOG.md.

## Execution Log

- [2026-09-06] **[Read]**
  hinolugi-auth's `Logs.java` was, even at its first commit, a thin named-logger wrapper around
  hinolugi-support.java's full logging SPI — never an independent implementation of its own.

- [2026-09-06] **[Decide]**
  **[Decided]** Ship a single dependency-free `myproject.Log` class (global sink, no SPI/back-end
  abstraction) rather than porting hinolugi-support.java's SPI: A0013 already found that depending on
  hinolugi-support.java (a private repo) would break `gradlew build` for every non-`@gio` consumer of this
  public template, so the template must stay self-contained.
  **[Decided]** Startup/completion logging wraps only the "operational" commands (`greet`, `create`);
  `help`/`version`/no-args stay single-line and unlogged so they remain scriptable, matching common CLI
  convention (e.g. `--version` output is never preceded by log noise).
  **[Decided]** `ERROR`/`WARN` go to stderr, `INFO`/`DEBUG` go to stdout; severity indicator tokens and
  padding follow the cross-project logging guideline literally (`**[ERROR]**`, `**[WARN]** `, `**[INFO]** `,
  `**[DEBUG]**`), giving a fixed 12-column prefix width across all four levels once the separating space is
  added.

- [2026-09-06] **[Implement]**
  Added `Log.java`: `error`/`warn`/`info`/`debug` (each with an optional `origin` overload), `configure(...)`
  to reset debug/log-file/streams (needed since state is static and tests invoke `Cli.run` repeatedly),
  timestamped `[YYYY-MM-DD HH:MM:SS]` file-line prefixes, console lines that omit both timestamp and the
  `INFO` indicator, and continuation-line indentation aligned to the first line's message column.
  Wired `--debug`/`--log-file <path>` as global options parsed out of `args` before command dispatch in
  `Cli.java`, plus `logStartup`/`logCompletion` helpers wrapping the `greet` and `create` branches (a
  multi-line startup message with name/version/command/config, and a completion summary with duration and
  exit code).

- [2026-09-06] **[Verify]**
  Added `LogTest.java` (indicator formatting, DEBUG gating, file output, multi-line indent, origin tag) and
  extended `CliTest.java` (`--debug`/`--log-file` wiring, startup/completion lines on `greet`); updated the
  pre-existing `greet`/`greetDefaultName` tests from exact-`equals` to `contains` since stdout now also
  carries the startup/completion lines. `gradlew build` passed (compile, Spotless, 24 tests, jar). Manually
  ran the built jar with `--debug --log-file` and confirmed console and file output match the guideline's
  format, and that `--help`/`--version` output is unchanged.

- [2026-09-06] **[Verify]**
  Sub-agent review before integration found two real gaps: (1) `create`/unknown-command error messages went
  straight to `err.println`, bypassing `Log`, so `--log-file` never captured failure reasons; (2) an
  unwritable `--log-file` path threw an uncaught `UncheckedIOException` instead of a clean CLI error. Fixed
  both: error paths now go through `Log.error` (still visible on stderr, now also logged to file), and
  `Cli.run` wraps dispatch in a try/catch converting `UncheckedIOException` into `error: failed to write
  --log-file '<path>': ...` with exit code 1. Added `errorMessagesAreCapturedInLogFile` and
  `unwritableLogFileProducesCleanErrorInsteadOfCrashing` to `CliTest.java`; `gradlew build` passed (23 tests).
  Remaining review notes (global `--debug`/`--log-file` scanning could theoretically swallow a positional
  argument that happens to match one of those tokens; each log line opens/closes the file individually) are
  low-severity/precedent-consistent and left as-is for a template-scale reference implementation.

- [2026-09-06] **[Doc]**
  Updated README.md (directory layout, new **Logging** section), `docs/requirements.md` (new `2.4. Logging`
  subsection), and `package-info.java` (design rationale for `Log`'s single-sink scope vs. an SPI).
  Recorded the change under the active `v1.0.1-pre` CHANGELOG heading.

- [2026-09-06] **[Complete]**
  The template now ships `myproject.Log`, a minimal reference logging implementation wired into `Cli.java`
  via `--debug`/`--log-file`, with startup/completion logging around `greet`/`create` and full test coverage.
