---
id: T0039
owner: ~
needs: ['T0037', 'T0038', 'T0033']
branch: —
worktree: —
status: ready
started_at: —
ended_at: —
review: agent
preview: none
protocol: standard
---
# T0039: Split tests into fast and slow suites with timeouts, parallel safety, a jar end-to-end test, and leak guard

## Goals
Split the Gradle tests into fast and slow suites using the java guidelines' `test` and `slowTest` blocks, with 30 s and 120 s timeouts and parallel execution.
Make every test parallel-safe, isolating the static `Log` sink state that CliTest and LogTest share.
Add a slow end-to-end test running the built jar with `java -jar`, checking exit codes and streams for help, version, greet, and an invalid command.
Add a guard that fails the run when a test writes into the user home or the repository.
Wire `slowTest` into `check`, so `devops.py check` and CI run it, and document both suites in devops.md and AGENTS.md.

## Task Execution Steps
- [ ] **[Implement]** Add the jar end-to-end test tagged slow and a deliberately leaking probe test.
- [ ] **[Verify]**    Confirm the end-to-end test fails without wiring and the probe passes without a guard.
- [ ] **[Implement]** Add test and slowTest configuration, parallel-safe Log state, and the leak guard; delete the probe.
- [ ] **[Verify]**    Run test, slowTest, and devops.py check, recording output in validation-proof.md.
- [ ] **[Doc]**       Document the fast and slow suites in devops.md, AGENTS.md, and CHANGELOG.md.
- [ ] **[Review]**    Obtain review approval of the finished work before completion.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.
