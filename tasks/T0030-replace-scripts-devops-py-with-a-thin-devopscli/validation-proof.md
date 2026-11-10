# T0030 Validation Proof

## Walkthrough & Validation
### Baseline Before the Change
The old hand-written script, run with the global Python 3.12 interpreter.

```text
$ python scripts/devops.py help        -> exit 0
devops v2.0.1-pre - Copyright (c) Giovanni Pellicciotta
Actions:
  check      run every CI validation step: Spotless, Checkstyle, tests, and jar assembly (gradlew build)
  build      compile, test, and assemble the jar, sources jar, and Javadoc jar (gradlew build)
  test       run the unit tests only, optionally narrowed with --tests
  clean      delete Gradle build output (gradlew clean)
  versions   show gradle.properties and CHANGELOG.md versions and fail when they drift apart
  release    cut a release with the Gradle doFullRelease pipeline (publishes and pushes)
$ python scripts/devops.py versions    -> exit 0
gradle.properties : 2.0.1-pre
CHANGELOG.md top  : 2.0.1-pre
$ python scripts/devops.py release --dry-run   -> exit 0
:doFullRelease SKIPPED
BUILD SUCCESSFUL in 10s
$ C:\Dev-Projects\template.java\gradlew.bat doFullRelease --dry-run --console=plain
```

### Failing Tests Before the Implementation
```text
$ .venv/Scripts/python -m unittest discover -s scripts/tests
ERROR: test_builds_devops_cli_over_gradle_project_at_repo_root
AttributeError: module 'devops_script' has no attribute 'build_cli'
ERROR: test_release_delegates_to_do_full_release_with_confirmation_property
AttributeError: module 'devops_script' has no attribute 'build_cli'
ERROR: test_versions_exits_one_when_versions_drift
AttributeError: module 'devops_script' has no attribute 'build_cli'
ERROR: test_versions_exits_zero_when_versions_agree
AttributeError: module 'devops_script' has no attribute 'build_cli'
FAIL: test_help_lists_standard_actions
AssertionError: Regex didn't match: '(?m)^\\s+lint\\b' not found in 'devops v2.0.1-pre - Copyright ...'
Ran 5 tests in 0.109s
FAILED (failures=1, errors=4)
exit 1

$ .\gradlew.bat test --tests myproject.CliTest.createScaffoldsRenamedProject --console=plain
CliTest > createScaffoldsRenamedProject(Path) FAILED
    org.gradle.internal.exceptions.DefaultMultiCauseException: fresh project metadata (2 failures)
    	org.opentest4j.AssertionFailedError: expected: <true> but was: <false>
    	org.opentest4j.AssertionFailedError: expected: <false> but was: <true>
1 test completed, 1 failed
BUILD FAILED in 4s
exit 1
```

The two scaffold failures are the missing `scripts/requirements.txt` and the copied `.venv`.

### Tests After the Implementation
```text
$ .venv/Scripts/python -m unittest discover -s scripts/tests -v
test_builds_devops_cli_over_gradle_project_at_repo_root ... ok
test_help_lists_standard_actions ... ok
test_release_delegates_to_do_full_release_with_confirmation_property ... ok
test_versions_exits_one_when_versions_drift ... ok
test_versions_exits_zero_when_versions_agree ... ok
Ran 5 tests in 0.306s
OK

$ python scripts/devops.py test myproject.CliTest -- --rerun   -> exit 0
Command: C:\Dev-Projects\template.java\gradlew.bat --console=plain test --tests myproject.CliTest --rerun
BUILD SUCCESSFUL in 3s
build/test-results/test/TEST-myproject.CliTest.xml: tests 17 failures 0 errors 0
```

### Fresh-Clone Bootstrap (Integration)
Cloned commit `fb1ed73` into the session scratchpad and bootstrapped it with the global Python 3.12.

```text
$ python scripts/bootstrap-dev-environment.py   -> exit 0
$ C:\Program Files\Python312\python.exe -m venv {{clone}}\.venv
$ {{clone}}\.venv\Scripts\python.exe -m pip install --quiet --disable-pip-version-check -r {{clone}}\scripts\requirements.txt
$ {{clone}}\gradlew.bat build
BUILD SUCCESSFUL in 3s
$ python scripts/devops.py version   -> exit 0
devops v2.0.1-pre+fb1ed73 - Copyright (c) Giovanni Pellicciotta
$ .venv/Scripts/python -m unittest discover -s scripts/tests
Ran 5 tests in 0.285s
OK
```

The global interpreter keeps its own support-python, which counters.server and todos.server still use:
```text
$ pip show support-python | grep Version
Version: 1.0.0
```

### Completion Check and Actions in the Checkout
```text
$ python scripts/devops.py check   -> exit 0
Command: C:\Dev-Projects\template.java\gradlew.bat --console=plain build
> Task :checkstyleMain UP-TO-DATE
> Task :checkstyleTest UP-TO-DATE
> Task :spotlessCheck UP-TO-DATE
> Task :test FROM-CACHE
BUILD SUCCESSFUL in 2s
$ python scripts/devops.py versions   -> exit 0
Version file: 2.0.1-pre
CHANGELOG.md: 2.0.1-pre
Versions agree.
$ python scripts/devops.py lint   -> exit 0
Command: C:\Dev-Projects\template.java\gradlew.bat --console=plain spotlessCheck checkstyleMain checkstyleTest
BUILD SUCCESSFUL in 1s
$ python scripts/devops.py test myproject.CoreTest   -> exit 0
Command: C:\Dev-Projects\template.java\gradlew.bat --console=plain test --tests myproject.CoreTest
BUILD SUCCESSFUL in 2s
$ python scripts/devops.py release --dry-run   -> exit 0
Command: C:\Dev-Projects\template.java\gradlew.bat --console=plain doFullRelease --dry-run
:doFullRelease SKIPPED
BUILD SUCCESSFUL in 1s
$ echo no | python scripts/devops.py release   -> exit 1
About to release v2.0.1 with the Gradle task 'doFullRelease'.
Type 'release v2.0.1' to continue: **[ERROR]** Release not confirmed; nothing was changed.
**[ERROR]** Finished devops release in 0.0s with status: failed
```

### Review Rework
Agent review approved with four low findings; the CI docs and the missing-venv traceback were fixed.

```text
$ python {{scratchpad}}/novenv/scripts/devops.py help   -> exit 1 (copy without .venv, global support-python 1.0.0)
**[ERROR]** support-python v2 is missing (cannot import name 'DevopsCli' from 'support_python.devops' (...)); run `python scripts/bootstrap-dev-environment.py` first.
$ python scripts/devops.py versions   -> exit 0
Versions agree.
$ .venv/Scripts/python -m unittest discover -s scripts/tests
OK
```
