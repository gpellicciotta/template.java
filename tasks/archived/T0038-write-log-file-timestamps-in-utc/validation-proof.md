## Walkthrough & Validation
### Red-Phase Unit Test Failure
Command: `.\gradlew.bat test --tests myproject.LogTest` (exit code: `1`)
```text
[... 115 lines omitted ...]
> Task :test FAILED

LogTest > fileEntriesUseUtcTimestampsRegardlessOfClockZone(Path) FAILED
    org.opentest4j.AssertionFailedError: expected: <[2026-11-11 23:15:30] **[INFO]**  started up
    > but was: <[2026-11-12 01:15:30] **[INFO]**  started up
    >
        at app//org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:1199)
        at app//myproject.LogTest.fileEntriesUseUtcTimestampsRegardlessOfClockZone(LogTest.java:84)
Generating HTML test report...
Finished generating test html results (0.013 secs) into: C:\Dev-Projects\template.java\.plan\work\T0038-write-log-file-timestamps-in-utc\build\reports\tests\test
Finished generating test XML results (0.004 secs) into: C:\Dev-Projects\template.java\.plan\work\T0038-write-log-file-timestamps-in-utc\build\test-results\test
3 actionable tasks: 3 executed
Watched directory hierarchies: [C:\Dev-Projects\template.java\.plan\work\T0038-write-log-file-timestamps-in-utc, C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length]



│···············│ 0% WAITING> IDLE> IDLE
7 tests completed, 1 failed

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':test'.
> There were failing tests. See the report at: file:///C:/Dev-Projects/template.java/.plan/work/T0038-write-log-file-timestamps-in-utc/build/reports/tests/test/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 8s
```

### Green-Phase Unit Test Verification
Command: `.\gradlew.bat test --tests myproject.LogTest` (exit code: `0`)
```text
[... 102 lines omitted ...]
> Task :test
Generating HTML test report...
Finished generating test html results (0.008 secs) into: C:\Dev-Projects\template.java\.plan\work\T0038-write-log-file-timestamps-in-utc\build\reports\tests\test
Finished generating test XML results (0.002 secs) into: C:\Dev-Projects\template.java\.plan\work\T0038-write-log-file-timestamps-in-utc\build\test-results\test
Stored cache entry for task ':test' with cache key 411ffa9ea4ba44ba1f29d7120b62bccb

BUILD SUCCESSFUL in 5s
3 actionable tasks: 2 executed, 1 up-to-date
Watched directory hierarchies: [C:\Dev-Projects\template.java\.plan\work\T0038-write-log-file-timestamps-in-utc, C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length]



│···············│ 0% WAITING> :test> IDLE
```

### Full Check Suite Verification
Command: `python scripts/devops.py check` (exit code: `0`)
```text
Command: C:\Dev-Projects\template.java\.venv\Scripts\lint-markdown.EXE check
Process ID: 61756
All 9 markdown files passed linting.
Command: C:\Dev-Projects\template.java\.plan\work\T0038-write-log-file-timestamps-in-utc\gradlew.bat --console=plain build
Process ID: 27036
[... 511 lines omitted ...]
> Task :check
Skipping task ':check' as it has no actions.
Resolve mutations for :build (Thread[#370,Execution worker,5,main]) started.
:build (Thread[#370,Execution worker,5,main]) started.
> Task :build
Skipping task ':build' as it has no actions.
BUILD SUCCESSFUL in 14s
16 actionable tasks: 12 executed, 1 from cache, 3 up-to-date
Watched directory hierarchies: [C:\Dev-Projects\template.java\.plan\work\T0038-write-log-file-timestamps-in-utc, C:\Dev-Projects\support.java\.plan\work\T0003-answer-a-handler-that-declares-a-content-length]
Stopped 2 worker daemon(s).
```

### JAR Integration Scenario
Command: `java "-Duser.timezone=Europe/Brussels" -jar build/libs/template.java-3.0.0-pre.jar --log-file build/tmp/utc-verify.log greet` (exit code: `0`)
```text
Starting template.java v3.0.0-pre+e5840a7
  command: greet
  config: debug=false, log-file=build\tmp\utc-verify.log
Hello, wereld
Completed in 1ms (exit code 0)
```

Command: `Get-Content build/tmp/utc-verify.log` (exit code: `0`)
```text
[2026-11-11 09:18:15] **[INFO]**  Starting template.java v3.0.0-pre+e5840a7
                                    command: greet
                                    config: debug=false, log-file=build\tmp\utc-verify.log
[2026-11-11 09:18:15] **[INFO]**  Completed in 1ms (exit code 0)
```

Command: `[DateTime]::UtcNow.ToString("u")` (exit code: `0`)
```text
2026-11-11 09:18:29Z
```
