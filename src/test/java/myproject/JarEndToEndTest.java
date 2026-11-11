package myproject;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("slow")
class JarEndToEndTest {
  static final String JAR_PATH_PROPERTY = "test.jar.path";
  private static final AtomicInteger RUN_COUNTER = new AtomicInteger();

  private record JarResult(int exitCode, String stdout, String stderr) { }

  private static String readGradleProperty(String key) throws IOException {
    String prefix = key + "=";
    return Files.readString(Path.of("gradle.properties"), StandardCharsets.UTF_8)
        .lines()
        .filter(line -> line.startsWith(prefix))
        .map(line -> line.substring(prefix.length()).strip())
        .findFirst()
        .orElseThrow(() -> new AssertionError("Missing '" + key + "' in gradle.properties"));
  }

  private static String resolveJavaExecutable() {
    boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    Path candidate = Path.of(System.getProperty("java.home", ""), "bin", windows ? "java.exe" : "java");
    if (Files.isRegularFile(candidate)) {
      return candidate.toAbsolutePath().toString();
    }
    return "java";
  }

  private static JarResult runJar(Path tmpDir, String... args) throws Exception {
    String jarProp = System.getProperty(JAR_PATH_PROPERTY);
    assertNotNull(jarProp, "System property '" + JAR_PATH_PROPERTY + "' must point to the built jar");
    Path jarPath = Path.of(jarProp).toAbsolutePath().normalize();
    assertTrue(Files.isRegularFile(jarPath), "Built jar does not exist at: " + jarPath);

    int id = RUN_COUNTER.incrementAndGet();
    Path runCwd = Files.createDirectories(tmpDir.resolve("cwd-" + id));
    Path runHome = Files.createDirectories(tmpDir.resolve("home-" + id));
    Path stdoutFile = tmpDir.resolve("stdout-" + id + ".txt");
    Path stderrFile = tmpDir.resolve("stderr-" + id + ".txt");

    List<String> command = new ArrayList<>();
    command.add(resolveJavaExecutable());
    command.add("-Duser.home=" + runHome.toAbsolutePath());
    command.add("-jar");
    command.add(jarPath.toString());
    command.addAll(List.of(args));

    ProcessBuilder pb = new ProcessBuilder(command);
    pb.directory(runCwd.toFile());
    String runHomePath = runHome.toAbsolutePath().normalize().toString();
    pb.environment().put("HOME", runHomePath);
    pb.environment().put("USERPROFILE", runHomePath);
    pb.environment().put("XDG_CONFIG_HOME", runHome.resolve(".config").toAbsolutePath().normalize().toString());
    pb.environment().put("XDG_CACHE_HOME", runHome.resolve(".cache").toAbsolutePath().normalize().toString());
    pb.environment().put("XDG_DATA_HOME", runHome.resolve(".local/share").toAbsolutePath().normalize().toString());
    pb.environment().put("XDG_STATE_HOME", runHome.resolve(".local/state").toAbsolutePath().normalize().toString());
    pb.environment().put("APPDATA", runHome.resolve("AppData/Roaming").toAbsolutePath().normalize().toString());
    pb.environment().put("LOCALAPPDATA", runHome.resolve("AppData/Local").toAbsolutePath().normalize().toString());
    if (runHomePath.length() >= 2 && runHomePath.charAt(1) == ':') {
      pb.environment().put("HOMEDRIVE", runHomePath.substring(0, 2));
      pb.environment().put("HOMEPATH", runHomePath.substring(2));
    }
    pb.redirectOutput(stdoutFile.toFile());
    pb.redirectError(stderrFile.toFile());

    Process process = pb.start();
    try {
      boolean finished = process.waitFor(60, TimeUnit.SECONDS);
      assertTrue(finished, "Timed out waiting for java -jar process: " + command);
    }
    finally {
      if (process.isAlive()) {
        process.destroyForcibly();
        process.waitFor(5, TimeUnit.SECONDS);
      }
    }

    String stdout = Files.readString(stdoutFile, StandardCharsets.UTF_8);
    String stderr = Files.readString(stderrFile, StandardCharsets.UTF_8);
    return new JarResult(process.exitValue(), stdout, stderr);
  }

  @Test
  void helpExitsZeroWithUsageOnStdoutAndEmptyStderr(@TempDir Path tmpDir) throws Exception {
    JarResult result = runJar(tmpDir, "help");
    assertEquals(0, result.exitCode(), "stderr: " + result.stderr());
    assertEquals("", result.stderr());
    assertTrue(result.stdout().contains("usage: template.java <command> [options]"));
    assertTrue(result.stdout().contains("commands:"));
    assertTrue(result.stdout().contains("options:"));
    assertTrue(result.stdout().contains("exit codes:"));
  }

  @Test
  void versionExitsZeroWithManifestMetadataOnStdoutAndEmptyStderr(@TempDir Path tmpDir) throws Exception {
    String expectedVersion = readGradleProperty("version");
    String expectedCopyright = readGradleProperty("copyright");

    JarResult result = runJar(tmpDir, "version");
    assertEquals(0, result.exitCode(), "stderr: " + result.stderr());
    assertEquals("", result.stderr());
    String line = result.stdout().strip();
    assertTrue(line.startsWith("template.java v" + expectedVersion), "unexpected version line: " + line);
    assertTrue(line.endsWith(" - " + expectedCopyright), "unexpected copyright in: " + line);
    assertFalse(line.contains("unknown"), "manifest metadata fell back to unknown: " + line);
  }

  @Test
  void greetExitsZeroWithGreetingOnStdoutAndEmptyStderr(@TempDir Path tmpDir) throws Exception {
    JarResult result = runJar(tmpDir, "greet", "Gio");
    assertEquals(0, result.exitCode(), "stderr: " + result.stderr());
    assertEquals("", result.stderr());
    assertTrue(result.stdout().contains("Starting template.java v"));
    assertTrue(result.stdout().contains("Hello, Gio"));
    assertTrue(result.stdout().contains("Completed in"));
  }

  @Test
  void invalidCommandExitsTwoWithErrorOnStderrAndUsageOnStdout(@TempDir Path tmpDir) throws Exception {
    JarResult result = runJar(tmpDir, "unknown-cmd");
    assertEquals(2, result.exitCode());
    assertTrue(result.stderr().contains("**[ERROR]** error: unknown command 'unknown-cmd'"));
    assertTrue(result.stdout().contains("usage: template.java <command> [options]"));
  }
}
