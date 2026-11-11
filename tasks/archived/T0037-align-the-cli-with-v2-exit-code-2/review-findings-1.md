---
task: T0037
round: 1
verdict: rejected
reviewer: "@codex"
reviewed_at: 2026-11-11T08:52:08Z
reviewed_commit: "0be6a9b"
base_commit: "e5840a7"
branch: task/T0037-align-the-cli-with-v2-exit-code-2
---
# Review Findings: Task T0037 (Round 1)

## Summary
**[Decided]** Rejected because dry-run preconditions and invalid-path exit handling violate the CLI contract.
Reviewed `git log main..HEAD`, the branch diff, task specifications, execution log, and validation evidence.
The build passed, and a fresh test execution passed all 35 tests.
Version copyright, greeting JSON output, and unknown-command exit code worked in JAR probes.

Verification commands:
- `.\gradlew.bat build --console=plain`: exit 0; `BUILD SUCCESSFUL in 3s`.
- `.\gradlew.bat test --rerun-tasks --console=plain --quiet`: exit 0.
- JUnit XML totals: 35 tests, zero failures, zero errors.
- `git status --short`: empty after verification.

## Defects
- P2: Dry-run accepts an output parent that is a regular file.
  In [Cli.java](../../../../src/main/java/myproject/Cli.java), `executeCreate` checks destination existence but skips parent-directory validation.
  The real run performs that validation through `Scaffold.createProject`; dry-run bypasses it.
  This violates the requirement that dry-run fails when real-run preconditions fail.

  Reproduced without creating files:
  ```text
  java -jar build/libs/template.java-3.0.0-pre.jar create review-probe --dry-run -o gradle.properties
  DRY_EXIT=0
  java -jar build/libs/template.java-3.0.0-pre.jar create review-probe -o gradle.properties
  REAL_EXIT=1
  ```
  Dry-run reports planned success beneath `gradle.properties`, an existing regular file.
  The real run reports `error: Failed to create` for that parent.

- P2: Invalid path syntax escapes argument validation and terminates with a stack trace and exit code 1.
  In [Cli.java](../../../../src/main/java/myproject/Cli.java), `Path.of(val)` at line 175 can throw `InvalidPathException`.
  `run` catches only `UncheckedIOException`; destination path construction has the same unhandled exception risk.
  Invalid argument syntax must return 2 with a concise diagnostic.

  Reproduced on Windows:
  ```text
  java -jar build/libs/template.java-3.0.0-pre.jar greet --log-file 'bad|path'
  Exception in thread "main" java.nio.file.InvalidPathException: Illegal char <|> at index 3: bad|path
  INVALID_EXIT=1
  ```

## Process Gaps
Existing tests miss both reproduced edge cases.
Recorded red-phase evidence and final verification evidence otherwise support the declared workflow.

## Required Rework
- Validate existing output ancestors without writes, rejecting non-directory parents consistently in dry-run and real execution.
- Convert invalid user-supplied paths into argument diagnostics with exit code 2, without uncaught stack traces.
- Add failing regression tests before fixes, then rerun affected tests and JAR probes.
- Record exact verification commands, exit codes, and terminal output in validation-proof.md.
