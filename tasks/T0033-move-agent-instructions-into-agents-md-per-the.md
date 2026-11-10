---
id: T0033
owner: "@claude-code"
needs: []
branch: —
worktree: —
status: completed
started_at: "2026-11-10T16:23:21Z"
ended_at: "2026-11-10T16:26:33Z"
review: none
preview: none
protocol: adhoc
completion-commit: 400c26a2e5309b7c1e043654af3c08603be15309
---
# T0033: Move agent instructions into AGENTS.md per the v2 structure, reduce CLAUDE.md to an import, and fix deploy advice

## Goals
Rewrite AGENTS.md below the plan-managed block in the dev-guidelines v2 order: title with summary and doc links, Commands, Architecture, Project Conventions, Boundaries.
Move the project-specific content of CLAUDE.md there, keeping the template's TODO placeholders, and keep AGENTS.md under 150 lines.
Drop content copied from the dev-guidelines, including the Development Guidelines section and the GEMINI.md mention.
Reduce CLAUDE.md to `@AGENTS.md`, checking whether `plan init` still needs its managed block there.
Replace the stale `production` subcommand deploy advice with `DevopsCli.set_deploy` in AGENTS.md and devops.md.

Completion check: `lint-markdown check AGENTS.md CLAUDE.md docs/devops.md README.md` exits 0, plus review against the Agent Instruction Files rules.

Size estimate: 5 files, 220 lines

### Declared Files
- AGENTS.md
- CLAUDE.md
- docs/devops.md
- README.md
- CHANGELOG.md

## Task Execution Steps
- [x] **[Read]**      Read the project-setup Agent Instruction Files rules and the current CLAUDE.md content.
- [x] **[Doc]**       Rewrite AGENTS.md in the required section order and reduce CLAUDE.md to the import.
- [x] **[Doc]**       Replace production subcommand advice with DevopsCli.set_deploy and update README agent file references.
- [x] **[Doc]**       Add a Docs entry under the active CHANGELOG.md heading.
- [x] **[Verify]**    Run lint-markdown on the changed files and confirm AGENTS.md stays under 150 lines.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.

- [2026-11-10] **[Doc]**
  Rewrote AGENTS.md in the required order, reduced CLAUDE.md to @AGENTS.md, and replaced production-subcommand advice with DevopsCli.set_deploy.
  - Undeclared: docs/library-publishing.md cited a removed CLAUDE.md section.

- [2026-11-10] **[Verify]**
  lint-markdown passed on all nine files and devops.py check exited 0; AGENTS.md has 52 lines.
  - Files touched: 6.
  - Lines touched: 144.
  - Reopenings: 0.

- [2026-11-10] **[Read]**
  Confirmed plan init leaves a CLAUDE.md alone once it imports @AGENTS.md, so the managed block lives only in AGENTS.md.

- [2026-11-10] **[Complete]**
  Task completed successfully.

