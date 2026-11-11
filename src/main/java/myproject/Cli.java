package myproject;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Sample CLI for template.java. {@link #run} is the testable entry point (returns an exit code, never
 * calls {@link System#exit}); {@link #main} is the real process entry point.
 */
public final class Cli {
  private static final Set<String> COMMANDS = Set.of("help", "version", "greet", "create");
  private static final String FALLBACK_COPYRIGHT = "Copyright (c) unknown";
  private Cli() { }

  /** Application name from the jar manifest, or {@code template.java} when running from compiled classes. */
  public static String name() {
    String n = Cli.class.getPackage().getImplementationTitle();
    return (n != null) ? n : "template.java";
  }

  /** Application version from the jar manifest, or {@code 0.0.0+unknown} when running from compiled classes. */
  public static String version() {
    String v = Cli.class.getPackage().getImplementationVersion();
    return (v != null) ? v : "0.0.0+unknown";
  }

  /** Vendor name from the jar manifest, or {@code unknown} when running from compiled classes. */
  public static String vendor() {
    String v = Cli.class.getPackage().getImplementationVendor();
    return (v != null) ? v : "unknown";
  }

  /** Copyright notice from the jar manifest, or {@code Copyright (c) unknown} when running from compiled classes. */
  public static String copyright() {
    try {
      Path codeSource = Paths.get(Cli.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      return readManifestCopyright(codeSource);
    }
    catch (Exception ignored) {
      return FALLBACK_COPYRIGHT;
    }
  }

  static String readManifestCopyright(Path codeSource) {
    if (codeSource != null && Files.isRegularFile(codeSource)) {
      try (JarFile jar = new JarFile(codeSource.toFile())) {
        Manifest manifest = jar.getManifest();
        if (manifest != null) {
          String value = manifest.getMainAttributes().getValue("Copyright");
          if (value != null && !value.isBlank()) {
            return value;
          }
        }
      }
      catch (IOException ignored) {
        // Fall through to the neutral fallback when the archive cannot be inspected.
      }
    }
    return FALLBACK_COPYRIGHT;
  }

  /** Single-line {@code name vVersion - copyright} text shown by {@code version}. */
  public static String versionLine() {
    return name() + " v" + version() + " - " + copyright();
  }

  /**
   * Builds the usage text.
   *
   * @param verbose whether to include the extended details section
   * @return the usage text
   */
  public static String helpMessage(boolean verbose) {
    StringBuilder sb = new StringBuilder();
    sb.append(versionLine()).append("\n\n");
    sb.append("A minimal starter project and scaffolding CLI for Java and Gradle development.\n\n");
    sb.append("usage: ").append(name()).append(" <command> [options]\n\n");
    sb.append("commands:\n");
    sb.append("  help                              Show this usage info\n");
    sb.append("  version                           Show the current version\n");
    sb.append("  greet [name]                      Print a greeting (sample business logic; default: wereld)\n");
    sb.append("  create <project-name> [-o <dir>]  Create a new project as a renamed copy of this template\n\n");
    sb.append("options:\n");
    sb.append("  -h, --help                        Show this usage info\n");
    sb.append("  --version                         Show the current version\n");
    sb.append("  -v, --verbose                     Emit INFO progress detail (or extended details with help)\n");
    sb.append("  --debug                           Set log threshold to DEBUG and enable --verbose\n");
    sb.append("  --log-file <path>                 Append operational logs to <path>\n");
    sb.append("  --log-level <level>               Set log threshold (DEBUG, INFO, WARN, ERROR; overrides --debug)\n");
    sb.append("  --format <text|json>              Output format for greet and create (default: text)\n");
    sb.append("  -o, --output-dir <dir>            Parent directory for create (default: .)\n");
    sb.append("  --dry-run                         List planned create changes without writing anything\n\n");
    if (verbose) {
      sb.append("details:\n");
      sb.append("  greet:\n");
      sb.append("    Prints 'Hello, <name>' (default: wereld), or a JSON object with --format json.\n");
      sb.append("  create:\n");
      sb.append("    Copies this template tree to <dir>/<project-name>, updates package declarations,\n");
      sb.append("    resets CHANGELOG.md and the gradle.properties version, and leaves out the plan store.\n");
      sb.append("    Pass --dry-run to preview planned changes without writing files.\n");
      sb.append("    Run `plan init --store branch` in the new project after its first bootstrap.\n\n");
    }
    sb.append("exit codes:\n");
    sb.append("  0  Success (including help and version display)\n");
    sb.append("  1  Operational or runtime execution error\n");
    sb.append("  2  Invalid arguments, unknown command, or invalid options\n");
    return sb.toString();
  }

  /**
   * Runs the CLI without terminating the JVM.
   *
   * @param rawArgs command-line arguments
   * @param out standard output stream
   * @param err standard error stream
   * @return the process exit code: 0 on success, 1 on runtime error, 2 on invalid arguments/options
   */
  public static int run(String[] rawArgs, PrintStream out, PrintStream err) {
    Path[] logFileHolder = new Path[1];
    try {
      return parseAndDispatch(rawArgs, out, err, logFileHolder);
    }
    catch (InvalidPathException e) {
      err.println("error: invalid path argument '" + e.getInput() + "': " + e.getReason());
      return 2;
    }
    catch (UncheckedIOException e) {
      err.println("error: failed to write --log-file '" + logFileHolder[0] + "': " + e.getCause().getMessage());
      return 1;
    }
  }

  /** Returns the argument after {@code index} unless it is missing or looks like another option, else empty. */
  private static String nextValue(String[] rawArgs, int index) {
    if (index + 1 >= rawArgs.length || rawArgs[index + 1].startsWith("-")) {
      return "";
    }
    return rawArgs[index + 1];
  }

  private static int parseAndDispatch(String[] rawArgs, PrintStream out, PrintStream err, Path[] logFileHolder) {
    boolean helpRequested = false;
    boolean versionRequested = false;
    boolean verbose = false;
    boolean debug = false;
    Path logFile = null;
    Log.Level explicitLevel = null;
    String format = null;
    String outputDir = null;
    boolean dryRun = false;
    String parseError = null;
    List<String> positionals = new ArrayList<>();

    for (int i = 0; i < rawArgs.length; i++) {
      String a = rawArgs[i];
      if ("-h".equals(a) || "--help".equals(a)) {
        helpRequested = true;
      }
      else if ("--version".equals(a)) {
        versionRequested = true;
      }
      else if ("-v".equals(a) || "--verbose".equals(a)) {
        verbose = true;
      }
      else if ("--debug".equals(a)) {
        debug = true;
      }
      else if ("--log-file".equals(a) || a.startsWith("--log-file=")) {
        String val = a.startsWith("--log-file=")
            ? a.substring("--log-file=".length())
            : nextValue(rawArgs, i);
        if (!a.startsWith("--log-file=") && !val.isEmpty()) {
          i++;
        }
        if (val.isEmpty()) {
          parseError = (parseError != null) ? parseError : "error: '--log-file' requires a path argument";
        }
        else {
          try {
            logFile = Path.of(val);
          }
          catch (InvalidPathException e) {
            logFile = null;
            parseError = (parseError != null) ? parseError
                : "error: invalid --log-file path '" + val + "': " + e.getReason();
          }
        }
      }
      else if ("--log-level".equals(a) || a.startsWith("--log-level=")) {
        String val = a.startsWith("--log-level=")
            ? a.substring("--log-level=".length())
            : ((i + 1 < rawArgs.length) ? rawArgs[++i] : "");
        Log.Level parsed = Log.parseLevel(val);
        if (parsed == null) {
          parseError = (parseError != null) ? parseError
              : "error: invalid --log-level '" + val + "' (expected DEBUG, INFO, WARN, or ERROR)";
        }
        else {
          explicitLevel = parsed;
        }
      }
      else if ("--format".equals(a) || a.startsWith("--format=")) {
        String val = a.startsWith("--format=")
            ? a.substring("--format=".length())
            : ((i + 1 < rawArgs.length) ? rawArgs[++i] : "");
        if (!"text".equals(val) && !"json".equals(val)) {
          parseError = (parseError != null) ? parseError : "error: invalid --format '" + val + "' (expected text or json)";
        }
        else {
          format = val;
        }
      }
      else if ("-o".equals(a) || "--output-dir".equals(a) || a.startsWith("--output-dir=")) {
        String val = a.startsWith("--output-dir=")
            ? a.substring("--output-dir=".length())
            : nextValue(rawArgs, i);
        if (!a.startsWith("--output-dir=") && !val.isEmpty()) {
          i++;
        }
        if (val.isEmpty()) {
          parseError = (parseError != null) ? parseError : "error: '" + a + "' requires a directory argument";
        }
        else {
          try {
            Path.of(val);
            outputDir = val;
          }
          catch (InvalidPathException e) {
            parseError = (parseError != null) ? parseError
                : "error: invalid output directory '" + val + "': " + e.getReason();
          }
        }
      }
      else if ("--dry-run".equals(a)) {
        dryRun = true;
      }
      else if (a.startsWith("-")) {
        parseError = (parseError != null) ? parseError : "error: unknown option '" + a + "'";
      }
      else {
        positionals.add(a);
      }
    }

    logFileHolder[0] = logFile;
    boolean effectiveVerbose = verbose || debug;
    Log.Level effectiveLevel = (explicitLevel != null) ? explicitLevel : (debug ? Log.Level.DEBUG : Log.Level.INFO);
    boolean jsonMode = "json".equals(format);
    Log.configure(effectiveLevel, dryRun ? null : logFile, out, err, jsonMode);
    if (dryRun && logFile != null) {
      Log.warn("--log-file is ignored in a dry run: nothing is written");
    }
    if (parseError != null) {
      Log.error(parseError);
      return 2;
    }

    if (helpRequested) {
      if (!positionals.isEmpty() && !COMMANDS.contains(positionals.get(0))) {
        Log.error("error: unknown command '" + positionals.get(0) + "'");
        return 2;
      }
      out.print(helpMessage(effectiveVerbose));
      return 0;
    }
    if (versionRequested) {
      if (!positionals.isEmpty() || format != null || outputDir != null || dryRun) {
        Log.error("error: '--version' takes no arguments or action options");
        return 2;
      }
      out.println(versionLine());
      return 0;
    }
    if (positionals.isEmpty()) {
      if (format != null || outputDir != null || dryRun) {
        Log.error("error: action options require a command");
        return 2;
      }
      out.print(helpMessage(effectiveVerbose));
      return 0;
    }

    String cmd = positionals.get(0);
    if ("help".equals(cmd)) {
      if (format != null || outputDir != null || dryRun) {
        Log.error("error: 'help' does not accept action options");
        return 2;
      }
      if (positionals.size() > 2 || (positionals.size() == 2 && !COMMANDS.contains(positionals.get(1)))) {
        Log.error("error: unknown command '" + positionals.get(1) + "'");
        return 2;
      }
      out.print(helpMessage(effectiveVerbose));
      return 0;
    }
    if ("version".equals(cmd)) {
      if (positionals.size() > 1 || format != null || outputDir != null || dryRun) {
        Log.error("error: 'version' takes no arguments or action options");
        return 2;
      }
      out.println(versionLine());
      return 0;
    }
    if ("greet".equals(cmd)) {
      if (outputDir != null || dryRun) {
        Log.error("error: '-o/--output-dir' and '--dry-run' are only valid with 'create'");
        return 2;
      }
      if (positionals.size() > 2) {
        Log.error("error: 'greet' accepts at most one name argument");
        return 2;
      }
      String target = (positionals.size() == 2) ? positionals.get(1) : "wereld";
      long startedAt = logStartup(rawArgs, debug, effectiveVerbose, effectiveLevel, logFile);
      Log.debug("Dispatching 'greet' for target '" + target + "'");
      if (effectiveVerbose) {
        Log.info("Greeting target: " + target);
      }
      String greeting = Core.greet(target);
      if (jsonMode) {
        out.println("{\"name\": " + jsonString(target) + ", \"greeting\": " + jsonString(greeting) + "}");
      }
      else {
        out.println(greeting);
      }
      logCompletion(startedAt, 0);
      return 0;
    }
    if ("create".equals(cmd)) {
      if (positionals.size() < 2) {
        Log.error("error: 'create' requires a project-name argument");
        return 2;
      }
      if (positionals.size() > 2) {
        Log.error("error: 'create' accepts only one project-name argument");
        return 2;
      }
      String projectName = positionals.get(1);
      if (projectName.isBlank()) {
        Log.error("error: 'create' requires a non-empty project-name argument");
        return 2;
      }
      String targetDir = (outputDir != null) ? outputDir : ".";
      Path destination;
      try {
        Path namePath = Path.of(projectName);
        if (namePath.isAbsolute()
            || namePath.getNameCount() != 1
            || projectName.indexOf('/') >= 0
            || projectName.indexOf('\\') >= 0
            || ".".equals(projectName)
            || "..".equals(projectName)) {
          Log.error("error: invalid project-name '" + projectName + "': must be a single directory name");
          return 2;
        }
        destination = Paths.get(targetDir).toAbsolutePath().normalize().resolve(namePath);
      }
      catch (InvalidPathException e) {
        Log.error("error: invalid project-name '" + projectName + "': " + e.getReason());
        return 2;
      }
      String packageName = Scaffold.derivePackageName(projectName);
      if (packageName == null) {
        Log.error("error: Cannot derive a valid Java package name from '" + projectName + "'.");
        return 2;
      }
      long startedAt = logStartup(rawArgs, debug, effectiveVerbose, effectiveLevel, logFile);
      Log.debug("Dispatching 'create' for '" + projectName + "' into '" + targetDir + "' (dryRun=" + dryRun + ")");
      if (effectiveVerbose) {
        Log.info("Scaffolding project '" + projectName + "' into '" + targetDir + "'" + (dryRun ? " (dry run)" : ""));
      }
      try {
        return executeCreate(projectName, packageName, targetDir, destination, dryRun, jsonMode, out, startedAt);
      }
      catch (Scaffold.ScaffoldException e) {
        Log.error("error: " + e.getMessage());
        logCompletion(startedAt, 1);
        return 1;
      }
    }

    Log.error("error: unknown command '" + cmd + "'");
    out.print(helpMessage(false));
    return 2;
  }

  private static int executeCreate(
      String projectName,
      String packageName,
      String outputDir,
      Path destination,
      boolean dryRun,
      boolean jsonMode,
      PrintStream out,
      long startedAt
  ) throws Scaffold.ScaffoldException {
    Path templateRoot = Scaffold.findTemplateRoot();
    if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
      throw new Scaffold.ScaffoldException("Destination " + destination + " already exists.");
    }
    for (Path ancestor = destination.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
      if (Files.exists(ancestor, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(ancestor)) {
        throw new Scaffold.ScaffoldException("Output parent " + ancestor + " exists and is not a directory.");
      }
    }
    String title = toTitle(projectName);
    List<String> changes = new ArrayList<>();
    changes.add("Copy template from " + templateRoot + " to " + destination);
    if (!packageName.equals(Scaffold.PACKAGE_NAME)) {
      changes.add("Rename package directory " + Scaffold.PACKAGE_NAME + " to " + packageName
          + " under src/main/java and src/test/java");
    }
    changes.add("Rewrite placeholders (" + Scaffold.PROJECT_SLUG + " -> " + projectName
        + ", " + Scaffold.PACKAGE_NAME + " -> " + packageName + ")");
    changes.add("Write fresh CHANGELOG.md for " + title + " at v0.1.0-pre");
    changes.add("Reset gradle.properties version to 0.1.0-pre");

    if (!dryRun) {
      destination = Scaffold.createProject(projectName, outputDir);
    }
    if (jsonMode) {
      StringBuilder items = new StringBuilder("[");
      for (int i = 0; i < changes.size(); i++) {
        if (i > 0) {
          items.append(", ");
        }
        items.append(jsonString(changes.get(i)));
      }
      items.append("]");
      out.println("{\"project-name\": " + jsonString(projectName)
          + ", \"package-name\": " + jsonString(packageName)
          + ", \"destination\": " + jsonString(destination.toString())
          + ", \"dry-run\": " + dryRun
          + ", \"changes\": " + items + "}");
    }
    else if (dryRun) {
      out.println("Dry run: would create new project at " + destination);
      for (String change : changes) {
        out.println("  - " + change);
      }
    }
    else {
      out.println("Created new project at " + destination);
    }
    logCompletion(startedAt, 0);
    return 0;
  }

  private static String toTitle(String projectName) {
    String[] words = projectName.replace('-', ' ').replace('_', ' ').split(" +");
    StringBuilder title = new StringBuilder();
    for (String word : words) {
      if (!word.isEmpty()) {
        if (title.length() > 0) {
          title.append(' ');
        }
        title.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase(Locale.ROOT));
      }
    }
    return title.toString();
  }

  static String jsonString(String value) {
    StringBuilder sb = new StringBuilder("\"");
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\b' -> sb.append("\\b");
        case '\f' -> sb.append("\\f");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        default -> {
          if (c < 0x20) {
            sb.append(String.format("\\u%04x", (int) c));
          }
          else {
            sb.append(c);
          }
        }
      }
    }
    return sb.append('"').toString();
  }

  // Startup/completion logging is only wired into operational commands (greet, create): help and version stay
  // single-line or unlogged.
  private static long logStartup(
      String[] args, boolean debugEnabled, boolean verbose, Log.Level level, Path logFile
  ) {
    Log.info("Starting " + name() + " v" + version() + "\n"
        + "  command: " + String.join(" ", args) + "\n"
        + "  config: debug=" + debugEnabled + ", verbose=" + verbose + ", log-level=" + level
        + ", log-file=" + (logFile == null ? "(none)" : logFile));
    return System.currentTimeMillis();
  }

  private static void logCompletion(long startedAt, int exitCode) {
    Log.info("Completed in " + (System.currentTimeMillis() - startedAt) + "ms (exit code " + exitCode + ")");
  }

  /**
   * Process entry point.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    System.exit(run(args, System.out, System.err));
  }
}
