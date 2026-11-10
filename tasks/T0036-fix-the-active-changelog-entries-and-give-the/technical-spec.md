### Declared Files
- CHANGELOG.md
- src/main/java/myproject/Scaffold.java
- src/test/java/myproject/CliTest.java

### Tests
- Unit: `CliTest.createScaffoldsRenamedProject` asserts the scaffolded initial entry starts with an area prefix such as `DevEx:`.
- Integration: build the jar, run `java -jar build/libs/template.java-{{version}}.jar create sample-app -o {{tmp-dir}}`, and inspect the generated CHANGELOG.md.

Size estimate: 3 files, 40 lines

Completion check: `.\gradlew.bat test --tests myproject.CliTest` exits 0, plus the integration scenario above.
