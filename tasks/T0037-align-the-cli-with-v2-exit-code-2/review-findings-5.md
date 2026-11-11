---
task: T0037
round: 5
verdict: rejected
reviewer: "@codex"
reviewed_at: 2026-11-11T15:40:40Z
reviewed_commit: "ae9a41f"
base_commit: "main"
branch: task/T0037-align-the-cli-with-v2-exit-code-2
---
# Review Findings: Task T0037 (Round 5)

## Summary
**[Decided]** Rejected because path options still swallow a following flag as their value.
The locale fix and the dry-run log-file fix both work.
`python scripts/devops.py check` exited 0.
Reviewer output recorded from `codex exec`; launched and recorded by @claude-code.

## Defects
- P2: `create demo -o --dry-run` takes `--dry-run` as the output directory, creates `--dry-run/demo`, and exits 0.
  `greet Gio --log-file --debug` writes a file named `--debug` and exits 0.
  See Cli.java lines 175 and 217. Both should exit 2 with a missing-value error.

## Evidence
- `devops.py check` exit 0.
- Turkish-locale JAR create, dry-run and real: exit 0, package `idea`.
- JAR `create demo --dry-run --log-file {{probe}}/dry.log -o {{probe}}/no-write`: exit 0; neither path created.
- JAR `create demo -o --dry-run`: exit 0; project unexpectedly created.
- JAR `greet Gio --log-file --debug`: exit 0; `--debug` file created.
- Unknown command, missing name, invalid options, names, and paths: exit 2.
- Non-directory output parent, dry-run and real: exit 1.
- Working tree clean; all spawned commands exited.

## Process Gaps
None identified.

## Required Rework
- Reject a separated option value that starts with `-` as a missing value, exit 2.
- Add failing regressions for both probes before fixing.
