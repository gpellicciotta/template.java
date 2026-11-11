package myproject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public final class StateLeakGuard implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {
  static final String ISOLATED_HOME_PROPERTY = "myproject.isolated.home";
  static final String REPO_ROOT_PROPERTY = "myproject.repo.root";
  private static final long DEFAULT_GIT_TIMEOUT_MILLIS = 10_000L;

  private static final Set<String> IGNORED_REPO_DIRS = Set.of(
      ".git", ".gradle", "build", "out", "bin", ".venv", "work",
      "__pycache__", ".pytest_cache", ".ruff_cache"
  );

  private static volatile Baseline baseline;

  record EntryStamp(boolean directory, boolean symbolicLink, long size, long modifiedMillis) { }

  public StateLeakGuard() { }

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    ensureBaseline();
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    ensureBaseline();
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    ensureBaseline().verifyFilesystem(context.getDisplayName());
  }

  @Override
  public void afterAll(ExtensionContext context) throws Exception {
    Baseline current = ensureBaseline();
    current.verifyFilesystem(context.getDisplayName());
    current.verifyGitStatus(context.getDisplayName());
  }

  private static Baseline ensureBaseline() throws IOException {
    Baseline existing = baseline;
    if (existing != null) {
      return existing;
    }
    synchronized (StateLeakGuard.class) {
      if (baseline == null) {
        baseline = Baseline.capture();
      }
      return baseline;
    }
  }

  static Baseline captureForTest(Path isolatedHome, Path repoRoot, Map<String, String> env) throws IOException {
    return Baseline.capture(isolatedHome, repoRoot, env);
  }

  static String readGitPorcelainForTest(
      Path repo,
      List<String> command,
      long timeoutMillis,
      AtomicReference<Process> startedProcessOut
  ) {
    return Baseline.readGitPorcelain(repo, command, timeoutMillis, startedProcessOut);
  }

  static Map<String, EntryStamp> snapshotDirectory(Path root, boolean applyRepoExclusions) throws IOException {
    Map<String, EntryStamp> entries = new TreeMap<>();
    if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
      return entries;
    }
    Files.walkFileTree(root, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (dir.equals(root)) {
          return FileVisitResult.CONTINUE;
        }
        Path rel = root.relativize(dir);
        if (applyRepoExclusions && isIgnoredRepoPath(rel)) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        entries.put(normalizeRelative(rel), new EntryStamp(true, false, 0L, 0L));
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        Path rel = root.relativize(file);
        if (applyRepoExclusions && isIgnoredRepoPath(rel)) {
          return FileVisitResult.CONTINUE;
        }
        boolean symlink = attrs.isSymbolicLink();
        long size = symlink ? 0L : attrs.size();
        long modified = symlink ? 0L : attrs.lastModifiedTime().toMillis();
        entries.put(normalizeRelative(rel), new EntryStamp(false, symlink, size, modified));
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) {
        Path rel = root.relativize(file);
        if (applyRepoExclusions && isIgnoredRepoPath(rel)) {
          return FileVisitResult.CONTINUE;
        }
        entries.put(normalizeRelative(rel), new EntryStamp(false, true, 0L, 0L));
        return FileVisitResult.CONTINUE;
      }
    });
    return entries;
  }

  private static boolean isIgnoredRepoPath(Path rel) {
    int count = rel.getNameCount();
    if (count == 0) {
      return false;
    }
    String first = rel.getName(0).toString();
    if (IGNORED_REPO_DIRS.contains(first)) {
      return true;
    }
    if (count >= 2 && ".plan".equals(first) && "work".equals(rel.getName(1).toString())) {
      return true;
    }
    for (int i = 1; i < count; i++) {
      String part = rel.getName(i).toString();
      if ("__pycache__".equals(part) || ".pytest_cache".equals(part) || ".ruff_cache".equals(part)) {
        return true;
      }
    }
    return false;
  }

  private static String normalizeRelative(Path rel) {
    return rel.toString().replace('\\', '/');
  }

  static List<String> diffSnapshots(Map<String, EntryStamp> before, Map<String, EntryStamp> after) {
    List<String> diffs = new ArrayList<>();
    for (Map.Entry<String, EntryStamp> entry : after.entrySet()) {
      EntryStamp prev = before.get(entry.getKey());
      if (prev == null) {
        diffs.add("added: " + entry.getKey());
      }
      else if (!Objects.equals(prev, entry.getValue())) {
        diffs.add("modified: " + entry.getKey());
      }
    }
    for (String key : before.keySet()) {
      if (!after.containsKey(key)) {
        diffs.add("deleted: " + key);
      }
    }
    return diffs;
  }

  static final class Baseline {
    private final Path isolatedHome;
    private final Map<String, EntryStamp> initialHomeEntries;
    private final Path repoRoot;
    private final Map<String, EntryStamp> initialRepoEntries;
    private final String initialGitStatus;

    private Baseline(
        Path isolatedHome,
        Map<String, EntryStamp> initialHomeEntries,
        Path repoRoot,
        Map<String, EntryStamp> initialRepoEntries,
        String initialGitStatus
    ) {
      this.isolatedHome = isolatedHome;
      this.initialHomeEntries = initialHomeEntries;
      this.repoRoot = repoRoot;
      this.initialRepoEntries = initialRepoEntries;
      this.initialGitStatus = initialGitStatus;
    }

    static Baseline capture() throws IOException {
      Path home = resolveIsolatedHome();
      Path userHome = Path.of(System.getProperty("user.home", "")).toAbsolutePath().normalize();
      if (!userHome.startsWith(home)) {
        fail("System property user.home (" + userHome + ") escapes isolated home " + home);
      }
      Path repo = Path.of(System.getProperty(REPO_ROOT_PROPERTY, System.getProperty("user.dir")))
          .toAbsolutePath()
          .normalize();
      return capture(home, repo, System.getenv());
    }

    static Baseline capture(Path home, Path repo, Map<String, String> env) throws IOException {
      Path normalizedHome = home.toAbsolutePath().normalize();
      Path normalizedRepo = repo.toAbsolutePath().normalize();
      validateHomeEnvironment(normalizedHome, env);
      Map<String, EntryStamp> homeEntries = snapshotDirectory(normalizedHome, false);
      Map<String, EntryStamp> repoEntries = snapshotDirectory(normalizedRepo, true);
      String gitStatus = readGitPorcelain(normalizedRepo);
      return new Baseline(normalizedHome, homeEntries, normalizedRepo, repoEntries, gitStatus);
    }

    private static void validateHomeEnvironment(Path isolatedHome, Map<String, String> env) {
      List<String> requiredVars = List.of("HOME", "USERPROFILE", "XDG_CONFIG_HOME", "APPDATA", "LOCALAPPDATA");
      for (String key : requiredVars) {
        String value = env.get(key);
        if (value == null || value.isBlank()) {
          fail("Home environment variable " + key + " must be redirected inside isolated home " + isolatedHome);
        }
        Path resolved = Path.of(value).toAbsolutePath().normalize();
        if (!resolved.startsWith(isolatedHome)) {
          fail("Home environment variable " + key + " (" + resolved + ") escapes isolated home " + isolatedHome);
        }
      }
      for (String key : List.of("XDG_CACHE_HOME", "XDG_DATA_HOME", "XDG_STATE_HOME")) {
        String value = env.get(key);
        if (value != null && !value.isBlank()) {
          Path resolved = Path.of(value).toAbsolutePath().normalize();
          if (!resolved.startsWith(isolatedHome)) {
            fail("Home environment variable " + key + " (" + resolved + ") escapes isolated home " + isolatedHome);
          }
        }
      }
      String homeStr = isolatedHome.toString();
      boolean windowsDriveHome = homeStr.length() >= 2 && homeStr.charAt(1) == ':';
      String homeDrive = env.get("HOMEDRIVE");
      String homePath = env.get("HOMEPATH");
      if (windowsDriveHome || homeDrive != null || homePath != null) {
        String combined = (homeDrive != null ? homeDrive : "") + (homePath != null ? homePath : "");
        if (combined.isBlank()) {
          fail("HOMEDRIVE and HOMEPATH must be redirected inside isolated home " + isolatedHome);
        }
        Path resolved = Path.of(combined).toAbsolutePath().normalize();
        if (!resolved.startsWith(isolatedHome)) {
          fail("HOMEDRIVE/HOMEPATH (" + resolved + ") escapes isolated home " + isolatedHome);
        }
      }
    }

    private static Path resolveIsolatedHome() throws IOException {
      String configured = System.getProperty(ISOLATED_HOME_PROPERTY);
      if (configured != null && !configured.isBlank()) {
        Path path = Path.of(configured).toAbsolutePath().normalize();
        Files.createDirectories(path);
        return path;
      }
      Path fallback = Files.createTempDirectory("myproject-isolated-home-");
      System.setProperty("user.home", fallback.toString());
      Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteTreeQuietly(fallback)));
      return fallback;
    }

    void verifyFilesystem(String contextName) throws IOException {
      List<String> problems = new ArrayList<>();

      Map<String, EntryStamp> currentHome = snapshotDirectory(isolatedHome, false);
      for (String diff : diffSnapshots(initialHomeEntries, currentHome)) {
        problems.add("user.home (" + isolatedHome + ") " + diff);
      }

      Map<String, EntryStamp> currentRepo = snapshotDirectory(repoRoot, true);
      for (String diff : diffSnapshots(initialRepoEntries, currentRepo)) {
        problems.add("repository (" + repoRoot + ") " + diff);
      }

      if (!problems.isEmpty()) {
        fail("State leak detected after " + contextName + ":\n  " + String.join("\n  ", problems));
      }
    }

    void verifyGitStatus(String contextName) throws IOException {
      if (initialGitStatus == null) {
        return;
      }
      String currentGitStatus = readGitPorcelain(repoRoot);
      if (currentGitStatus != null && !initialGitStatus.equals(currentGitStatus)) {
        fail("Repository git status changed during " + contextName
            + ".\nBefore:\n" + initialGitStatus + "\nAfter:\n" + currentGitStatus);
      }
    }

    private static String readGitPorcelain(Path repo) {
      return readGitPorcelain(
          repo,
          List.of("git", "-C", repo.toString(), "--no-optional-locks", "status", "--porcelain", "--untracked-files=all"),
          DEFAULT_GIT_TIMEOUT_MILLIS,
          null
      );
    }

    private static String readGitPorcelain(
        Path repo,
        List<String> command,
        long timeoutMillis,
        AtomicReference<Process> startedProcessOut
    ) {
      if (!Files.exists(repo.resolve(".git"), LinkOption.NOFOLLOW_LINKS)) {
        return null;
      }
      Path outputFile = null;
      Process proc = null;
      try {
        outputFile = Files.createTempFile("state-leak-git-", ".out");
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pb.redirectOutput(outputFile.toFile());
        proc = pb.start();
        proc.getOutputStream().close();
        if (startedProcessOut != null) {
          startedProcessOut.set(proc);
        }
        boolean finished = proc.waitFor(timeoutMillis, TimeUnit.MILLISECONDS);
        if (!finished) {
          terminateAndReap(proc);
          return fail("Git status timed out after " + timeoutMillis + " ms in " + repo + ": " + command);
        }
        int exitCode = proc.exitValue();
        String output = Files.readString(outputFile, StandardCharsets.UTF_8);
        if (exitCode != 0) {
          return fail("Git status exited with code " + exitCode + " in " + repo + ": " + output.strip());
        }
        return output;
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        terminateAndReap(proc);
        return fail("Interrupted while waiting for git status in " + repo, e);
      }
      catch (IOException e) {
        terminateAndReap(proc);
        return fail("Failed to run git status in " + repo + ": " + e.getMessage(), e);
      }
      finally {
        terminateAndReap(proc);
        if (outputFile != null) {
          try {
            Files.deleteIfExists(outputFile);
          }
          catch (IOException ignored) {
            // Best-effort cleanup of temporary git status capture file.
          }
        }
      }
    }

    private static void terminateAndReap(Process proc) {
      if (proc == null) {
        return;
      }
      boolean wasInterrupted = Thread.interrupted();
      try {
        List<ProcessHandle> descendants = proc.descendants().toList();
        for (ProcessHandle child : descendants) {
          child.destroyForcibly();
        }
        if (proc.isAlive()) {
          proc.destroyForcibly();
        }
        proc.waitFor(5, TimeUnit.SECONDS);
        for (ProcessHandle child : descendants) {
          if (child.isAlive()) {
            try {
              child.onExit().get(2, TimeUnit.SECONDS);
            }
            catch (Exception ignored) {
              // Best-effort wait on descendant exit.
            }
          }
        }
      }
      catch (InterruptedException e) {
        wasInterrupted = true;
      }
      finally {
        if (wasInterrupted) {
          Thread.currentThread().interrupt();
        }
      }
    }

    private static void deleteTreeQuietly(Path root) {
      if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
        return;
      }
      try (Stream<Path> walk = Files.walk(root)) {
        walk.sorted(Comparator.reverseOrder()).forEach(path -> {
          try {
            Files.deleteIfExists(path);
          }
          catch (IOException ignored) {
            // Best-effort cleanup for fallback temporary home directory.
          }
        });
      }
      catch (IOException ignored) {
        // Best-effort cleanup for fallback temporary home directory.
      }
    }
  }
}

class StateLeakGuardTest {
  private final AtomicReference<Process> trackedProcess = new AtomicReference<>();

  public static final class EnvProbeMain {
    private EnvProbeMain() { }

    public static void main(String[] args) throws Exception {
      String mode = args[0];
      if ("print".equals(mode)) {
        System.out.println("HOME=" + System.getenv("HOME"));
        System.out.println("USERPROFILE=" + System.getenv("USERPROFILE"));
        System.out.println("XDG_CONFIG_HOME=" + System.getenv("XDG_CONFIG_HOME"));
        System.out.println("APPDATA=" + System.getenv("APPDATA"));
        System.out.println("LOCALAPPDATA=" + System.getenv("LOCALAPPDATA"));
        return;
      }
      if ("write-env-home".equals(mode)) {
        String envVar = args[1];
        String target = System.getenv(envVar);
        if (target == null || target.isBlank()) {
          throw new IllegalStateException("Missing env var: " + envVar);
        }
        Path dir = Path.of(target);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("env-home-leak.txt"), "leaked", StandardCharsets.UTF_8);
      }
    }
  }

  public static final class StalledGitMain {
    private StalledGitMain() { }

    public static void main(String[] args) throws Exception {
      System.out.print("stalling-git-output");
      System.out.flush();
      Thread.sleep(10_000L);
    }
  }

  @AfterEach
  void cleanupTrackedProcess() throws InterruptedException {
    Process proc = trackedProcess.getAndSet(null);
    if (proc != null) {
      proc.descendants().forEach(ProcessHandle::destroyForcibly);
      proc.destroyForcibly();
      proc.waitFor(5, TimeUnit.SECONDS);
    }
  }

  private static String resolveJavaExecutable() {
    boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    Path candidate = Path.of(System.getProperty("java.home", ""), "bin", windows ? "java.exe" : "java");
    if (Files.isRegularFile(candidate)) {
      return candidate.toAbsolutePath().toString();
    }
    return "java";
  }

  private static Map<String, String> isolatedEnvMap(Path isolatedHome) {
    String home = isolatedHome.toAbsolutePath().normalize().toString();
    Map<String, String> env = new LinkedHashMap<>();
    env.put("HOME", home);
    env.put("USERPROFILE", home);
    env.put("XDG_CONFIG_HOME", isolatedHome.resolve(".config").toAbsolutePath().normalize().toString());
    env.put("APPDATA", isolatedHome.resolve("AppData/Roaming").toAbsolutePath().normalize().toString());
    env.put("LOCALAPPDATA", isolatedHome.resolve("AppData/Local").toAbsolutePath().normalize().toString());
    if (home.length() >= 2 && home.charAt(1) == ':') {
      env.put("HOMEDRIVE", home.substring(0, 2));
      env.put("HOMEPATH", home.substring(2));
    }
    return env;
  }

  @Test
  void homeEnvironmentVariablesAndInheritedSubprocessStayInsideIsolatedHome(@TempDir Path tmpDir) throws Exception {
    String configured = System.getProperty(StateLeakGuard.ISOLATED_HOME_PROPERTY);
    assertNotNull(configured, "Expected " + StateLeakGuard.ISOLATED_HOME_PROPERTY + " to be configured");
    Path isolatedHome = Path.of(configured).toAbsolutePath().normalize();
    assertEquals(isolatedHome, Path.of(System.getProperty("user.home")).toAbsolutePath().normalize());

    for (String key : List.of("HOME", "USERPROFILE", "XDG_CONFIG_HOME", "APPDATA", "LOCALAPPDATA")) {
      String value = System.getenv(key);
      assertNotNull(value, "Expected environment variable " + key + " to be set");
      Path resolved = Path.of(value).toAbsolutePath().normalize();
      assertTrue(
          resolved.startsWith(isolatedHome),
          () -> key + "=" + resolved + " escapes isolated home " + isolatedHome
      );
    }

    String homeStr = isolatedHome.toString();
    if (homeStr.length() >= 2 && homeStr.charAt(1) == ':') {
      String drive = System.getenv("HOMEDRIVE");
      String homePath = System.getenv("HOMEPATH");
      assertNotNull(drive, "Expected HOMEDRIVE to be set");
      assertNotNull(homePath, "Expected HOMEPATH to be set");
      assertEquals(isolatedHome, Path.of(drive + homePath).toAbsolutePath().normalize());
    }

    Path outFile = tmpDir.resolve("child-env.txt");
    ProcessBuilder pb = new ProcessBuilder(
        resolveJavaExecutable(),
        "-cp",
        System.getProperty("java.class.path"),
        EnvProbeMain.class.getName(),
        "print"
    );
    pb.directory(tmpDir.toFile());
    pb.redirectErrorStream(true);
    pb.redirectOutput(outFile.toFile());
    Process proc = pb.start();
    trackedProcess.set(proc);
    assertTrue(proc.waitFor(15, TimeUnit.SECONDS), "Timed out waiting for child env probe");
    assertEquals(0, proc.exitValue());

    List<String> lines = Files.readAllLines(outFile, StandardCharsets.UTF_8);
    assertEquals(5, lines.size(), () -> "Unexpected child output: " + lines);
    for (String line : lines) {
      int eq = line.indexOf('=');
      String key = line.substring(0, eq);
      Path resolved = Path.of(line.substring(eq + 1)).toAbsolutePath().normalize();
      assertTrue(
          resolved.startsWith(isolatedHome),
          () -> "Inherited subprocess " + key + "=" + resolved + " escapes isolated home " + isolatedHome
      );
    }
  }

  @Test
  void unisolatedHomeEnvironmentVariableFailsBaselineCapture(@TempDir Path tmpDir) throws Exception {
    Path isolatedHome = Files.createDirectories(tmpDir.resolve("isolated-home"));
    Path externalHome = Files.createDirectories(tmpDir.resolve("external-home"));
    Path repoDir = Files.createDirectories(tmpDir.resolve("repo"));

    for (String key : List.of("HOME", "USERPROFILE", "XDG_CONFIG_HOME", "APPDATA", "LOCALAPPDATA")) {
      Map<String, String> badEnv = isolatedEnvMap(isolatedHome);
      badEnv.put(key, externalHome.toString());
      AssertionFailedError error = assertThrows(
          AssertionFailedError.class,
          () -> StateLeakGuard.captureForTest(isolatedHome, repoDir, badEnv),
          () -> "Expected unisolated " + key + " to fail guard validation"
      );
      assertTrue(error.getMessage().contains(key), () -> "Unexpected message: " + error.getMessage());
    }
  }

  @Test
  void subprocessWritingViaHomeEnvironmentVariablesFailsGuardVerification(@TempDir Path tmpDir) throws Exception {
    for (String envVar : List.of("HOME", "USERPROFILE", "XDG_CONFIG_HOME", "APPDATA")) {
      Path caseDir = Files.createDirectories(tmpDir.resolve("case-" + envVar));
      Path isolatedHome = Files.createDirectories(caseDir.resolve("isolated-home"));
      Path separateJvmHome = Files.createDirectories(caseDir.resolve("separate-jvm-home"));
      Path repoDir = Files.createDirectories(caseDir.resolve("repo"));
      Path childLog = caseDir.resolve("child.log");

      Map<String, String> env = isolatedEnvMap(isolatedHome);
      StateLeakGuard.Baseline baseline = StateLeakGuard.captureForTest(isolatedHome, repoDir, env);

      ProcessBuilder pb = new ProcessBuilder(
          resolveJavaExecutable(),
          "-Duser.home=" + separateJvmHome.toAbsolutePath(),
          "-cp",
          System.getProperty("java.class.path"),
          EnvProbeMain.class.getName(),
          "write-env-home",
          envVar
      );
      pb.directory(caseDir.toFile());
      pb.environment().putAll(env);
      pb.redirectErrorStream(true);
      pb.redirectOutput(childLog.toFile());
      Process proc = pb.start();
      trackedProcess.set(proc);
      assertTrue(proc.waitFor(15, TimeUnit.SECONDS), "Timed out waiting for env write subprocess");
      assertEquals(0, proc.exitValue(), () -> "Subprocess failed for " + envVar);

      AssertionFailedError error = assertThrows(
          AssertionFailedError.class,
          () -> baseline.verifyFilesystem("envWrite(" + envVar + ")")
      );
      assertTrue(error.getMessage().contains("env-home-leak.txt"), () -> "Unexpected error: " + error.getMessage());
    }
  }

  @Test
  @Timeout(value = 2, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
  void stalledGitCommandFailsWithinBoundAndReapsProcess(@TempDir Path tmpDir) throws Exception {
    Files.createDirectory(tmpDir.resolve(".git"));
    List<String> stalledCommand = List.of(
        resolveJavaExecutable(),
        "-cp",
        System.getProperty("java.class.path"),
        StalledGitMain.class.getName()
    );

    AssertionFailedError error = assertThrows(
        AssertionFailedError.class,
        () -> StateLeakGuard.readGitPorcelainForTest(tmpDir, stalledCommand, 300L, trackedProcess)
    );
    assertTrue(
        error.getMessage().toLowerCase(Locale.ROOT).contains("timed out"),
        () -> "Expected timeout message, got: " + error.getMessage()
    );
    Process started = trackedProcess.get();
    assertNotNull(started, "Expected stalled subprocess reference to be captured");
    assertFalse(started.isAlive(), "Expected stalled Git subprocess to be terminated and reaped");
  }

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
  void interruptedGitCommandReapsSubprocessAndPreservesInterruptFlag(@TempDir Path tmpDir) throws Exception {
    Files.createDirectory(tmpDir.resolve(".git"));
    List<String> stalledCommand = List.of(
        resolveJavaExecutable(),
        "-cp",
        System.getProperty("java.class.path"),
        StalledGitMain.class.getName()
    );

    AtomicReference<Throwable> thrown = new AtomicReference<>();
    AtomicReference<Boolean> interruptPreserved = new AtomicReference<>(false);
    Thread worker = new Thread(() -> {
      try {
        StateLeakGuard.readGitPorcelainForTest(tmpDir, stalledCommand, 10_000L, trackedProcess);
      }
      catch (Throwable t) {
        thrown.set(t);
      }
      finally {
        interruptPreserved.set(Thread.currentThread().isInterrupted());
      }
    });
    worker.start();

    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
    while (trackedProcess.get() == null && System.nanoTime() < deadline) {
      Thread.sleep(20L);
    }
    assertNotNull(trackedProcess.get(), "Expected stalled Git subprocess to start");

    worker.interrupt();
    worker.join(3_000L);
    assertFalse(worker.isAlive(), "Expected interrupted caller thread to terminate promptly");
    assertTrue(interruptPreserved.get(), "Expected caller thread interrupt flag to remain set");
    assertTrue(thrown.get() instanceof AssertionFailedError, () -> "Unexpected failure: " + thrown.get());
    assertFalse(trackedProcess.get().isAlive(), "Expected interrupted Git subprocess to be terminated and reaped");
  }
}
