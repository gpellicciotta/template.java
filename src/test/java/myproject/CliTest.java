package myproject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliTest {
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
    assertTrue(out.contains("Copyright Giovanni Pellicciotta"));
  }

  @Test
  void versionOption() throws Exception {
    String out = runAndCaptureOut("--version").strip();
    assertTrue(out.startsWith("template.java v"));
    assertTrue(out.contains("Copyright Giovanni Pellicciotta"));
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
  void debugOptionEnablesDebugLevelLogging(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("app.log");
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    runAndGetExitCode(outBuf, errBuf, "--debug", "--log-file", logFile.toString(), "greet", "Gio");

    String logged = Files.readString(logFile, StandardCharsets.UTF_8);
    assertTrue(logged.contains("**[INFO]**"));
    assertTrue(logged.contains("config: debug=true"));
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

    assertEquals(1, exitCode);
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
  void unknownCommandReturnsOne() throws Exception {
    ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
    ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
    int exitCode = runAndGetExitCode(outBuf, errBuf, "unknown-cmd");
    assertEquals(1, exitCode);
    assertTrue(errBuf.toString(StandardCharsets.UTF_8).contains("error: unknown command 'unknown-cmd'"));
    assertTrue(outBuf.toString(StandardCharsets.UTF_8).contains("usage:"));
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
    assertTrue(changelog.contains("Initial release of the Sample App project."));

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
}
