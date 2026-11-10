## Walkthrough & Validation
Red phase: the four new devops script tests fail before devops.py runs lint-markdown and before .lint-markdown.json exists.

Command: `.venv/Scripts/python -m unittest discover -s scripts/tests` (exit code 1)

```text
.FF.EF...
======================================================================
ERROR: test_lint_markdown_excludes_changelog_and_keeps_default_excludes (test_devops.DevopsScriptTest.test_lint_markdown_excludes_changelog_and_keeps_default_excludes)
[... 13 lines omitted ...]
======================================================================
FAIL: test_check_runs_lint_markdown_before_gradle_build (test_devops.DevopsScriptTest.test_check_runs_lint_markdown_before_gradle_build)
----------------------------------------------------------------------
Traceback (most recent call last):
  File "c:\Dev-Projects\template.java\scripts\tests\test_devops.py", line 76, in test_check_runs_lint_markdown_before_gradle_build
    self.assertEqual(["lint-markdown", "check"], command_names(calls[0]))
AssertionError: Lists differ: ['lint-markdown', 'check'] != ['gradlew', '--console=plain', 'build']

First differing element 0:
'lint-markdown'
'gradlew'

Second list contains 1 additional elements.
First extra element 2:
'build'

- ['lint-markdown', 'check']
+ ['gradlew', '--console=plain', 'build']

======================================================================
FAIL: test_check_skips_gradle_build_when_lint_markdown_fails (test_devops.DevopsScriptTest.test_check_skips_gradle_build_when_lint_markdown_fails)
----------------------------------------------------------------------
Traceback (most recent call last):
  File "c:\Dev-Projects\template.java\scripts\tests\test_devops.py", line 81, in test_check_skips_gradle_build_when_lint_markdown_fails
[... 26 lines omitted ...]
Ran 9 tests in 0.279s

FAILED (failures=3, errors=1)
```

Green phase after implementing the lint step, adding the config, and fixing the nine findings.

Command: `.venv/Scripts/python -m unittest discover -s scripts/tests` (exit code 0)

```text
.........
----------------------------------------------------------------------
Ran 9 tests in 0.301s

OK
```

Command: `.venv/Scripts/lint-markdown check` (exit code 0)

```text
All 9 markdown files passed linting.
```

Command: `python scripts/devops.py check` (exit code 0)

```text
Command: C:\Dev-Projects\template.java\.venv\Scripts\lint-markdown.EXE check
Process ID: 77084
All 9 markdown files passed linting.
Command: C:\Dev-Projects\template.java\gradlew.bat --console=plain build
Process ID: 65840
[... 155 lines omitted ...]
Resolve mutations for :build (Thread[#1366,Execution worker Thread 2,5,main]) started.
:build (Thread[#1366,Execution worker Thread 2,5,main]) started.
> Task :build
Skipping task ':build' as it has no actions.
BUILD SUCCESSFUL in 4s
16 actionable tasks: 4 executed, 12 up-to-date
[... 1 lines omitted ...]
```

Review rework, round 1: lint-markdown adds config excludes to its defaults, so the config now lists only CHANGELOG.md.

Command: `.venv/Scripts/python -m unittest discover -s scripts/tests` (exit code 1, red phase for the tightened config test)

```text
....F....
======================================================================
FAIL: test_lint_markdown_config_only_adds_changelog_to_default_excludes (test_devops.DevopsScriptTest.test_lint_markdown_config_only_adds_changelog_to_default_excludes)
----------------------------------------------------------------------
Traceback (most recent call last):
  File "c:\Dev-Projects\template.java\scripts\tests\test_devops.py", line 86, in test_lint_markdown_config_only_adds_changelog_to_default_excludes
    self.assertEqual(["CHANGELOG.md"], config["exclude"])
AssertionError: Lists differ: ['CHANGELOG.md'] != ['.git', 'node_modules', '.venv', 'venv', '[83 chars].md']

First differing element 0:
'CHANGELOG.md'
'.git'

Second list contains 11 additional elements.
First extra element 1:
'node_modules'

+ ['.git',
+  'node_modules',
+  '.venv',
+  'venv',
+  'work',
+  'build',
+  'dist',
+  '.idea',
+  '.vscode',
+  '__pycache__',
+  '.plan/work',
- ['CHANGELOG.md']
? ^

+  'CHANGELOG.md']
? ^


----------------------------------------------------------------------
Ran 9 tests in 0.303s

FAILED (failures=1)
```

Command: `.venv/Scripts/python -m unittest discover -s scripts/tests` (exit code 0)

```text
.........
----------------------------------------------------------------------
Ran 9 tests in 0.281s

OK
```

Command: `.venv/Scripts/lint-markdown check` (exit code 0)

```text
All 9 markdown files passed linting.
```

Command: `python scripts/devops.py check` (exit code 0)

```text
Command: C:\Dev-Projects\template.java\.venv\Scripts\lint-markdown.EXE check
Process ID: 76740
All 9 markdown files passed linting.
Command: C:\Dev-Projects\template.java\gradlew.bat --console=plain build
Process ID: 2744
[... 155 lines omitted ...]
Resolve mutations for :build (Thread[#1439,Execution worker,5,main]) started.
:build (Thread[#1439,Execution worker,5,main]) started.
> Task :build
Skipping task ':build' as it has no actions.
BUILD SUCCESSFUL in 1s
16 actionable tasks: 4 executed, 12 up-to-date
[... 1 lines omitted ...]
```
