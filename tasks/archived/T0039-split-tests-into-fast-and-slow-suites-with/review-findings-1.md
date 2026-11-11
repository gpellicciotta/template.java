---
task: T0039
round: 1
verdict: rejected
reviewer: "@codex"
reviewed_at: 2026-11-11T18:48:34Z
reviewed_commit: "45744cd"
base_commit: "6711046"
branch: task/T0039-split-tests-into-fast-and-slow-suites-with
---
# Review Findings: Task T0039 (Round 1)

## Summary
**[Decided]** Rejected because home isolation leaves real user state exposed, and the guard's Git timeout cannot bound execution.
Suite selection, configured timeouts, concurrent logging isolation, jar execution, and check wiring otherwise work.
Independent verification passed 43 fast tests, four slow tests, Spotless, and Checkstyle.

Command: `.\gradlew.bat test slowTest check --console=plain --rerun-tasks --no-daemon`; exit code: `0`.

```text
BUILD SUCCESSFUL in 23s
12 actionable tasks: 12 executed
```

## Defects
- P1: Home environment variables bypass isolation and detection.
  In `build.gradle`, lines 77–100 configure JVM home properties but leave inherited `HOME` and `USERPROFILE` unchanged.
  `StateLeakGuard.java`, lines 189–197 and 213–224, snapshots only the redirected home and repository.
  Consequently, tests or subprocesses resolving home through environment variables can write into real user state without detection.
  The jar helper redirects these variables, but other tests and subprocesses receive no equivalent protection.
  This finding follows directly from configuration and guard inspection; no real-home write probe was executed.

- P2: The Git subprocess timeout starts after an unbounded blocking read.
  In `StateLeakGuard.java`, lines 251–255 call `readAllBytes()` before `waitFor(10, ...)`.
  A stalled Git process retaining stdout prevents execution from reaching that timeout.
  These extension callbacks have no configured callback timeout, so a hung Git command can stall the suite indefinitely.
  The exception handler also lacks guaranteed process cleanup after interruption or read failure.
  This finding follows from control-flow inspection; no hanging subprocess was injected.

## Process Gaps
The recorded red-phase and final verification evidence covers the original repository and redirected-home probes.
It does not cover environment-based home resolution or a stalled Git subprocess.
The production logging concurrency test has green evidence, but no dedicated failing-first evidence appears in validation-proof.md.

## Required Rework
- Redirect home environment variables for every test JVM and inherited subprocess, using guarded temporary directories.
- Add an isolated regression probe for environment-based home writes without touching the actual user profile.
- Bound Git execution independently from stdout consumption and guarantee subprocess termination and reaping on every exit path.
- Add a regression scenario with a controllable stalled Git command and verify bounded failure and process cleanup.
- Rerun fast tests, affected slow tests, and required checks; record exact commands, exit codes, and verbatim output.
