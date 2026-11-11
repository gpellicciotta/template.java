## Walkthrough & Validation

### Red Phase
Command: `.\gradlew.bat test --tests myproject.CliTest --console=plain` (exit code 1)

```text
[... 95 lines omitted ...]
CliTest > versionOption() FAILED
    org.opentest4j.AssertionFailedError: expected: <true> but was: <false>
        at app//org.junit.jupiter.api.Assertions.assertTrue(Assertions.java:190)
        at app//myproject.CliTest.versionOption(CliTest.java:93)

CliTest > unknownCommandReturnsTwo() FAILED
    org.opentest4j.AssertionFailedError: expected: <2> but was: <1>
        at app//org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:569)
        at app//myproject.CliTest.unknownCommandReturnsTwo(CliTest.java:217)

CliTest > greetFormatJsonPrintsSingleDocumentOnStdoutWithLogsOnStderr() FAILED
    org.opentest4j.AssertionFailedError: expected: <{"name": "Gio \"Jr\"", "greeting": "Hello, Gio \"Jr\""}> but was: <Starting template.java v0.0.0+unknown
      command: greet Gio "Jr" --format json
      config: debug=false, log-file=(none)
    Hello, Gio "Jr"
    Completed in 0ms (exit code 0)>
        at app//org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:1199)
        at app//myproject.CliTest.greetFormatJsonPrintsSingleDocumentOnStdoutWithLogsOnStderr(CliTest.java:168)

> Task :test FAILED

23 tests completed, 12 failed

FAILURE: Build failed with an exception.

BUILD FAILED in 7s
3 actionable tasks: 2 executed, 1 from cache
```

### Green Phase Build, Linting, and Unit Tests
Command: `python scripts/devops.py check` (exit code 0)

```text
[... 102 lines omitted ...]
> Task :spotlessCheck UP-TO-DATE
Skipping task ':spotlessCheck' as it has no actions.
Resolve mutations for :test (Thread[#146,Execution worker Thread 6,5,main]) started.
:test (Thread[#146,Execution worker Thread 6,5,main]) started.
> Task :test UP-TO-DATE
Build cache key for task ':test' is d1054402c3d6dbcd03060348bb923613
Skipping task ':test' as it is up-to-date.
Resolve mutations for :check (Thread[#146,Execution worker Thread 6,5,main]) started.
:check (Thread[#147,Execution worker Thread 7,5,main]) started.
> Task :check UP-TO-DATE
Skipping task ':check' as it has no actions.
Resolve mutations for :build (Thread[#147,Execution worker Thread 7,5,main]) started.
:build (Thread[#147,Execution worker Thread 7,5,main]) started.
> Task :build
Skipping task ':build' as it has no actions.
BUILD SUCCESSFUL in 5s
16 actionable tasks: 4 executed, 12 up-to-date
```

Command: `python scripts/devops.py versions` (exit code 0)

```text
Version file: 3.0.0-pre
CHANGELOG.md: 3.0.0-pre
Versions agree.
```

### Jar Integration Scenarios
Command: `java -jar build/libs/template.java-3.0.0-pre.jar version` (exit code 0)

```text
template.java v3.0.0-pre+79190e9 - Copyright (c) Giovanni Pellicciotta
```

Command: `java -jar build/libs/template.java-3.0.0-pre.jar bogus; exit $LASTEXITCODE` (exit code 2)

```text
**[ERROR]** error: unknown command 'bogus'
template.java v3.0.0-pre+79190e9 - Copyright (c) Giovanni Pellicciotta

A minimal starter project and scaffolding CLI for Java and Gradle development.

usage: template.java <command> [options]

commands:
  help                              Show this usage info
  version                           Show the current version
  greet [name]                      Print a greeting (sample business logic; default: wereld)
  create <project-name> [-o <dir>]  Create a new project as a renamed copy of this template

options:
  -h, --help                        Show this usage info
  --version                         Show the current version
  -v, --verbose                     Emit INFO progress detail (or extended details with help)
  --debug                           Set log threshold to DEBUG and enable --verbose
  --log-file <path>                 Append operational logs to <path>
  --log-level <level>               Set log threshold (DEBUG, INFO, WARN, ERROR; overrides --debug)
  --format <text|json>              Output format for greet and create (default: text)
  -o, --output-dir <dir>            Parent directory for create (default: .)
  --dry-run                         List planned create changes without writing anything

exit codes:
  0  Success (including help and version display)
  1  Operational or runtime execution error
  2  Invalid arguments, unknown command, or invalid options
```

Command: `java -jar build/libs/template.java-3.0.0-pre.jar greet Gio --format json | python -m json.tool` (exit code 0)

```text
Starting template.java v3.0.0-pre+79190e9
  command: greet Gio --format json
  config: debug=false, verbose=false, log-level=INFO, log-file=(none)
Completed in 6ms (exit code 0)
{
    "name": "Gio",
    "greeting": "Hello, Gio"
}
```

Command: `java -jar build/libs/template.java-3.0.0-pre.jar create demo --dry-run -o C:\Users\giova\.gemini\antigravity-cli\brain\92daa5d1-a696-4603-82fb-110a1532a761\scratch\dry-run-check` (exit code 0)

```text
Starting template.java v3.0.0-pre+79190e9
  command: create demo --dry-run -o C:\Users\giova\.gemini\antigravity-cli\brain\92daa5d1-a696-4603-82fb-110a1532a761\scratch\dry-run-check
  config: debug=false, verbose=false, log-level=INFO, log-file=(none)
Dry run: would create new project at C:\Users\giova\.gemini\antigravity-cli\brain\92daa5d1-a696-4603-82fb-110a1532a761\scratch\dry-run-check\demo
  - Copy template from C:\Dev-Projects\template.java\.plan\work\T0037-align-the-cli-with-v2-exit-code-2 to C:\Users\giova\.gemini\antigravity-cli\brain\92daa5d1-a696-4603-82fb-110a1532a761\scratch\dry-run-check\demo
  - Rename package directory myproject to demo under src/main/java and src/test/java
  - Rewrite placeholders (template.java -> demo, myproject -> demo)
  - Write fresh CHANGELOG.md for Demo at v0.1.0-pre
  - Reset gradle.properties version to 0.1.0-pre
Completed in 32ms (exit code 0)
```

Command: `Test-Path C:\Users\giova\.gemini\antigravity-cli\brain\92daa5d1-a696-4603-82fb-110a1532a761\scratch\dry-run-check` (exit code 0)

```text
False
```
