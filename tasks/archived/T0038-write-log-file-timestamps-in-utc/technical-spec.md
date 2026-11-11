### Declared Files
- src/main/java/myproject/Log.java
- src/test/java/myproject/LogTest.java
- CHANGELOG.md

### Tests
- Unit: `LogTest` sets a fixed instant in a `+02:00` zone and asserts the file entry carries the UTC rendering.
- Integration: run `java -Duser.timezone=Europe/Brussels -jar build/libs/template.java-{{version}}.jar --log-file {{tmp-dir}}/app.log greet` and compare the timestamp with the current UTC time.

Size estimate: 3 files, 40 lines

Completion check: `.\gradlew.bat test --tests myproject.LogTest` exits 0, plus the integration scenario above.
