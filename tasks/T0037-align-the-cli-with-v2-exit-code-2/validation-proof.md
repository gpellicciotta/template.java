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

### Review Round 1 Rework
Command: `.\gradlew.bat test --tests myproject.CliTest.invalidPathArgumentsReturnTwoWithoutThrowing --tests myproject.CliTest.createRejectsNonDirectoryOutputAncestorInDryRunAndRealExecution --console=plain` (exit code 1)

```text
[... 72 lines omitted ...]
> Task :test FAILED

CliTest > createRejectsNonDirectoryOutputAncestorInDryRunAndRealExecution(Path) FAILED
    org.opentest4j.AssertionFailedError: expected exit 1 for args: create demo-app --dry-run -o C:\Users\giova\AppData\Local\Temp\junit-8579763967409569379\regular-file.txt ==> expected: <1> but was: <0>
        at app//org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:601)
        at app//myproject.CliTest.createRejectsNonDirectoryOutputAncestorInDryRunAndRealExecution(CliTest.java:406)

CliTest > invalidPathArgumentsReturnTwoWithoutThrowing() FAILED
    java.nio.file.InvalidPathException: Illegal char < > at index 3: bad path
        at java.base/sun.nio.fs.WindowsPathParser.normalize(WindowsPathParser.java:191)
        at java.base/sun.nio.fs.WindowsPathParser.parse(WindowsPathParser.java:142)
        at java.base/sun.nio.fs.WindowsPathParser.parse(WindowsPathParser.java:46)
        at java.base/sun.nio.fs.WindowsPath.parse(WindowsPath.java:92)
        at java.base/sun.nio.fs.WindowsFileSystem.getPath(WindowsFileSystem.java:203)
        at java.base/java.nio.file.Path.of(Path.java:148)
        at myproject.Cli.parseAndDispatch(Cli.java:175)
        at myproject.Cli.run(Cli.java:132)
        at myproject.CliTest.runAndGetExitCode(CliTest.java:34)
        at myproject.CliTest.invalidPathArgumentsReturnTwoWithoutThrowing(CliTest.java:377)

2 tests completed, 2 failed

BUILD FAILED in 6s
3 actionable tasks: 2 executed, 1 up-to-date
```

Command: `python scripts/devops.py check` (exit code 0)

```text
[... 324 lines omitted ...]
Stored cache entry for task ':checkstyleTest' with cache key 5a2294f785fab62bb38fb65d2438a8f4
Resolve mutations for :check (Thread[#312,included builds,5,main]) started.
:check (Thread[#312,included builds,5,main]) started.
> Task :check
Skipping task ':check' as it has no actions.
Resolve mutations for :build (Thread[#312,included builds,5,main]) started.
:build (Thread[#312,included builds,5,main]) started.
> Task :build
Skipping task ':build' as it has no actions.
BUILD SUCCESSFUL in 14s
16 actionable tasks: 12 executed, 4 up-to-date
```

Command: `java -jar build/libs/template.java-3.0.0-pre.jar create review-probe --dry-run -o gradle.properties; exit $LASTEXITCODE` (exit code 1)

```text
Starting template.java v3.0.0-pre+0be6a9b
  command: create review-probe --dry-run -o gradle.properties
  config: debug=false, verbose=false, log-level=INFO, log-file=(none)
**[ERROR]** error: Output parent C:\Dev-Projects\template.java\.plan\work\T0037-align-the-cli-with-v2-exit-code-2\gradle.properties exists and is not a directory.
Completed in 22ms (exit code 1)
```

Command: `java -jar build/libs/template.java-3.0.0-pre.jar create review-probe -o gradle.properties; exit $LASTEXITCODE` (exit code 1)

```text
Starting template.java v3.0.0-pre+0be6a9b
  command: create review-probe -o gradle.properties
  config: debug=false, verbose=false, log-level=INFO, log-file=(none)
**[ERROR]** error: Output parent C:\Dev-Projects\template.java\.plan\work\T0037-align-the-cli-with-v2-exit-code-2\gradle.properties exists and is not a directory.
Completed in 24ms (exit code 1)
```

Command: `java -jar build/libs/template.java-3.0.0-pre.jar greet --log-file 'bad|path'; exit $LASTEXITCODE` (exit code 2)

```text
**[ERROR]** error: invalid --log-file path 'bad|path': Illegal char <|>
```

Command: `java -jar build/libs/template.java-3.0.0-pre.jar create 'bad|name'; exit $LASTEXITCODE` (exit code 2)

```text
**[ERROR]** error: invalid project-name 'bad|name': Illegal char <|>
```

### Review Round 2 Rework
Command: `.\gradlew.bat test --tests myproject.CliTest.createInvalidProjectNameReturnsTwoInDryRunAndRealExecution --console=plain` (exit code 1)

```text
[... 72 lines omitted ...]
> Task :test FAILED

CliTest > createInvalidProjectNameReturnsTwoInDryRunAndRealExecution(Path) FAILED
    org.opentest4j.AssertionFailedError: expected exit 2 for args: create 123 --dry-run -o C:\Users\giova\AppData\Local\Temp\junit-12152055951865405506 ==> expected: <2> but was: <1>
        at app//org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:601)
        at app//myproject.CliTest.createInvalidProjectNameReturnsTwoInDryRunAndRealExecution(CliTest.java:423)

1 test completed, 1 failed

BUILD FAILED in 5s
3 actionable tasks: 2 executed, 1 up-to-date
```

Command: `python scripts/devops.py check` (exit code 0)

```text
[... 140 lines omitted ...]
Stored cache entry for task ':test' with cache key 810cd0301a3200b758df31b848ee00fe
Resolve mutations for :check (Thread[#873,Execution worker Thread 7,5,main]) started.
:check (Thread[#873,Execution worker Thread 7,5,main]) started.
> Task :check
Skipping task ':check' as it has no actions.
Resolve mutations for :build (Thread[#873,Execution worker Thread 7,5,main]) started.
:build (Thread[#873,Execution worker Thread 7,5,main]) started.
> Task :build
Skipping task ':build' as it has no actions.
BUILD SUCCESSFUL in 10s
16 actionable tasks: 7 executed, 9 up-to-date
```

Command: `java -jar build/libs/template.java-3.0.0-pre.jar create 123 --dry-run; exit $LASTEXITCODE` (exit code 2)

```text
**[ERROR]** error: Cannot derive a valid Java package name from '123'.
```

Command: `java -jar build/libs/template.java-3.0.0-pre.jar create 123; exit $LASTEXITCODE` (exit code 2)

```text
**[ERROR]** error: Cannot derive a valid Java package name from '123'.
```

Command: `java -jar build/libs/template.java-3.0.0-pre.jar create review-probe --dry-run -o gradle.properties; exit $LASTEXITCODE` (exit code 1)

```text
Starting template.java v3.0.0-pre+b1c5936
  command: create review-probe --dry-run -o gradle.properties
  config: debug=false, verbose=false, log-level=INFO, log-file=(none)
**[ERROR]** error: Output parent C:\Dev-Projects\template.java\.plan\work\T0037-align-the-cli-with-v2-exit-code-2\gradle.properties exists and is not a directory.
Completed in 21ms (exit code 1)
```
