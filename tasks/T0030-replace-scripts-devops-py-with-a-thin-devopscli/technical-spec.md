# Thin DevopsCli script for template.java

## Technical Specifications
### Context
- The dev-guidelines project setup guidelines require scripts built on support.python, with `devops.py` a thin runner on `support_python.devops`.
- support.python v2.0.0 provides `DevopsCli` and `GradleProject`; its `docs/migration-v2.md` maps the migration.
- Today `scripts/devops.py` is a 157-line hand-written CLI with `check`, `build`, `test`, `clean`, `versions`, `release`, `version`, and `help`.
- `scripts/bootstrap-dev-environment.py` runs `git init` when needed, then `gradlew build`.
- `.github/workflows/ci.yml` runs `python3 scripts/devops.py check`.

### Required Behavior
- `scripts/requirements.txt` pins `support-python @ git+https://github.com/gpellicciotta/support.python.git@v2.0.0`.
- `bootstrap-dev-environment.py` stays stdlib-only and installs `scripts/requirements.txt` before the Gradle build.
- `scripts/devops.py` runs `DevopsCli(GradleProject(REPO_ROOT, ...)).run()` and holds no other logic.
- Every current action keeps working with equivalent results: `check`, `build`, `test`, `clean`, `versions`, `release`, `version`, `help`.
- `release` still delegates to the Gradle `doFullRelease` task through `release_task`, keeping a typed confirmation and `--dry-run`.
- `test --tests {{pattern}}` keeps narrowing the test run; add it through `DevopsCli.add_action` if `GradleProject` lacks it.
- CI installs `scripts/requirements.txt` before running `python3 scripts/devops.py check`.

### Design Decisions
- **[Decided]** Install `scripts/requirements.txt` into a project-local `.venv`, not the interpreter running the bootstrap.
  The global interpreter holds support-python v1.0.0 for counters.server and todos.server; v2 would break them.
  `devops.py` reruns itself with the `.venv` interpreter when present, so `python scripts/devops.py` keeps working.
- **[Decided]** `Scaffold` skips `.venv`, and CI installs the requirements into the runner's Python without a venv.
- **[Decided]** Replace `test --tests {{pattern}}` with `GradleProject`'s `test {{pattern}}`; no major bump, since `devops.py` is dev tooling.
- **[Decided]** Override the help examples, since the `DevopsCli` defaults show a pytest selection.
- **[Decided]** Override `GradleProject` task lists only where the current script differs from the defaults.
- **[Decided]** Keep the project-local bootstrap script, as support.python ADR-0001 requires.

### Out of Scope
- Server service groups (`add_services`) and `set_deploy`; this template has no server or production target.
- Changes to `gradle/release.gradle` or the `doFullRelease` pipeline.

### Tests
Author these before changing the scripts, and record their failing output:
- Unit: A Python test loads `scripts/devops.py` and asserts it builds a `DevopsCli` over a `GradleProject` rooted at the repository.
- Unit: `python scripts/devops.py help` lists `check`, `build`, `test`, `clean`, `versions`, and `release`.
- Unit: `python scripts/devops.py versions` exits 0 while `gradle.properties` and `CHANGELOG.md` agree, and 1 when they drift.
- Integration: On a fresh clone, `python scripts/bootstrap-dev-environment.py` installs support.python and builds successfully.
- Integration: `python scripts/devops.py check` passes, and `python scripts/devops.py release --dry-run` prints the `doFullRelease` plan without pushing.
- Integration: CI passes on the task branch.

### Declared Files
- `scripts/devops.py`
- `scripts/bootstrap-dev-environment.py`
- `scripts/requirements.txt` (new)
- `.github/workflows/ci.yml`
- `docs/devops.md`
- `README.md`
- `CHANGELOG.md`
