package myproject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliTest {
  @AfterEach
  void resetLog() {
    Log.reset();
  }

  private static String runAndCaptureOut(String... args) throws Exception {
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    int exitCode = Cli.run(args, new PrintStream(outBuf, true, "UTF-8"), new PrintStream(errBuf, true, "UTF-8"));
    assertEquals(0, exitCode, "expected exit code 0, stderr was: " + errBuf);
    return outBuf.toString(StandardCharsets.UTF_8);
  }

  private static int runAndGetExitCode(ByteArrayOutputStream outBuf, ByteArrayOutputStream errBuf, String... args) throws Exception {
    return Cli.run(args, new PrintStream(outBuf, true, "UTF-8"), new PrintStream(errBuf, true, "UTF-8"));
  }

  @Test
  void helpExitsZero() throws Exception {
    String out = runAndCaptureOut("help");
    assertTrue(out.contains("usage:"));
    assertTrue(out.contains("exit codes:"));
    assertTrue(out.contains("commands:"));
    assertTrue(out.contains("options:"));
    assertTrue(out.contains("--debug"));
    assertTrue(out.contains("--log-file"));
    assertTrue(out.contains("--log-level"));
    assertTrue(out.contains("-v, --verbose"));
    assertTrue(out.contains("--format"));
    assertTrue(out.contains("--dry-run"));
    assertTrue(out.contains("  2  "));
  }

  @Test
  void helpShortOptionExitsZero() throws Exception {
    String out = runAndCaptureOut("-h");
    assertTrue(out.contains("usage:"));
    assertTrue(out.contains("exit codes:"));
  }

  @Test
  void helpLongOptionExitsZero() throws Exception {
    String out = runAndCaptureOut("--help");
    assertTrue(out.contains("usage:"));
    assertTrue(out.contains("exit codes:"));
  }

  @Test
  void helpVerboseOptionIncludesDetails() throws Exception {
    String out = runAndCaptureOut("help", "--verbose");
    assertTrue(out.contains("details:"));
    assertTrue(out.contains("create:"));
    assertTrue(out.contains("gradle.properties"));
    assertTrue(out.contains("plan init --store branch"));
    assertFalse(out.contains("PLAN.md"));
    assertFalse(out.contains("Starting template.java"));
  }

  @Test
  void noArgsShowsHelp() throws Exception {
    String out = runAndCaptureOut();
    assertTrue(out.contains("usage:"));
    assertTrue(out.contains("exit codes:"));
  }

  @Test
  void versionCommand() throws Exception {
    String out = runAndCaptureOut("version").strip();
    assertTrue(out.startsWith("template.java v"));
    assertTrue(out.contains(" - Copyright (c) unknown"));
    assertFalse(out.contains("Giovanni Pellicciotta"));
  }

  @Test
  void versionOption() throws Exception {
    String out = runAndCaptureOut("--version").strip();
    assertTrue(out.startsWith("template.java v"));
    assertTrue(out.contains(" - Copyright (c) unknown"));
    assertFalse(out.contains("Giovanni Pellicciotta"));
  }

  @Test
  void manifestCopyrightIsReadFromJar(@TempDir Path tmpDir) throws Exception {
    String expected = Files.readString(Path.of("gradle.properties"), StandardCharsets.UTF_8)
        .lines()
        .filter(line -> line.startsWith("copyright="))
        .map(line -> line.substring("copyright=".length()))
        .findFirst()
        .orElseThrow();

    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().putValue("Copyright", expected);
    Path jarPath = tmpDir.resolve("app.jar");
    try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
      jos.flush();
    }

    assertEquals(expected, Cli.readManifestCopyright(jarPath));
    assertEquals("Copyright (c) unknown", Cli.readManifestCopyright(tmpDir));
  }

  @Test
  void greet() throws Exception {
    assertTrue(runAndCaptureOut("greet", "Gio").contains("Hello, Gio"));
  }

  @Test
  void greetDefaultName() throws Exception {
    assertTrue(runAndCaptureOut("greet").contains("Hello, wereld"));
  }

  @Test
  void greetLogsStartupAndCompletionByDefault() throws Exception {
    String out = runAndCaptureOut("greet", "Gio");
    assertTrue(out.contains("Starting template.java"));
    assertTrue(out.contains("Completed in"));
  }

  @Test
  void verboseOptionEmitsProgressWithoutEnablingDebug() throws Exception {
    for (String flag : List.of("-v", "--verbose")) {
      String out = runAndCaptureOut(flag, "greet", "Gio");
      assertTrue(out.contains("Greeting target: Gio"));
      assertFalse(out.contains("**[DEBUG]**"));
    }
  }

  @Test
  void logLevelWarnSuppressesInfo(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("app.log");
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    int exitCode = runAndGetExitCode(outBuf, errBuf, "--log-level", "WARN", "--log-file", logFile.toString(), "greet", "Gio");

    assertEquals(0, exitCode);
    assertEquals("Hello, Gio", outBuf.toString(StandardCharsets.UTF_8).strip());
    assertEquals("", errBuf.toString(StandardCharsets.UTF_8));
    assertFalse(Files.exists(logFile));
  }

  @Test
  void debugOptionEnablesDebugAndImpliesVerboseAndYieldsToLogLevel(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("app.log");
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    int exitCode = runAndGetExitCode(outBuf, errBuf, "--debug", "--log-file", logFile.toString(), "greet", "Gio");

    assertEquals(0, exitCode);
    String out = outBuf.toString(StandardCharsets.UTF_8);
    assertTrue(out.contains("Greeting target: Gio"));
    assertTrue(out.contains("**[DEBUG]**"));
    String logged = Files.readString(logFile, StandardCharsets.UTF_8);
    assertTrue(logged.contains("**[INFO]**"));
    assertTrue(logged.contains("**[DEBUG]**"));
    assertTrue(logged.contains("config: debug=true"));

    ByteArrayOutputStream overrideOut = new ByteArrayOutputStream();
    ByteArrayOutputStream overrideErr = new ByteArrayOutputStream();
    int overrideExit = runAndGetExitCode(overrideOut, overrideErr, "--debug", "--log-level", "INFO", "greet", "Gio");
    assertEquals(0, overrideExit);
    String overridden = overrideOut.toString(StandardCharsets.UTF_8);
    assertTrue(overridden.contains("Greeting target: Gio"));
    assertFalse(overridden.contains("**[DEBUG]**"));
  }

  @Test
  void greetFormatJsonPrintsSingleDocumentOnStdoutWithLogsOnStderr() throws Exception {
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    int exitCode = runAndGetExitCode(outBuf, errBuf, "greet", "Gio \"Jr\"", "--format", "json");

    assertEquals(0, exitCode);
    assertEquals("{\"name\": \"Gio \\\"Jr\\\"\", \"greeting\": \"Hello, Gio \\\"Jr\\\"\"}",
        outBuf.toString(StandardCharsets.UTF_8).strip());
    String errOut = errBuf.toString(StandardCharsets.UTF_8);
    assertTrue(errOut.contains("Starting template.java"));
    assertTrue(errOut.contains("Completed in"));
  }

  @Test
  void logFileOptionAppendsOperationalLogs(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("app.log");
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    runAndGetExitCode(outBuf, errBuf, "--log-file", logFile.toString(), "greet", "Gio");

    assertTrue(Files.isRegularFile(logFile));
    String logged = Files.readString(logFile, StandardCharsets.UTF_8);
    assertTrue(logged.contains("Starting template.java"));
    assertTrue(logged.contains("Completed in"));
  }

  @Test
  void errorMessagesAreCapturedInLogFile(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("app.log");
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    int exitCode = runAndGetExitCode(outBuf, errBuf, "--log-file", logFile.toString(), "create");

    assertEquals(2, exitCode);
    String logged = Files.readString(logFile, StandardCharsets.UTF_8);
    assertTrue(logged.contains("**[ERROR]**"));
    assertTrue(logged.contains("requires a project-name argument"));
  }

  @Test
  void unwritableLogFileProducesCleanErrorInsteadOfCrashing(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("missing-parent-dir").resolve("app.log");
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    int exitCode = runAndGetExitCode(outBuf, errBuf, "--log-file", logFile.toString(), "greet", "Gio");

    assertEquals(1, exitCode);
    assertTrue(errBuf.toString(StandardCharsets.UTF_8).contains("error: failed to write --log-file"));
  }

  @Test
  void unknownCommandReturnsTwo() throws Exception {
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    int exitCode = runAndGetExitCode(outBuf, errBuf, "unknown-cmd");
    assertEquals(2, exitCode);
    assertTrue(errBuf.toString(StandardCharsets.UTF_8).contains("error: unknown command 'unknown-cmd'"));
    assertTrue(outBuf.toString(StandardCharsets.UTF_8).contains("usage:"));
  }

  @Test
  void invalidOptionsAndMissingValuesReturnTwo() throws Exception {
    for (String[] badArgs : List.of(
        new String[]{"--unknown"},
        new String[]{"greet", "--unknown"},
        new String[]{"greet", "--format", "xml"},
        new String[]{"greet", "--format"},
        new String[]{"--log-level", "TRACE", "greet"},
        new String[]{"greet", "--log-file"},
        new String[]{"greet", "Gio", "Extra"},
        new String[]{"create", "demo", "-o"}
    )) {
      ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
      ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
      assertEquals(2, runAndGetExitCode(outBuf, errBuf, badArgs),
          "expected exit 2 for args: " + String.join(" ", badArgs));
    }
  }

  @Test
  void createScaffoldsRenamedProject(@TempDir Path tmpDir) throws Exception {
    String projectName = "sample-app";
    runAndCaptureOut("create", projectName, "-o", tmpDir.toString());

    Path destination = tmpDir.resolve(projectName);
    assertTrue(Files.isDirectory(destination));
    assertTrue(Files.isRegularFile(destination.resolve("src/main/java/sample_app/Cli.java")));

    try (Stream<Path> children = Files.list(destination.resolve("src/main/java"))) {
      List<Path> packageDirs = children.filter(Files::isDirectory).toList();
      assertEquals(1, packageDirs.size(), "expected exactly one package directory, found: " + packageDirs);
    }

    String buildGradle = Files.readString(destination.resolve("build.gradle"), StandardCharsets.UTF_8);
    assertTrue(buildGradle.contains("mainClass = 'sample_app.Cli'"));

    String gradleProperties = Files.readString(destination.resolve("gradle.properties"), StandardCharsets.UTF_8);

    String readme = Files.readString(destination.resolve("README.md"), StandardCharsets.UTF_8);
    assertTrue(readme.contains("Sample App"));

    assertTrue(Files.isRegularFile(destination.resolve("LICENSE.md")));

    String changelog = Files.readString(destination.resolve("CHANGELOG.md"), StandardCharsets.UTF_8);
    List<String> entries = changelog.lines().filter(line -> line.startsWith("- ")).toList();
    assertEquals(1, entries.size(), "expected one initial entry, found: " + entries);
    assertTrue(entries.get(0).matches("- [A-Z][A-Za-z]+: \\S.*Sample App.*"),
        "initial entry lacks an area prefix: " + entries.get(0));

    assertAll("fresh project metadata",
        () -> assertEquals(List.of("version=0.1.0-pre"),
            gradleProperties.lines().filter(line -> line.startsWith("version=")).toList()),
        () -> assertEquals(List.of("## v0.1.0-pre"),
            changelog.lines().filter(line -> line.startsWith("## ")).toList()),
        () -> assertFalse(changelog.contains("[in development]")),
        () -> assertFalse(Files.exists(destination.resolve("PLAN.md"), LinkOption.NOFOLLOW_LINKS)),
        () -> assertFalse(Files.exists(destination.resolve(".plan"))),
        () -> assertFalse(Files.exists(destination.resolve("plan.code-workspace"))),
        () -> assertFalse(buildGradle.contains("hinolugi")),
        () -> assertFalse(Files.exists(destination.resolve("TODO.md"))),
        () -> assertTrue(Files.isRegularFile(destination.resolve("scripts/requirements.txt"))),
        () -> assertTrue(Files.isRegularFile(destination.resolve("scripts/tests/test_devops.py"))),
        () -> assertFalse(Files.exists(destination.resolve(".venv"))));

    assertTrue(Files.isRegularFile(destination.resolve("docs/index.md")));
    assertTrue(Files.isRegularFile(destination.resolve("docs/requirements.md")));
    assertTrue(Files.isRegularFile(destination.resolve("docs/devops.md")));
  }

  @Test
  void createRefusesExistingDestination(@TempDir Path tmpDir) throws Exception {
    String projectName = "dup-app";
    Files.createDirectory(tmpDir.resolve(projectName));

    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    int exitCode = Cli.run(new String[]{"create", projectName, "-o", tmpDir.toString()},
        new PrintStream(outBuf, true, "UTF-8"), new PrintStream(errBuf, true, "UTF-8"));
    assertEquals(1, exitCode);
  }

  @Test
  void createDryRunListsPlannedChangesWithoutWritingAnything(@TempDir Path tmpDir) throws Exception {
    Path missingParent = tmpDir.resolve("nested");
    String out = runAndCaptureOut("create", "demo-app", "--dry-run", "-o", missingParent.toString());

    assertFalse(Files.exists(missingParent.resolve("demo-app")));
    assertFalse(Files.exists(missingParent));
    assertTrue(out.contains("Dry run"));
    assertTrue(out.contains("demo_app"));
    assertTrue(out.contains("CHANGELOG.md"));
    assertTrue(out.contains("gradle.properties"));

    Files.createDirectories(tmpDir.resolve("existing-app"));
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    assertEquals(1, runAndGetExitCode(outBuf, errBuf, "create", "existing-app", "--dry-run", "-o", tmpDir.toString()));
  }

  @Test
  void createFormatJsonPrintsSingleDocumentOnStdoutWithLogsOnStderr(@TempDir Path tmpDir) throws Exception {
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    int exitCode = runAndGetExitCode(outBuf, errBuf,
        "create", "demo-app", "--dry-run", "--format", "json", "-o", tmpDir.toString());

    assertEquals(0, exitCode);
    assertFalse(Files.exists(tmpDir.resolve("demo-app")));
    String json = outBuf.toString(StandardCharsets.UTF_8).strip();
    assertTrue(json.startsWith("{") && json.endsWith("}"));
    assertTrue(json.contains("\"project-name\": \"demo-app\""));
    assertTrue(json.contains("\"package-name\": \"demo_app\""));
    assertTrue(json.contains("\"dry-run\": true"));
    assertTrue(json.contains("\"changes\": ["));
    assertFalse(json.contains("Starting template.java"));
    String errOut = errBuf.toString(StandardCharsets.UTF_8);
    assertTrue(errOut.contains("Starting template.java"));
    assertTrue(errOut.contains("Completed in"));
  }

  @Test
  @ResourceLock(Resources.LOCALE)
  void createDryRunAndRealExecutionAgreeUnderTurkishLocale(@TempDir Path tmpDir) throws Exception {
    Locale original = Locale.getDefault();
    Locale.setDefault(Locale.forLanguageTag("tr-TR"));
    try {
      ByteArrayOutputStream dryOut = new ByteArrayOutputStream();
      ByteArrayOutputStream dryErr = new ByteArrayOutputStream();
      assertEquals(0, runAndGetExitCode(dryOut, dryErr,
          "create", "IDEA", "--dry-run", "--format", "json", "-o", tmpDir.toString()),
          "dry run stderr: " + dryErr);
      assertTrue(dryOut.toString(StandardCharsets.UTF_8).contains("\"package-name\": \"idea\""));

      ByteArrayOutputStream realOut = new ByteArrayOutputStream();
      ByteArrayOutputStream realErr = new ByteArrayOutputStream();
      assertEquals(0, runAndGetExitCode(realOut, realErr,
          "create", "IDEA", "--format", "json", "-o", tmpDir.toString()),
          "real run stderr: " + realErr);
      assertTrue(realOut.toString(StandardCharsets.UTF_8).contains("\"package-name\": \"idea\""));
      assertTrue(Files.isDirectory(tmpDir.resolve("IDEA/src/main/java/idea")));
    }
    finally {
      Locale.setDefault(original);
    }
  }

  @Test
  void createDryRunDoesNotWriteTheLogFile(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("dry.log");
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    int exitCode = runAndGetExitCode(outBuf, errBuf,
        "create", "demo-app", "--dry-run", "--log-file", logFile.toString(), "-o", tmpDir.toString());

    assertEquals(0, exitCode, "stderr: " + errBuf);
    assertFalse(Files.exists(logFile));
    assertFalse(Files.exists(tmpDir.resolve("demo-app")));
    assertTrue(errBuf.toString(StandardCharsets.UTF_8).contains("--log-file is ignored in a dry run"));
  }

  @Test
  void pathOptionsRejectAFollowingFlagAsTheirValue(@TempDir Path tmpDir) throws Exception {
    // The create probes keep --dry-run first, so a regression fails the test without writing a project.
    Path debugProbe = Path.of("--debug");
    for (String[] args : List.of(
        new String[]{"create", "demo", "--dry-run", "-o", "--verbose"},
        new String[]{"create", "demo", "--dry-run", "--output-dir", "--verbose"},
        new String[]{"greet", "Gio", "--log-file", "--debug"}
    )) {
      ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
      ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
      assertEquals(2, runAndGetExitCode(outBuf, errBuf, args),
          "expected exit 2 for args: " + String.join(" ", args));
      assertTrue(errBuf.toString(StandardCharsets.UTF_8).contains("requires"));
    }
    assertFalse(Files.exists(debugProbe));
  }

  @Test
  void invalidPathArgumentsReturnTwoWithoutThrowing() throws Exception {
    for (String[] badArgs : List.of(
        new String[]{"greet", "--log-file", "bad\0path"},
        new String[]{"create", "demo", "-o", "bad\0path"},
        new String[]{"create", "bad\0name"},
        new String[]{"create", ""}
    )) {
      ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
      ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
      assertEquals(2, runAndGetExitCode(outBuf, errBuf, badArgs),
          "expected exit 2 for args: " + String.join(" ", badArgs));
      assertTrue(errBuf.toString(StandardCharsets.UTF_8).contains("error:"));
    }
    if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
      for (String[] winBadArgs : List.of(
          new String[]{"greet", "--log-file", "bad|path"},
          new String[]{"create", "demo", "-o", "bad|path"},
          new String[]{"create", "bad|name"}
      )) {
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
        assertEquals(2, runAndGetExitCode(outBuf, errBuf, winBadArgs),
            "expected exit 2 for Windows args: " + String.join(" ", winBadArgs));
        assertTrue(errBuf.toString(StandardCharsets.UTF_8).contains("error:"));
      }
    }
  }

  @Test
  void createRejectsNonDirectoryOutputAncestorInDryRunAndRealExecution(@TempDir Path tmpDir) throws Exception {
    Path regularFile = Files.writeString(tmpDir.resolve("regular-file.txt"), "data", StandardCharsets.UTF_8);
    for (Path badOutputDir : List.of(regularFile, regularFile.resolve("nested"))) {
      for (String[] args : List.of(
          new String[]{"create", "demo-app", "--dry-run", "-o", badOutputDir.toString()},
          new String[]{"create", "demo-app", "-o", badOutputDir.toString()}
      )) {
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
        assertEquals(1, runAndGetExitCode(outBuf, errBuf, args),
            "expected exit 1 for args: " + String.join(" ", args));
        assertTrue(errBuf.toString(StandardCharsets.UTF_8).contains("error:"));
      }
    }
    assertEquals("data", Files.readString(regularFile, StandardCharsets.UTF_8));
  }

  @Test
  void createInvalidProjectNameReturnsTwoInDryRunAndRealExecution(@TempDir Path tmpDir) throws Exception {
    for (String badName : List.of("123", "9-app", "---", "class", "../escape")) {
      for (String[] args : List.of(
          new String[]{"create", badName, "--dry-run", "-o", tmpDir.toString()},
          new String[]{"create", badName, "-o", tmpDir.toString()}
      )) {
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
        assertEquals(2, runAndGetExitCode(outBuf, errBuf, args),
            "expected exit 2 for args: " + String.join(" ", args));
        assertTrue(errBuf.toString(StandardCharsets.UTF_8).contains("error:"));
      }
    }
    try (Stream<Path> entries = Files.list(tmpDir)) {
      assertEquals(0L, entries.count());
    }
  }
}
