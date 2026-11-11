# T0031 Validation Proof

## Walkthrough & Validation
### Baseline Failure in CI
CI runs 41516386092 (0b429e3) and 42839368375 (fb1ed73) both failed the same way:

```text
CliTest > createScaffoldsRenamedProject(Path) FAILED
    org.opentest4j.AssertionFailedError: expected exit code 0, stderr was: **[ERROR]** error: Could not locate the template project root (expected build.gradle, PLAN.md, CHANGELOG.md and src/main/java/myproject/ in a parent directory). ...
24 tests completed, 1 failed
```

### Failing Tests Before the Implementation
```text
$ python scripts/devops.py test myproject.ScaffoldTest myproject.CliTest   -> exit 1
> Task :compileTestJava FAILED
C:\Dev-Projects\template.java\src\test\java\myproject\ScaffoldTest.java:23: error: method findTemplateRoot in class Scaffold cannot be applied to given types;
C:\Dev-Projects\template.java\src\test\java\myproject\ScaffoldTest.java:29: error: method findTemplateRoot in class Scaffold cannot be applied to given types;
BUILD FAILED in 2s
```

The compile failure hid the new `CliTest` assertions (no `.plan/`, no `plan.code-workspace`), so they had no separate failing run.

### Tests After the Implementation
```text
$ python scripts/devops.py test myproject.ScaffoldTest myproject.CliTest   -> exit 0
BUILD SUCCESSFUL in 4s
myproject.CliTest 17 0 0       (tests, failures, errors)
myproject.ScaffoldTest 2 0 0

$ python scripts/devops.py check   -> exit 0
[ant:checkstyle] Running Checkstyle 14.3.0 on 5 files
[ant:checkstyle] Running Checkstyle 14.3.0 on 4 files
BUILD SUCCESSFUL in 8s
```

### Integration: Fresh Clone Without Plan Data
Cloned commit `f7f02cd`; the clone has neither `PLAN.md` nor `.plan/`, as in CI.
The first build took `:test` from the local build cache, so the tests were rerun without it.

```text
$ ls PLAN.md .plan
ls: cannot access 'PLAN.md': No such file or directory
ls: cannot access '.plan': No such file or directory
$ .\gradlew.bat test --console=plain --no-build-cache --rerun   -> exit 0
> Task :test
BUILD SUCCESSFUL in 2s
myproject.CliTest 17 0 0
myproject.CoreTest 1 0 0
myproject.LogTest 6 0 0
myproject.ScaffoldTest 2 0 0
```

### Review Rework
The agent review rejected round 1: `plan init --store branch` refused the regular `PLAN.md` that `create` wrote.
A plain `plan init` would put files under the copied `.gitignore` entries `.plan/` and `/PLAN.md`.
`create` now leaves out `PLAN.md` (also avoiding a dangling link), and the version moved to 3.0.0-pre for the breaking changes.

```text
$ python scripts/devops.py test myproject.CliTest -- --rerun   -> exit 1 (new no-PLAN.md assertion)
CliTest > createScaffoldsRenamedProject(Path) FAILED
    	org.opentest4j.AssertionFailedError: expected: <false> but was: <true>
17 tests completed, 1 failed
$ python scripts/devops.py test myproject.CliTest myproject.ScaffoldTest -- --rerun   -> exit 0 (after the fix)
BUILD SUCCESSFUL in 2s
$ .\gradlew.bat run -q --args="create demo-app -o {{scratchpad}}\scaf"   -> exit 0
Created new project at {{scratchpad}}\scaf\demo-app
$ ls -a   (in demo-app)
. .. .editorconfig .gitattributes .github .gitignore AGENTS.md CHANGELOG.md CLAUDE.md LICENSE.md README.md build.gradle config docs gradle gradle.properties gradlew gradlew.bat scripts settings.gradle src
$ git init -q && plan init --store branch   -> exit 0
Initialized plan project in {{scratchpad}}\scaf\demo-app: created the plan branch store and plan.code-workspace
$ git check-ignore PLAN.md .plan
PLAN.md
.plan
$ python scripts/devops.py check   -> exit 0
BUILD SUCCESSFUL in 7s
```

### Review Round 2 Rework
Round 2 rejected stale text: the `create` verbose help, `docs/requirements.md`, and the README's preferred path.

```text
$ python scripts/devops.py test myproject.CliTest.helpVerboseOptionIncludesDetails -- --rerun   -> exit 1 (before)
CliTest > helpVerboseOptionIncludesDetails() FAILED
BUILD FAILED in 3s
$ python scripts/devops.py check   -> exit 0 (after)
BUILD SUCCESSFUL in 8s
```
