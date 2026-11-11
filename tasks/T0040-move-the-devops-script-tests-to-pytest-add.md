---
id: T0040
owner: "@gemini"
session: antigravity-20261111-8f71bc
needs: []
branch: task/T0040-move-the-devops-script-tests-to-pytest-add
worktree: .plan/work/T0040-move-the-devops-script-tests-to-pytest-add
status: active
started_at: "2026-11-11T09:21:42Z"
ended_at: —
review: none
preview: none
protocol: adhoc
---
# T0040: Move the devops script tests to pytest, add Ruff checks, and test the minimum supported Python in CI

## Goals
Convert scripts/tests/test_devops.py from unittest to pytest, per the python guidelines.
Add pinned pytest, pytest-xdist, pytest-timeout, and Ruff as script development requirements.
Configure pytest for scripts/ with a `slow` marker and default timeouts, and configure Ruff, recording where the configuration lives.
Fix all `ruff check` and `ruff format --check` findings in scripts/.
Make CI run pytest and both Ruff checks, testing the scripts on Python 3.9, the documented minimum, and the latest 3.x.
Pin support-python v3.1.0 in scripts/requirements.txt and update its v2 references in devops.md and devops.py.
Update the commands in devops.md, AGENTS.md, and README.md.

## Task Execution Steps
- [x] **[Read]**      Read support-python v3.1.0 migration-v3.md and confirm devops.py still works.
- [x] **[Implement]** Convert test_devops.py to pytest and add the pytest and Ruff requirements and configuration.
- [x] **[Verify]**    Run pytest, ruff check, and ruff format --check, capturing initial failures.
- [x] **[Implement]** Fix the Ruff findings and add the pytest, Ruff, and Python version matrix steps to CI.
- [x] **[Verify]**    Run pytest and both Ruff checks to exit 0, recording output in validation-proof.md.
- [x] **[Doc]**       Update the commands in devops.md, AGENTS.md, README.md, and CHANGELOG.md.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.

- [2026-11-11] **[Read]**
  Read migration-v3.md and verified devops.py runs cleanly against support-python v3.1.0.

- [2026-11-11] **[Implement]**
  Converted test_devops.py to pytest and configured pytest and Ruff in scripts/pyproject.toml.

- [2026-11-11] **[Verify]**
  Captured initial failures for pytest (v2 hint), ruff check (4 docstring findings), and ruff format (line length).

- [2026-11-11] **[Implement]**
  Fixed Ruff findings, updated devops.py to v3, and added CI Python 3.9/3.x script checks.

- [2026-11-11] **[Verify]**
  Verified pytest, ruff check, ruff format --check, and devops.py check all exit 0; touched undeclared .lint-markdown.json for v3 github-owners.

- [2026-11-11] **[Doc]**
  Updated commands and configuration notes in devops.md, AGENTS.md, README.md, and CHANGELOG.md.

- [2026-11-11] **[Verify]**
  Recorded task outcomes.
  - Files touched: 11.
  - Lines touched: 289.
  - Reopenings: 0.

