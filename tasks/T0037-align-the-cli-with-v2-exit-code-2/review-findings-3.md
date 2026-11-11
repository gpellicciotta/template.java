---
task: T0037
round: 3
verdict: rejected
reviewer: "@codex"
reviewed_at: 2026-11-11T09:09:02Z
reviewed_commit: "e153977"
base_commit: "e5840a7"
branch: task/T0037-align-the-cli-with-v2-exit-code-2
---
# Review Findings: Task T0037 (Round 3)

## Summary
**[Decided]** Rejected because dry-run and real creation still disagree under Turkish locale.
Earlier reported invalid-name and invalid-path regressions are covered and fixed.
`python scripts/devops.py check` exited 0; Gradle reused current test results.
Recorded red-phase and green-phase evidence was inspected.

## Defects
- P2: Package derivation differs between preview and execution.
  [Cli.java](../../../work/T0037-align-the-cli-with-v2-exit-code-2/src/main/java/myproject/Cli.java) uses `Locale.ROOT` at line 394.
  Execution delegates to `Scaffold.createProject` at line 433, whose package conversion uses the default locale.
  Under Turkish locale, `IDEA` passes CLI validation but fails scaffold validation before copying.
  Therefore, dry-run reports success for a real invocation that fails with exit 1.

Reproduced against the rebuilt JAR:

```text
java -Duser.language=tr -Duser.country=TR -jar build/libs/template.java-3.0.0-pre.jar create IDEA --dry-run --log-level ERROR
```

Exit code: 0. Preview reported package `idea` and planned project creation.

```text
java -Duser.language=tr -Duser.country=TR -jar build/libs/template.java-3.0.0-pre.jar create IDEA --log-level ERROR
**[ERROR]** error: Cannot derive a valid Java package name from 'IDEA'.
```

Exit code: 1. No project was created.

## Process Gaps
None identified in the implementation evidence.
Reviewer check session `93223` completed successfully; Gradle child PID was `17836`.
The spawned Gradle daemon, PID `75456`, was stopped using `processes kill 75456 --force`.

## Required Rework
- Share locale-independent package derivation between CLI validation, preview, and actual scaffolding.
- Add a failing regression covering Turkish locale and uppercase project names before fixing production code.
- Verify preview and actual JSON package names match, with both invocations returning 0 for valid names.
- Refresh affected-test and JAR execution evidence after the fix.
