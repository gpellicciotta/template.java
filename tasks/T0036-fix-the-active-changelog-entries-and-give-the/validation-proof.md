## Walkthrough & Validation

### Red Phase
`CliTest.createScaffoldsRenamedProject` now requires one initial entry matching `- {{area}}: ... Sample App ...`.

Command: `.\gradlew.bat test --tests myproject.CliTest` (exit code 1)

```text
[... 30 lines omitted ...]
> Task :test FAILED
CliTest > createScaffoldsRenamedProject(Path) FAILED
    org.opentest4j.AssertionFailedError: initial entry lacks an area prefix: - Initial release of the Sample App 
project. ==> expected: <true> but was: <false>
        at app//myproject.CliTest.createScaffoldsRenamedProject(CliTest.java:192)
Finished generating test html results (0.027 secs) into: C:\Dev-Projects\template.java\build\reports\tests\test
Finished generating test XML results (0.005 secs) into: C:\Dev-Projects\template.java\build\test-results\test
17 tests completed, 1 failed
FAILURE: Build failed with an exception.
Execution failed for task ':test'.
> There were failing tests. See the report at: 
file:///C:/Dev-Projects/template.java/build/reports/tests/test/index.html
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
BUILD FAILED in 5s
```

### Green Phase
Command: `.\gradlew.bat test --console=plain` (exit code 0)

```text
[... 20 lines omitted ...]
Finished generating test html results (0.007 secs) into: C:\Dev-Projects\template.java\build\reports\tests\test
Finished generating test XML results (0.001 secs) into: C:\Dev-Projects\template.java\build\test-results\test
BUILD SUCCESSFUL in 3s
```

JUnit XML summary of that run:

```text
myproject.CliTest: tests=17 failures=0 errors=0 skipped=0
myproject.CoreTest: tests=1 failures=0 errors=0 skipped=0
myproject.LogTest: tests=6 failures=0 errors=0 skipped=0
myproject.ScaffoldTest: tests=2 failures=0 errors=0 skipped=0
```

### Integration Scenario
Command: `java -jar build/libs/template.java-3.0.0-pre.jar create sample-app -o {{tmp-dir}}`, then `tail -n 4 {{tmp-dir}}/sample-app/CHANGELOG.md` (exit code 0)

```text
Starting template.java v3.0.0-pre+400c26a
  command: create sample-app -o {{tmp-dir}}
  config: debug=false, log-file=(none)
Created new project at {{tmp-dir}}\sample-app
Completed in 258ms (exit code 0)
exit=0

## v0.1.0-pre

- DevEx: Created the Sample App project from the Java template.
```

The `{{tmp-dir}}` placeholder replaces a session scratchpad path.

### Changelog Entries
Every active `v3.0.0-pre` entry is past tense, at most 18 words after the area, and uses `**[breaking]**` after the area.
