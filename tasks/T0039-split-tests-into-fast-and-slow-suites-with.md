---
id: T0039
owner: "@gemini"
session: antigravity-20261111-13fb82
needs: []
branch: task/T0039-split-tests-into-fast-and-slow-suites-with
worktree: .plan/work/T0039-split-tests-into-fast-and-slow-suites-with
status: active
started_at: "2026-11-11T18:22:51Z"
ended_at: —
review: agent
preview: none
protocol: standard
usage:
  worker: {turns: 2, duration_s: 2839.4, input: 510648, output: 151866, cache_read: 14540486}
  review: {turns: 2, duration_s: 206.2, input: 114786, output: 4802, cache_read: 804608, cache_write: 0}
review_status: rejected
review_round: 1
reviewed_by: "@codex"
work_duration: 1545.9
---
# T0039: Split tests into fast and slow suites with timeouts, parallel safety, a jar end-to-end test, and leak guard

## Goals
Split the Gradle tests into fast and slow suites using the java guidelines' `test` and `slowTest` blocks, with 30 s and 120 s timeouts and parallel execution.
Make every test parallel-safe, isolating the static `Log` sink state that CliTest and LogTest share.
Add a slow end-to-end test running the built jar with `java -jar`, checking exit codes and streams for help, version, greet, and an invalid command.
Add a guard that fails the run when a test writes into the user home or the repository.
Rely on support-python v3.1.0, pinned by T0040, so `devops.py check` runs both suites.
Wire `slowTest` into `check`, so `devops.py check` and CI run it, and document both suites in devops.md and AGENTS.md.

## Task Execution Steps
- [x] **[Implement]** Add the jar end-to-end test tagged slow and a deliberately leaking probe test.
- [x] **[Verify]**    Confirm the end-to-end test fails without wiring and the probe passes without a guard.
- [x] **[Implement]** Add test and slowTest configuration, parallel-safe Log state, and the leak guard; delete the probe.
- [x] **[Verify]**    Run test, slowTest, and devops.py check, recording output in validation-proof.md.
- [x] **[Doc]**       Document the fast and slow suites in devops.md, AGENTS.md, and CHANGELOG.md.
- [ ] **[Review]**    Obtain review approval of the finished work before completion.

## Execution Log
- [2026-11-10] **[Doc]**
  Companion task file scaffolded at task creation.

- [2026-11-11] **[Verify]**
  Verified red phase before wiring slowTest and StateLeakGuard.
  - JarEndToEndTest failed without test.jar.path wiring.
  - Repository leak probe passed without guard.

- [2026-11-11] **[Implement]**
  Configured parallel test and slowTest suites, isolated thread-local Log state, added StateLeakGuard, and deleted temporary leak probes.

- [2026-11-11] **[Verify]**
  Verified test, slowTest, devops.py check, and pytest scripts/tests exit 0; recorded outputs in validation-proof.md.

- [2026-11-11] **[Doc]**
  Documented fast and slow test suites and leak guard in devops.md, AGENTS.md, README.md, and CHANGELOG.md.

- [2026-11-11] **[Verify]**
  Recorded task outcomes.
  - Files touched: 10.
  - Lines touched: 668.
  - Reopenings: 0.

- [2026-11-11] **[Review]**
  Round 1 requested (mode: agent).

- [2026-11-11] **[Review]**
  Round 1 rejected by @codex: [Decided]** Rejected because home isolation leaves real user state exposed, and the guard's Git timeout cannot bound execution.
  - Findings: [review-findings-1.md](T0039-split-tests-into-fast-and-slow-suites-with/review-findings-1.md).

- [2026-11-11] **[Implement]**
  Redirected home environment variables for all Test tasks, enforced environment isolation in StateLeakGuard, and bounded Git subprocess execution with file redirection and guaranteed reaping.

- [2026-11-11] **[Verify]**
  Recorded red-phase and green-phase proofs for Log concurrency, home environment isolation, and stalled Git timeout; verified test, slowTest, devops.py check, and pytest exit 0.

- [2026-11-11] **[Verify]**
  Recorded task outcomes.
  - Files touched: 10.
  - Lines touched: 1091.
  - Reopenings: 0.

