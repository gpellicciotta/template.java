### Declared Files
- build.gradle
- src/main/java/myproject/Log.java
- src/test/java/myproject/CliTest.java
- src/test/java/myproject/LogTest.java
- src/test/java/myproject/JarEndToEndTest.java
- src/test/java/myproject/StateLeakGuard.java
- docs/devops.md
- AGENTS.md
- CHANGELOG.md
- README.md

### Requirements
- Use the `test`, `slowTest`, and `check` blocks from the java guidelines' Testing and Quality section.
- Make `slowTest` depend on `jar` and pass the jar path to the end-to-end test as a system property.
- Make `Log` safe under concurrent tests, for example per-invocation sink state or JUnit resource locks.
- Implement the guard so a write into the user home or repository fails the run.
- One option: point `user.home` at a temp directory in the test tasks and compare `git status --porcelain` before and after.

### Tests
- Unit: the guard detects a probe test writing into the repository, then the probe is removed.
- Integration: `.\gradlew.bat test` excludes `JarEndToEndTest`, `.\gradlew.bat slowTest` runs it against the built jar, and `python scripts/devops.py check` runs both suites.

Size estimate: 10 files, 450 lines

Size note: Above the typical line because the user grouped the suite split, parallel safety, end-to-end test, and leak guard together.

Combined task: The user asked on 2026-11-10 for these test-infrastructure changes as one task.

Completion check: `python scripts/devops.py check` exits 0, plus `.\gradlew.bat test` and `.\gradlew.bat slowTest` output showing the split.
