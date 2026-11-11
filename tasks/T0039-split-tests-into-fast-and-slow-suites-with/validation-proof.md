## Walkthrough & Validation
### Initial Red-Phase Verification (Before Suite Wiring and Leak Guard)
Command: `.\gradlew.bat --console=plain test --tests myproject.JarEndToEndTest` (exit code: `1`)
```text
[... 72 lines omitted ...]
> Task :test
Build cache key for task ':test' is 5b5d0e7c80f9a740c6aa445b6510b523
Task ':test' is not up-to-date because:
  No history is available.
[... 2 lines omitted ...]
JarEndToEndTest > versionExitsZeroWithManifestMetadataOnStdoutAndEmptyStderr(Path) FAILED
    org.opentest4j.AssertionFailedError: System property 'test.jar.path' must point to the built jar ==> expected: not <null>
        at app//org.junit.jupiter.api.Assertions.assertNotNull(Assertions.java:345)
        at app//myproject.JarEndToEndTest.runJar(JarEndToEndTest.java:50)
        at app//myproject.JarEndToEndTest.versionExitsZeroWithManifestMetadataOnStdoutAndEmptyStderr(JarEndToEndTest.java:107)

JarEndToEndTest > invalidCommandExitsTwoWithErrorOnStderrAndUsageOnStdout(Path) FAILED
    org.opentest4j.AssertionFailedError: System property 'test.jar.path' must point to the built jar ==> expected: not <null>
        at app//org.junit.jupiter.api.Assertions.assertNotNull(Assertions.java:345)
        at app//myproject.JarEndToEndTest.runJar(JarEndToEndTest.java:50)
        at app//myproject.JarEndToEndTest.invalidCommandExitsTwoWithErrorOnStderrAndUsageOnStdout(JarEndToEndTest.java:128)

JarEndToEndTest > helpExitsZeroWithUsageOnStdoutAndEmptyStderr(Path) FAILED
    org.opentest4j.AssertionFailedError: System property 'test.jar.path' must point to the built jar ==> expected: not <null>
        at app//org.junit.jupiter.api.Assertions.assertNotNull(Assertions.java:345)
        at app//myproject.JarEndToEndTest.runJar(JarEndToEndTest.java:50)
        at app//myproject.JarEndToEndTest.helpExitsZeroWithUsageOnStdoutAndEmptyStderr(JarEndToEndTest.java:93)

JarEndToEndTest > greetExitsZeroWithGreetingOnStdoutAndEmptyStderr(Path) FAILED
    org.opentest4j.AssertionFailedError: System property 'test.jar.path' must point to the built jar ==> expected: not <null>
        at app//org.junit.jupiter.api.Assertions.assertNotNull(Assertions.java:345)
        at app//myproject.JarEndToEndTest.runJar(JarEndToEndTest.java:50)
        at app//myproject.JarEndToEndTest.greetExitsZeroWithGreetingOnStdoutAndEmptyStderr(JarEndToEndTest.java:118)

> Task :test FAILED

4 tests completed, 4 failed
[... 14 lines omitted ...]
BUILD FAILED in 6s
3 actionable tasks: 2 executed, 1 from cache
```

Command: `.\gradlew.bat --console=plain test --tests myproject.CliTest.probeWritesIntoRepository` (exit code: `0`)
```text
[... 67 lines omitted ...]
> Task :test
Generating HTML test report...
Finished generating test html results (0.031 secs) into: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\build\reports\tests\test
Finished generating test XML results (0.001 secs) into: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\build\test-results\test
Stored cache entry for task ':test' with cache key d13d82f6f0127b580376c15167a92b5b

BUILD SUCCESSFUL in 4s
3 actionable tasks: 1 executed, 2 up-to-date
```

### Leak Guard Probe Verification (Before Removing Temporary Probes)
Command: `.\gradlew.bat --console=plain test --tests myproject.CliTest.probeWritesIntoRepository` (exit code: `1`)
```text
[... 61 lines omitted ...]
CliTest > probeWritesIntoRepository() FAILED
    org.opentest4j.AssertionFailedError: State leak detected after probeWritesIntoRepository():
      repository (C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with) added: probe-repo-leak.tmp
        at app//myproject.StateLeakGuard$Baseline.verifyFilesystem(StateLeakGuard.java:227)
        at app//myproject.StateLeakGuard.afterEach(StateLeakGuard.java:56)
[... 27 lines omitted ...]
2 tests completed, 2 failed
[... 14 lines omitted ...]
BUILD FAILED in 4s
4 actionable tasks: 2 executed, 2 up-to-date
```

Command: `.\gradlew.bat --console=plain test --tests myproject.CliTest.probeWritesIntoUserHome` (exit code: `1`)
```text
[... 64 lines omitted ...]
CliTest > probeWritesIntoUserHome() FAILED
    org.opentest4j.AssertionFailedError: State leak detected after probeWritesIntoUserHome():
      user.home (C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\build\tmp\test-user-home) added: probe-home-leak.tmp
        at app//myproject.StateLeakGuard$Baseline.verifyFilesystem(StateLeakGuard.java:227)
        at app//myproject.StateLeakGuard.afterEach(StateLeakGuard.java:56)
[... 27 lines omitted ...]
2 tests completed, 2 failed
[... 14 lines omitted ...]
BUILD FAILED in 3s
4 actionable tasks: 1 executed, 3 up-to-date
```

### Final Green-Phase Verification
Command: `.\gradlew.bat test --console=plain --rerun-tasks` (exit code: `0`)
```text
[... 102 lines omitted ...]
CoreTest > greet() PASSED
LogTest > warnAndErrorKeepIndicatorOnConsoleButNoTimestamp() PASSED
CliTest > greetFormatJsonPrintsSingleDocumentOnStdoutWithLogsOnStderr() PASSED
LogTest > allToStderrRoutesInfoAndDebugToErrorStream() PASSED
CliTest > pathOptionsRejectAFollowingFlagAsTheirValue(Path) PASSED
LogTest > levelThresholdFiltersConsoleAndFileIdentically(Path) PASSED
LogTest > infoOmitsIndicatorAndTimestampOnConsole() PASSED
LogTest > originIsRenderedInAngleBrackets(Path) PASSED
ScaffoldTest > findTemplateRootNeedsNoPlanData(Path) PASSED
[... 64 lines omitted ...]
LogTest > concurrentConfigurationsStayIsolatedAcrossThreads(Path) PASSED
CliTest > createRefusesExistingDestination(Path) PASSED
CliTest > createInvalidProjectNameReturnsTwoInDryRunAndRealExecution(Path) PASSED
CliTest > versionOption() PASSED
CliTest > noArgsShowsHelp() PASSED
CliTest > createScaffoldsRenamedProject(Path) PASSED
CliTest > createDryRunAndRealExecutionAgreeUnderTurkishLocale(Path) PASSED

Gradle Test Executor 17 finished executing tests.

> Task :test
Generating HTML test report...
Finished generating test html results (0.015 secs) into: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\build\reports\tests\test
Finished generating test XML results (0.004 secs) into: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\build\test-results\test
Stored cache entry for task ':test' with cache key 35beb889ace3a3eb98050fe3a80c53ed

BUILD SUCCESSFUL in 7s
4 actionable tasks: 4 executed
```

Command: `.\gradlew.bat slowTest --console=plain --rerun-tasks` (exit code: `0`)
```text
[... 99 lines omitted ...]
> Task :slowTest
Custom actions are attached to task ':slowTest'.
Build cache key for task ':slowTest' is 194d82fb8b608c84027a80f9c56d59c8
Task ':slowTest' is not up-to-date because:
  Executed with '--rerun-tasks'.
[... 2 lines omitted ...]
JarEndToEndTest > helpExitsZeroWithUsageOnStdoutAndEmptyStderr(Path) PASSED

JarEndToEndTest > versionExitsZeroWithManifestMetadataOnStdoutAndEmptyStderr(Path) PASSED

JarEndToEndTest > invalidCommandExitsTwoWithErrorOnStderrAndUsageOnStdout(Path) PASSED

JarEndToEndTest > greetExitsZeroWithGreetingOnStdoutAndEmptyStderr(Path) PASSED

Gradle Test Executor 18 finished executing tests.

> Task :slowTest
Generating HTML test report...
Finished generating test html results (0.007 secs) into: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\build\reports\tests\slowTest
Finished generating test XML results (0.001 secs) into: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\build\test-results\slowTest
Stored cache entry for task ':slowTest' with cache key 194d82fb8b608c84027a80f9c56d59c8

BUILD SUCCESSFUL in 5s
5 actionable tasks: 5 executed
```

Command: `$env:PATH = "C:\Dev-Projects\template.java\.venv\Scripts;$env:PATH"; C:\Dev-Projects\template.java\.venv\Scripts\python.exe scripts/devops.py check` (exit code: `0`)
```text
Command: C:\Dev-Projects\template.java\.venv\Scripts\lint-markdown.EXE check
Process ID: 74964
All 9 markdown files passed linting.
Command: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\gradlew.bat --console=plain build
Process ID: 85996
[... 26 lines omitted ...]
Tasks to be executed: [task ':compileJava', task ':processResources', task ':classes', task ':jar', task ':startScripts', task ':distTar', task ':distZip', task ':javadoc', task ':javadocJar', task ':sourcesJar', task ':assemble', task ':checkstyleMain', task ':compileTestJava', task ':generateTestServices', task ':processTestResources', task ':testClasses', task ':checkstyleTest', task ':slowTest', task ':spotlessGroovyGradle', task ':spotlessGroovyGradleCheck', task ':spotlessJava', task ':spotlessJavaCheck', task ':spotlessCheck', task ':test', task ':check', task ':build']
Tasks that were excluded: []
[... 127 lines omitted ...]
JarEndToEndTest > greetExitsZeroWithGreetingOnStdoutAndEmptyStderr(Path) PASSED
JarEndToEndTest > helpExitsZeroWithUsageOnStdoutAndEmptyStderr(Path) PASSED
JarEndToEndTest > versionExitsZeroWithManifestMetadataOnStdoutAndEmptyStderr(Path) PASSED
JarEndToEndTest > invalidCommandExitsTwoWithErrorOnStderrAndUsageOnStdout(Path) PASSED
[... 371 lines omitted ...]
> Task :check
Skipping task ':check' as it has no actions.
Resolve mutations for :build (Thread[#835,Execution worker,5,main]) started.
:build (Thread[#835,Execution worker,5,main]) started.
> Task :build
Skipping task ':build' as it has no actions.
BUILD SUCCESSFUL in 11s
18 actionable tasks: 14 executed, 4 up-to-date
Watched directory hierarchies: [C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with, C:\Dev-Projects\todos.server]
Stopped 2 worker daemon(s).
```

Command: `C:\Dev-Projects\template.java\.venv\Scripts\python.exe -m pytest scripts/tests` (exit code: `0`)
```text
============================= test session starts =============================
platform win32 -- Python 3.12.8, pytest-8.4.2, pluggy-1.7.0
rootdir: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\scripts
configfile: pyproject.toml
plugins: timeout-2.4.0, xdist-3.8.0
timeout: 30.0s
timeout method: thread
timeout func_only: False
created: 8/8 workers
8 workers [11 items]

...........                                                              [100%]
============================= 11 passed in 2.50s ==============================
```
