---
task: T0037
round: 4
verdict: rejected
reviewer: "@codex"
reviewed_at: 2026-11-11T15:37:06Z
reviewed_commit: "d52202f"
base_commit: "main"
branch: task/T0037-align-the-cli-with-v2-exit-code-2
---
# Review Findings: Task T0037 (Round 4)

## Summary
**[Decided]** Rejected because dry-run still writes files when logging is enabled.
The Turkish-locale fix works: dry-run and real creation both exit 0 with package `idea`.
`python scripts/devops.py check` exited 0.
Reviewer output recorded verbatim from `codex exec`; launched and recorded by @claude-code.

## Defects
- P2: `create demo --dry-run --log-file build/review-r4-dry.log` creates and writes the log file.
  See Cli.java line 362. This violates the "dry run writes nothing" contract.

## Evidence
- `git diff main...HEAD` exit 0; full branch diff reviewed.
- `devops.py check` exit 0.
- Turkish-locale JAR create, dry-run and real: exit 0, package `idea`.
- JAR `create demo --dry-run --format json --log-file build/review-r4-dry.log -o build/review-r4-no-write`: exit 0; log file created.
- JAR `bogus`: exit 2. JAR `greet Gio --format json --debug --log-level WARN`: exit 0, JSON only.
- `git status --short` clean; all spawned commands exited.

## Process Gaps
None identified.

## Required Rework
- Make `create --dry-run` leave the filesystem untouched, including any `--log-file` target.
- Add a failing regression for `--dry-run --log-file`, then fix, then refresh evidence.
