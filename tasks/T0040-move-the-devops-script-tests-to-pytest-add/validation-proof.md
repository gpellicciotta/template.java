## Walkthrough & Validation
### Initial Red-Phase Failures
Command: `.\.venv\Scripts\python -m pytest scripts/tests` (exit code: `1`)
```text
============================= test session starts =============================
platform win32 -- Python 3.12.8, pytest-8.4.2, pluggy-1.7.0
rootdir: C:\Dev-Projects\template.java\.plan\work\T0040-move-the-devops-script-tests-to-pytest-add\scripts
configfile: pyproject.toml
plugins: timeout-2.4.0, xdist-3.8.0
timeout: 30.0s
timeout method: thread
timeout func_only: False
created: 8/8 workers
8 workers [11 items]

.........F.                                                              [100%]
================================== FAILURES ===================================
__________ test_missing_support_python_exits_with_v3_bootstrap_hint ___________
[gw1] win32 -- Python 3.12.8 C:\Dev-Projects\template.java\.plan\work\T0040-move-the-devops-script-tests-to-pytest-add\.venv\Scripts\python.exe

monkeypatch = <_pytest.monkeypatch.MonkeyPatch object at 0x000001EE1A7DD0D0>

    def test_missing_support_python_exits_with_v3_bootstrap_hint(monkeypatch: pytest.MonkeyPatch) -> None:
        for name in [key for key in sys.modules if key == "support_python" or key.startswith("support_python.")]:
            monkeypatch.delitem(sys.modules, name)
        monkeypatch.setitem(sys.modules, "support_python", None)
        monkeypatch.setitem(sys.modules, "support_python.devops", None)
        with pytest.raises(SystemExit) as exc_info:
            load_devops()
>       assert "support-python v3 is missing" in str(exc_info.value)
E       AssertionError: assert 'support-python v3 is missing' in '**[ERROR]** support-python v2 is missing (import of support_python.devops halted; None in sys.modules); run `python scripts/bootstrap-dev-environment.py` first.'
[... 3 lines omitted ...]

scripts\tests\test_devops.py:141: AssertionError
=========================== short test summary info ===========================
FAILED scripts\tests\test_devops.py::test_missing_support_python_exits_with_v3_bootstrap_hint
======================== 1 failed, 10 passed in 3.58s =========================
```

Command: `.\.venv\Scripts\ruff check scripts` (exit code: `1`)
```text
D103 Missing docstring in public function
  --> scripts\bootstrap-dev-environment.py:16:5
   |
16 | def run(argv: list) -> None:
   |     ^^^
17 |     print(f"$ {' '.join(str(arg) for arg in argv)}", flush=True)
18 |     subprocess.run([str(arg) for arg in argv], cwd=ROOT, check=True)
   |

D103 Missing docstring in public function
  --> scripts\bootstrap-dev-environment.py:21:5
   |
21 | def main() -> int:
   |     ^^^^
22 |     if not (ROOT / ".git").exists():
23 |         run(["git", "init"])
   |

D102 Missing docstring in public method
  --> scripts\devops.py:32:9
   |
30 |     """Gradle project whose `lint` and `check` also run `lint-markdown check`, stopping at the first failure."""
31 |
32 |     def lint(self) -> bool:
   |         ^^^^
33 |         return self.lint_markdown() and super().lint()
   |

D102 Missing docstring in public method
  --> scripts\devops.py:35:9
   |
33 |         return self.lint_markdown() and super().lint()
34 |
35 |     def check(self) -> bool:
   |         ^^^^^
36 |         return self.lint_markdown() and super().check()
   |

Found 4 errors.
```

Command: `.\.venv\Scripts\ruff format --check scripts` (exit code: `1`)
```text
unformatted: File would be reformatted
  --> scripts\bootstrap-dev-environment.py:27:9
   |
26 |         run([sys.executable, "-m", "venv", VENV_DIR])
   -     run([VENV_PYTHON, "-m", "pip", "install", "--quiet", "--disable-pip-version-check", "-r", ROOT / "scripts" / "requirements.txt"])
27 +     run(
28 +         [VENV_PYTHON, "-m", "pip", "install", "--quiet", "--disable-pip-version-check", "-r", ROOT / "scripts" / "requirements.txt"]
29 +     )
30 |
   |

1 file would be reformatted, 2 files already formatted
```

### Final Green-Phase Verification
Command: `.\.venv\Scripts\python -m pytest scripts/tests` (exit code: `0`)
```text
============================= test session starts =============================
platform win32 -- Python 3.12.8, pytest-8.4.2, pluggy-1.7.0
rootdir: C:\Dev-Projects\template.java\.plan\work\T0040-move-the-devops-script-tests-to-pytest-add\scripts
configfile: pyproject.toml
plugins: timeout-2.4.0, xdist-3.8.0
timeout: 30.0s
timeout method: thread
timeout func_only: False
created: 8/8 workers
8 workers [11 items]

...........                                                              [100%]
============================= 11 passed in 3.65s ==============================
```

Command: `.\.venv\Scripts\ruff check scripts` (exit code: `0`)
```text
All checks passed!
```

Command: `.\.venv\Scripts\ruff format --check scripts` (exit code: `0`)
```text
3 files already formatted
```

Command: `python scripts/devops.py check` (exit code: `0`)
```text
Command: C:\Dev-Projects\template.java\.plan\work\T0040-move-the-devops-script-tests-to-pytest-add\.venv\Scripts\lint-markdown.EXE check
Process ID: 1744
All 9 markdown files passed linting.
Command: C:\Dev-Projects\template.java\.plan\work\T0040-move-the-devops-script-tests-to-pytest-add\gradlew.bat --console=plain build
Process ID: 75448
[... 154 lines omitted ...]
Skipping task ':check' as it has no actions.
Resolve mutations for :build (Thread[#1315,Execution worker,5,main]) started.
:build (Thread[#1315,Execution worker,5,main]) started.
> Task :build
Skipping task ':build' as it has no actions.
BUILD SUCCESSFUL in 2s
16 actionable tasks: 4 executed, 12 up-to-date
Watched directory hierarchies: [C:\Dev-Projects\template.java\.plan\work\T0040-move-the-devops-script-tests-to-pytest-add, C:\Dev-Projects\template.java\.plan\work\T0037-align-the-cli-with-v2-exit-code-2, C:\Dev-Projects\todos.server]
```
