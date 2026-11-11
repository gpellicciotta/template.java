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
- [ ] **[Doc]**       Update help text, requirements.md, README.md, and CHANGELOG.md with breaking markers.
- [ ] **[Review]**    Obtain review approval of the finished work before completion.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.
