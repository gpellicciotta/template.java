## Walkthrough & Validation
### Initial Red-Phase Verification (Before Suite Wiring, ThreadLocal Log, and Leak Guard)
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

Command: `.\gradlew.bat --console=plain test --tests myproject.LogTest.concurrentConfigurationsStayIsolatedAcrossThreads --rerun-tasks` (exit code: `1`, shared `volatile Sink` before `ThreadLocal<Sink>`)
```text
[... 94 lines omitted ...]
LogTest > concurrentConfigurationsStayIsolatedAcrossThreads(Path) FAILED
    java.util.concurrent.ExecutionException: java.nio.file.NoSuchFileException: C:\Users\giova\AppData\Local\Temp\junit-2118650664452642879\thread-0.log
        at java.base/java.util.concurrent.FutureTask.report(FutureTask.java:124)
        at java.base/java.util.concurrent.FutureTask.get(FutureTask.java:193)
        at myproject.LogTest.concurrentConfigurationsStayIsolatedAcrossThreads(LogTest.java:194)

        Caused by:
        java.nio.file.NoSuchFileException: C:\Users\giova\AppData\Local\Temp\junit-2118650664452642879\thread-0.log
            at java.base/sun.nio.fs.WindowsException.translateToIOException(WindowsException.java:85)
            at java.base/sun.nio.fs.WindowsException.rethrowAsIOException(WindowsException.java:103)
            at java.base/sun.nio.fs.WindowsException.rethrowAsIOException(WindowsException.java:108)
            at java.base/sun.nio.fs.WindowsFileSystemProvider.newByteChannel(WindowsFileSystemProvider.java:231)
            at java.base/java.nio.file.Files.newByteChannel(Files.java:357)
            at java.base/java.nio.file.Files.newByteChannel(Files.java:399)
            at java.base/java.nio.file.Files.readAllBytes(Files.java:2973)
            at java.base/java.nio.file.Files.readString(Files.java:3043)
            at myproject.LogTest.lambda$concurrentConfigurationsStayIsolatedAcrossThreads$0(LogTest.java:177)

Gradle Test Executor 23 finished executing tests.

> Task :test FAILED

1 test completed, 1 failed
[... 15 lines omitted ...]
BUILD FAILED in 6s
4 actionable tasks: 4 executed
Watched directory hierarchies: [C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with, C:\Dev-Projects\todos.server]
```

Command: `.\gradlew.bat --console=plain test --tests myproject.CliTest.probeWritesIntoRepository` (exit code: `0`, before adding `StateLeakGuard`)
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

### Round 1 Rework Red-Phase Verification (Home Environment & Stalled Git Subprocess)
Command: `.\gradlew.bat --console=plain test --tests myproject.StateLeakGuardTest --rerun-tasks` (exit code: `1`, before redirecting home environment variables and before file-redirected Git timeout)
```text
[... 56 lines omitted ...]
StateLeakGuardTest > homeEnvironmentVariablesAndInheritedSubprocessStayInsideIsolatedHome(Path) FAILED
    org.opentest4j.AssertionFailedError: Expected environment variable HOME to be set ==> expected: not <null>
        at app//org.junit.jupiter.api.Assertions.assertNotNull(Assertions.java:345)
        at app//myproject.StateLeakGuardTest.homeEnvironmentVariablesAndInheritedSubprocessStayInsideIsolatedHome(StateLeakGuard.java:422)

StateLeakGuardTest > unisolatedHomeEnvironmentVariableFailsBaselineCapture(Path) FAILED
    org.opentest4j.AssertionFailedError: Expected unisolated HOME to fail guard validation ==> Expected org.opentest4j.AssertionFailedError to be thrown, but nothing was thrown.
        at app//org.junit.jupiter.api.Assertions.assertThrows(Assertions.java:3287)
        at app//myproject.StateLeakGuardTest.unisolatedHomeEnvironmentVariableFailsBaselineCapture(StateLeakGuard.java:477)

StateLeakGuardTest > subprocessWritingViaHomeEnvironmentVariablesFailsGuardVerification(Path) PASSED

StateLeakGuardTest > stalledGitCommandFailsWithinBoundAndReapsProcess(Path) FAILED
    java.util.concurrent.TimeoutException: stalledGitCommandFailsWithinBoundAndReapsProcess(java.nio.file.Path) timed out after 2 seconds
        at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:511)
[... 3 lines omitted ...]
        Caused by:
        org.junit.jupiter.api.timeout.PreemptiveTimeoutUtils$ExecutionTimeoutException: Execution timed out in thread junit-timeout-thread-1
            at java.base@25.0.4.1/java.io.FileInputStream.readBytes(Native Method)
            at java.base@25.0.4.1/java.io.FileInputStream.read(FileInputStream.java:281)
            at java.base@25.0.4.1/java.lang.Process$PipeInputStream.read(Process.java:988)
            at java.base@25.0.4.1/java.io.BufferedInputStream.read1(BufferedInputStream.java:328)
            at java.base@25.0.4.1/java.io.BufferedInputStream.read(BufferedInputStream.java:388)
            at java.base@25.0.4.1/java.io.InputStream.readNBytes(InputStream.java:411)
            at java.base@25.0.4.1/java.io.InputStream.readAllBytes(InputStream.java:348)
            at app//myproject.StateLeakGuard$Baseline.readGitPorcelain(StateLeakGuard.java:300)
[... 9 lines omitted ...]
> Task :test FAILED

4 tests completed, 3 failed
[... 15 lines omitted ...]
BUILD FAILED in 8s
4 actionable tasks: 4 executed
Watched directory hierarchies: [C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with, C:\Dev-Projects\todos.server]
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

Command: `.\gradlew.bat --console=plain test --tests myproject.StateLeakGuardTest.probeWritesViaHomeEnvironmentVariable --rerun-tasks` (exit code: `1`, guarded isolated `HOME` write)
```text
[... 88 lines omitted ...]
StateLeakGuardTest > probeWritesViaHomeEnvironmentVariable() FAILED
    org.opentest4j.AssertionFailedError: State leak detected after probeWritesViaHomeEnvironmentVariable():
      user.home (C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\build\tmp\test-user-home) added: probe-env-home-leak.tmp
        at app//myproject.StateLeakGuard$Baseline.verifyFilesystem(StateLeakGuard.java:302)
        at app//myproject.StateLeakGuard.afterEach(StateLeakGuard.java:70)
[... 14 lines omitted ...]
> Task :test FAILED

2 tests completed, 2 failed
[... 15 lines omitted ...]
BUILD FAILED in 5s
4 actionable tasks: 4 executed
Watched directory hierarchies: [C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with, C:\Dev-Projects\todos.server]
```

### Final Green-Phase Verification
Command: `.\gradlew.bat test --console=plain --rerun-tasks` (exit code: `0`)
```text
[... 102 lines omitted ...]
CliTest > greetFormatJsonPrintsSingleDocumentOnStdoutWithLogsOnStderr() PASSED
CliTest > pathOptionsRejectAFollowingFlagAsTheirValue(Path) PASSED
LogTest > levelThresholdFiltersConsoleAndFileIdentically(Path) PASSED
ScaffoldTest > findTemplateRootNeedsNoPlanData(Path) PASSED
[... 39 lines omitted ...]
LogTest > concurrentConfigurationsStayIsolatedAcrossThreads(Path) PASSED
StateLeakGuardTest > unisolatedHomeEnvironmentVariableFailsBaselineCapture(Path) PASSED
StateLeakGuardTest > interruptedGitCommandReapsSubprocessAndPreservesInterruptFlag(Path) PASSED
[... 37 lines omitted ...]
CliTest > createDryRunAndRealExecutionAgreeUnderTurkishLocale(Path) PASSED
CliTest > unknownCommandReturnsTwo() PASSED
CliTest > logLevelWarnSuppressesInfo(Path) PASSED
CliTest > versionOption() PASSED
CliTest > greet() PASSED
StateLeakGuardTest > stalledGitCommandFailsWithinBoundAndReapsProcess(Path) PASSED
StateLeakGuardTest > homeEnvironmentVariablesAndInheritedSubprocessStayInsideIsolatedHome(Path) PASSED
StateLeakGuardTest > subprocessWritingViaHomeEnvironmentVariablesFailsGuardVerification(Path) PASSED

Gradle Test Executor 28 finished executing tests.

> Task :test
Generating HTML test report...
Finished generating test html results (0.013 secs) into: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\build\reports\tests\test
Finished generating test XML results (0.005 secs) into: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\build\test-results\test
Stored cache entry for task ':test' with cache key 6de3fbcd85c59803513cf4a5944a1f43

BUILD SUCCESSFUL in 7s
4 actionable tasks: 4 executed
Watched directory hierarchies: [C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with, C:\Dev-Projects\todos.server]
```

Command: `.\gradlew.bat slowTest --console=plain --rerun-tasks` (exit code: `0`)
```text
[... 72 lines omitted ...]
> Task :slowTest
Custom actions are attached to task ':slowTest'.
Build cache key for task ':slowTest' is ce5218f3429559a6abc762d62165b1ae
Task ':slowTest' is not up-to-date because:
  Executed with '--rerun-tasks'.
[... 2 lines omitted ...]
JarEndToEndTest > greetExitsZeroWithGreetingOnStdoutAndEmptyStderr(Path) PASSED

JarEndToEndTest > versionExitsZeroWithManifestMetadataOnStdoutAndEmptyStderr(Path) PASSED

JarEndToEndTest > helpExitsZeroWithUsageOnStdoutAndEmptyStderr(Path) PASSED

JarEndToEndTest > invalidCommandExitsTwoWithErrorOnStderrAndUsageOnStdout(Path) PASSED

Gradle Test Executor 29 finished executing tests.

> Task :slowTest
Generating HTML test report...
Finished generating test html results (0.007 secs) into: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\build\reports\tests\slowTest
Finished generating test XML results (0.002 secs) into: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\build\test-results\slowTest
Stored cache entry for task ':slowTest' with cache key ce5218f3429559a6abc762d62165b1ae

BUILD SUCCESSFUL in 6s
5 actionable tasks: 5 executed
Watched directory hierarchies: [C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with, C:\Dev-Projects\todos.server]
```

Command: `$env:PATH = "C:\Dev-Projects\template.java\.venv\Scripts;$env:PATH"; C:\Dev-Projects\template.java\.venv\Scripts\python.exe scripts/devops.py check` (exit code: `0`)
```text
Command: C:\Dev-Projects\template.java\.venv\Scripts\lint-markdown.EXE check
Process ID: 82080
All 9 markdown files passed linting.
Command: C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with\gradlew.bat --console=plain build
Process ID: 72648
[... 26 lines omitted ...]
Tasks to be executed: [task ':compileJava', task ':processResources', task ':classes', task ':jar', task ':startScripts', task ':distTar', task ':distZip', task ':javadoc', task ':javadocJar', task ':sourcesJar', task ':assemble', task ':checkstyleMain', task ':compileTestJava', task ':generateTestServices', task ':processTestResources', task ':testClasses', task ':checkstyleTest', task ':slowTest', task ':spotlessGroovyGradle', task ':spotlessGroovyGradleCheck', task ':spotlessJava', task ':spotlessJavaCheck', task ':spotlessCheck', task ':test', task ':check', task ':build']
Tasks that were excluded: []
[... 115 lines omitted ...]
JarEndToEndTest > versionExitsZeroWithManifestMetadataOnStdoutAndEmptyStderr(Path) PASSED
JarEndToEndTest > greetExitsZeroWithGreetingOnStdoutAndEmptyStderr(Path) PASSED
JarEndToEndTest > helpExitsZeroWithUsageOnStdoutAndEmptyStderr(Path) PASSED
JarEndToEndTest > invalidCommandExitsTwoWithErrorOnStderrAndUsageOnStdout(Path) PASSED
[... 48 lines omitted ...]
[ant:checkstyle] Running Checkstyle 14.3.0 on 6 files
[... 161 lines omitted ...]
> Task :check
Skipping task ':check' as it has no actions.
Resolve mutations for :build (Thread[#1254,Execution worker Thread 2,5,main]) started.
:build (Thread[#1254,Execution worker Thread 2,5,main]) started.
> Task :build
Skipping task ':build' as it has no actions.
BUILD SUCCESSFUL in 16s
18 actionable tasks: 10 executed, 8 up-to-date
Watched directory hierarchies: [C:\Dev-Projects\template.java\.plan\work\T0039-split-tests-into-fast-and-slow-suites-with, C:\Dev-Projects\todos.server]
Stopped 1 worker daemon(s).
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
============================= 11 passed in 6.86s ==============================
```
