---
task: T0037
round: 2
verdict: rejected
reviewer: "@codex"
reviewed_at: 2026-11-11T09:00:05Z
reviewed_commit: "b1c5936"
base_commit: "e5840a7"
branch: task/T0037-align-the-cli-with-v2-exit-code-2
---
# Review Findings: Task T0037 (Round 2)

## Summary
**[Decided]** Rejected because invalid project-name arguments still return exit code 1 instead of 2.
Round 1 defects are fixed, with regression tests and recorded red-phase evidence.
Reviewed both commits, the complete branch diff, task specifications, execution log, and validation evidence.
Affected tests passed: 26 CliTest cases and eight LogTest cases, with zero failures or errors.
Checkstyle, Spotless, and JAR packaging passed.

Verification command: `.\gradlew.bat test --tests myproject.CliTest --tests myproject.LogTest checkstyleMain checkstyleTest spotlessCheck jar --console=plain`.
Exit code: 0.

```text
BUILD SUCCESSFUL in 6s
10 actionable tasks: 2 executed, 8 up-to-date
```

JAR probes confirmed corrected non-directory ancestor handling, invalid-path exit 2, greeting JSON, and manifest copyright.
The working tree remained clean after verification.

## Defects
- P2: Invalid project names return a runtime-error status instead of an argument-error status.
  In [Cli.java](../../../src/main/java/myproject/Cli.java:390), package-name validation throws `ScaffoldException`.
  The handler at line 356 converts that argument rejection into exit code 1.
  Names such as `123` cannot produce valid packages; these are invalid arguments, requiring exit code 2.
  This affects both real execution and dry-run.

  Commands:
  ```powershell
  java -jar build/libs/template.java-3.0.0-pre.jar create 123 --dry-run
  java -jar build/libs/template.java-3.0.0-pre.jar create 123
  ```

  Both invocations exited 1 and emitted:
  ```text
  **[ERROR]** error: Cannot derive a valid Java package name from '123'.
  ```

## Process Gaps
Tests cover blank names and invalid path syntax, but omit names rejected by Java package-name validation.
Recorded rework evidence otherwise supports the required failing-first workflow.

## Required Rework
- Classify invalid project-name validation as an argument error returning 2 in real execution and dry-run.
- Preserve exit 1 for operational failures, including existing destinations and non-directory output ancestors.
- Add failing regression tests for invalid package-derived names before implementing the correction.
- Record affected-test and JAR verification commands, exit codes, and verbatim output in the validation evidence.
