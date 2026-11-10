---
id: T0030
owner: "@claude-code"
needs: []
branch: —
worktree: —
status: completed
started_at: "2026-11-10T14:51:21Z"
ended_at: "2026-11-10T15:08:08Z"
review: agent
preview: none
protocol: standard
review_status: approved
review_round: 1
reviewed_by: "@claude-code-reviewer"
completion-commit: d7d0f9425280433dd3defb4b326af85449a59bdc
---
# T0030: Replace scripts/devops.py with a thin DevopsCli script on the GradleProject adapter, pinning the released support-python v2.0.0

## Goals
Needs the released support-python v2.0.0; see `docs/migration-v2.md` in support-python for the full mapping.
Replace `scripts/devops.py` with a thin script running `DevopsCli(GradleProject(REPO_ROOT)).run()`.
Pin `support-python @ git+https://github.com/gpellicciotta/support.python.git@v2.0.0` in `scripts/requirements.txt`.
Override the Gradle task lists or set `release_task` where the current script differs from the defaults.
Keep project-specific actions through `DevopsCli.add_action`.

Specifications: [technical-spec.md](T0030-replace-scripts-devops-py-with-a-thin-devopscli/technical-spec.md).
Completion check: `python scripts/bootstrap-dev-environment.py`, then `python scripts/devops.py check`
Size estimate: 7 files, 250 lines

## Task Execution Steps
- [x] **[Verify]**    Record current `devops.py help`, `versions`, and `release --dry-run` output as the baseline.
- [x] **[Verify]**    Author the failing tests from the technical spec and record their failure output.
- [x] **[Implement]** Add `scripts/requirements.txt` and install it from `bootstrap-dev-environment.py`.
- [x] **[Implement]** Replace `scripts/devops.py` with the thin `DevopsCli(GradleProject(...))` script.
- [x] **[Implement]** Install `scripts/requirements.txt` in CI before `devops.py check`.
- [x] **[Verify]**    Run the completion check and the integration scenario; record verbatim output.
- [x] **[Doc]**       Update `docs/devops.md`, `README.md`, and CHANGELOG.md.
- [x] **[Review]**    Obtain review approval of the finished work before completion.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.

- [2026-11-10] **[Review]**
  Round 1 requested (mode: agent).

- [2026-11-10] **[Review]**
  Round 1 approved by @claude-code-reviewer.

- [2026-11-10] **[Review]**
  Agent review approved; fixed the stale CI docs and the missing-venv traceback, moved help text and -pre checks upstream.
  - Follow-ups: template.java T0031, support.python T0091.

- [2026-11-10] **[Verify]**
  Recorded task outcomes; CI fails only on the pre-existing scaffold root issue that T0031 fixes.
  - Files touched: 12.
  - Lines touched: 321.
  - Reopenings: 0.

- [2026-11-10] **[Verify]**
  Recorded the baseline help, versions, and release dry-run output, then five failing unittest cases and the failing scaffold test.

- [2026-11-10] **[Implement]**
  Added scripts/requirements.txt pinning support-python v2.0.0, a .venv bootstrap, and the thin DevopsCli devops.py with venv re-exec.

- [2026-11-10] **[Implement]**
  Added Python setup, requirements install, and the scripts/tests unittest run to CI; Scaffold now skips .venv.

- [2026-11-10] **[Doc]**
  Updated README, docs/devops.md, CLAUDE.md, and CHANGELOG.md for the new devops.py, bootstrap, and test syntax.

- [2026-11-10] **[Complete]**
  Task completed successfully.

