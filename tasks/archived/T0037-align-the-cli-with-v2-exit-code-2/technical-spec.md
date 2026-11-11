### Declared Files
- src/main/java/myproject/Cli.java
- src/main/java/myproject/Log.java
- src/test/java/myproject/CliTest.java
- src/test/java/myproject/LogTest.java
- build.gradle
- docs/requirements.md
- README.md
- CHANGELOG.md

### Requirements
- Follow the CLI guidelines' Subcommand Architecture and Flags, Standard Actions, Output Streams and Formats, and Exit Codes sections.
- Keep `help --verbose` working; the global `--verbose` must not break it.
- Keep `help` and `version` single-line or unlogged as today.
- Add no JSON library; hand-write the small JSON documents with proper string escaping.
- Stamp the copyright notice into a manifest attribute from gradle.properties, with a neutral fallback when running from classes.
- Treat the exit-code and output changes as **[breaking]** in CHANGELOG.md; `3.0.0-pre` already covers the major bump.

### Tests
- Unit: `CliTest` covers exit 2 for an unknown command and for `create` without a name.
- Unit: `CliTest` covers `--log-level WARN` suppressing INFO, `--debug` implying verbose, and `--log-level` overriding `--debug`.
- Unit: `CliTest` covers `greet --format json` printing one JSON document on stdout with logs on stderr.
- Unit: `CliTest` covers `create --dry-run` writing nothing, and the version line containing the copyright notice.
- Unit: `LogTest` covers the level threshold filtering console and file output identically.
- Integration: build the jar and run `java -jar build/libs/template.java-{{version}}.jar bogus` (exit 2), `greet Gio --format json` piped through `python -m json.tool`, and `create demo --dry-run -o {{tmp-dir}}` (no directory created).

Size estimate: 8 files, 380 lines
Size note: Option parsing, logging flags, JSON output, dry-run, tests, and docs form one cohesive CLI v2 contract update.

Completion check: `.\gradlew.bat build` exits 0, plus the three jar invocations above with their exit codes.
