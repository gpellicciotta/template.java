---
id: T0040
owner: ~
needs: ['T0033']
branch: —
worktree: —
status: ready
started_at: —
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
Update the commands in devops.md, AGENTS.md, and README.md.

## Task Execution Steps
- [ ] **[Implement]** Convert test_devops.py to pytest and add the pytest and Ruff requirements and configuration.
- [ ] **[Verify]**    Run pytest, ruff check, and ruff format --check, capturing initial failures.
- [ ] **[Implement]** Fix the Ruff findings and add the pytest, Ruff, and Python version matrix steps to CI.
- [ ] **[Verify]**    Run pytest and both Ruff checks to exit 0, recording output in validation-proof.md.
- [ ] **[Doc]**       Update the commands in devops.md, AGENTS.md, README.md, and CHANGELOG.md.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.
