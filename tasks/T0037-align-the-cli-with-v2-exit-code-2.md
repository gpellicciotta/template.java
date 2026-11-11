---
id: T0037
owner: "@gemini"
session: antigravity-20261111-5a7e0d
needs: []
branch: task/T0037-align-the-cli-with-v2-exit-code-2
worktree: .plan/work/T0037-align-the-cli-with-v2-exit-code-2
status: active
started_at: "2026-11-11T08:12:00Z"
ended_at: —
review: agent
preview: none
protocol: standard
usage:
  worker: {turns: 10, duration_s: 6190.6, input: 1144256, output: 279902, cache_read: 38888044}
  review: {turns: 4, duration_s: 370.4, input: 191454, output: 8298, cache_read: 1457152, cache_write: 0}
work_duration: 2982.9
review_status: pending
review_round: 3
---
# T0037: Align the CLI with v2: exit code 2, logging flags, --format, create --dry-run, and copyright line

## Goals
Make Cli.java follow the dev-guidelines v2 CLI and logging rules.
Return exit code 2 for unknown commands, missing arguments, and invalid options, and document it in help.
Add `--log-level`, add `-v`/`--verbose` emitting INFO progress, make `--debug` imply `--verbose`, and let `--log-level` override `--debug`.
Add `--format text|json` to `greet` and `create`, printing one JSON document on stdout with all logs on stderr in json mode.
Add `create --dry-run`, listing planned changes without writing anything.
Print the gradle.properties copyright notice in the version line, removing the hardcoded author fallback in Cli.java.

## Task Execution Steps
- [x] **[Implement]** Author failing CliTest and LogTest cases for exit codes, logging flags, formats, dry run, and copyright.
- [x] **[Verify]**    Run the new tests and confirm they fail, capturing the output.
- [x] **[Implement]** Implement the option parsing, log threshold, JSON output, dry run, and manifest copyright.
- [x] **[Verify]**    Run the tests and the jar integration scenario, recording output in validation-proof.md.
- [x] **[Doc]**       Update help text, requirements.md, README.md, and CHANGELOG.md with breaking markers.
- [ ] **[Review]**    Obtain review approval of the finished work before completion.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.

- [2026-11-11] **[Implement]**
  Authored failing CliTest and LogTest cases for exit code 2, logging flags, JSON format, dry-run, and manifest copyright.

- [2026-11-11] **[Verify]**
  Confirmed red-phase failures across 12 CliTest cases and LogTest compilation before production edits.

- [2026-11-11] **[Implement]**
  Implemented exit code 2, logging flags, JSON output, dry-run, and manifest copyright. Size note: CLI v2 contract spans 8 cohesive files.

- [2026-11-11] **[Doc]**
  Updated CLI help text, README.md, docs/requirements.md, and CHANGELOG.md breaking v3.0.0-pre entries.

- [2026-11-11] **[Verify]**
  Verified devops.py check, version parity, JUnit suites, and JAR integration scenarios.
  - Files touched: 8.
  - Lines touched: 745.
  - Reopenings: 0.

- [2026-11-11] **[Verify]**
  Completion validation found 1 errors; pending fix turn 1 of 2.
  - TF020.

- [2026-11-11] **[Verify]**
  Added missing red-phase execution log entries and verified completion validation.

- [2026-11-11] **[Review]**
  Round 1 requested (mode: agent).

- [2026-11-11] **[Review]**
  Round 1 rejected by @codex: [Decided]** Rejected because dry-run preconditions and invalid-path exit handling violate the CLI contract.
  - Findings: [review-findings-1.md](T0037-align-the-cli-with-v2-exit-code-2/review-findings-1.md).

- [2026-11-11] **[Implement]**
  Added failing regression tests, validated non-directory output ancestors without writes, and converted invalid paths into exit code 2 diagnostics.

- [2026-11-11] **[Verify]**
  Verified devops.py check, regression tests, and Round 1 review JAR probes.
  - Files touched: 8.
  - Lines touched: 835.
  - Reopenings: 0.

- [2026-11-11] **[Review]**
  Round 2 requested (mode: agent).

- [2026-11-11] **[Review]**
  Round 2 rejected by @codex: [Decided]** Rejected because invalid project-name arguments still return exit code 1 instead of 2.
  - Findings: [review-findings-2.md](T0037-align-the-cli-with-v2-exit-code-2/review-findings-2.md).

- [2026-11-11] **[Implement]**
  Added failing regression tests and classified invalid project-name package derivation as an exit code 2 argument error.

- [2026-11-11] **[Verify]**
  Verified devops.py check, regression tests, and Round 2 review JAR probes.
  - Files touched: 8.
  - Lines touched: 880.
  - Reopenings: 0.

- [2026-11-11] **[Review]**
  Round 3 requested (mode: agent).

