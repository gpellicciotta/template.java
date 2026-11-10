package myproject;

import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample CLI for template.java. {@link #run} is the testable entry point (returns an exit code, never
 * calls {@link System#exit}); {@link #main} is the real process entry point.
 */
public final class Cli {
  private Cli() { }

  /** Application name from the jar manifest, or {@code template.java} when running from compiled classes. */
  public static String name() {
    String n = Cli.class.getPackage().getImplementationTitle();
    // Same jar-manifest-only caveat as version() below: Implementation-Title only resolves when running from
    // the built jar, not when running compiled classes directly (e.g. `gradlew run` or the test suite).
    return (n != null) ? n : "template.java";
  }

  /** Application version from the jar manifest, or {@code 0.0.0+unknown} when running from compiled classes. */
  public static String version() {
    String v = Cli.class.getPackage().getImplementationVersion();
    // Package.getImplementationVersion() only resolves when running from a jar with a manifest (e.g. the
    // installed distribution) - it's null when running compiled classes directly (e.g. `gradlew run`,
    // an IDE run configuration, or the test suite).
    return (v != null) ? v : "0.0.0+unknown";
  }

  /** Vendor name from the jar manifest, or the template author when running from compiled classes. */
  public static String vendor() {
    String v = Cli.class.getPackage().getImplementationVendor();
    // Same jar-manifest-only caveat as version()/name(): Implementation-Vendor only resolves when running
    // from the built jar, not when running compiled classes directly (e.g. `gradlew run` or the test suite).
    return (v != null) ? v : "Giovanni Pellicciotta";
  }

  /** Single-line {@code name vVersion - Copyright vendor} text shown by {@code version}. */
  public static String versionLine() {
    return name() + " v" + version() + " - Copyright " + vendor();
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
    sb.append("  --verbose                         Show extended usage guidelines\n");
    sb.append("  --debug                           Enable debug-level logging\n");
    sb.append("  --log-file <path>                 Append operational logs to <path>\n\n");
    if (verbose) {
      sb.append("details:\n");
      sb.append("  create:\n");
      sb.append("    Copies this template tree to <dir>/<project-name>, updates package declarations,\n");
      sb.append("    resets CHANGELOG.md and the gradle.properties version, and leaves out the plan store.\n");
      sb.append("    Run `plan init --store branch` in the new project after its first bootstrap.\n\n");
    }
    sb.append("exit codes:\n");
    sb.append("  0  Success (including help and version display)\n");
    sb.append("  1  Error (invalid arguments or execution failure)\n");
    return sb.toString();
  }

  /**
   * Runs the CLI without terminating the JVM.
   *
   * @param rawArgs command-line arguments, including the global {@code --debug} and {@code --log-file} options
   * @param out standard output stream
   * @param err standard error stream
   * @return the process exit code: 0 on success, 1 on error
   */
  public static int run(String[] rawArgs, PrintStream out, PrintStream err) {
    boolean debugEnabled = false;
    Path logFile = null;
    List<String> remaining = new ArrayList<>();
    for (int i = 0; i < rawArgs.length; i++) {
      String a = rawArgs[i];
      if ("--debug".equals(a)) {
        debugEnabled = true;
      }
      else if ("--log-file".equals(a) && (i + 1 < rawArgs.length)) {
        logFile = Path.of(rawArgs[++i]);
      }
      else {
        remaining.add(a);
      }
    }
    Log.configure(debugEnabled, logFile, out, err);
    String[] args = remaining.toArray(new String[0]);

    try {
      return dispatch(args, out, err, debugEnabled, logFile);
    }
    catch (UncheckedIOException e) {
      err.println("error: failed to write --log-file '" + logFile + "': " + e.getCause().getMessage());
      return 1;
    }
  }

  private static int dispatch(String[] args, PrintStream out, PrintStream err, boolean debugEnabled, Path logFile) {
    if (args.length == 0) {
      out.print(helpMessage(false));
      return 0;
    }

    String firstArg = args[0];
    if ("help".equals(firstArg) || "--help".equals(firstArg) || "-h".equals(firstArg)) {
      boolean verbose = false;
      for (int i = 1; i < args.length; i++) {
        if ("--verbose".equals(args[i])) {
          verbose = true;
          break;
        }
      }
      out.print(helpMessage(verbose));
      return 0;
    }

    if ("version".equals(firstArg) || "--version".equals(firstArg)) {
      out.println(versionLine());
      return 0;
    }

    if ("greet".equals(firstArg)) {
      long startedAt = logStartup(args, debugEnabled, logFile);
      String name = (args.length > 1) ? args[1] : "wereld";
      out.println(Core.greet(name));
      logCompletion(startedAt, 0);
      return 0;
    }

    if ("create".equals(firstArg)) {
      long startedAt = logStartup(args, debugEnabled, logFile);
      if (args.length < 2) {
        Log.error("error: 'create' requires a project-name argument");
        logCompletion(startedAt, 1);
        return 1;
      }
      String projectName = args[1];
      String outputDir = ".";
      for (int i = 2; i < args.length; i++) {
        if (("-o".equals(args[i]) || "--output-dir".equals(args[i])) && (i + 1 < args.length)) {
          outputDir = args[++i];
        }
      }
      try {
        Path destination = Scaffold.createProject(projectName, outputDir);
        out.println("Created new project at " + destination);
        logCompletion(startedAt, 0);
        return 0;
      }
      catch (Scaffold.ScaffoldException e) {
        Log.error("error: " + e.getMessage());
        logCompletion(startedAt, 1);
        return 1;
      }
    }

    Log.error("error: unknown command '" + firstArg + "'");
    out.print(helpMessage(false));
    return 1;
  }

  // Startup/completion logging is only wired into the "operational" commands (greet, create): help/version
  // are meta-commands that must stay single-line and scriptable, so they never emit log lines.
  private static long logStartup(String[] args, boolean debugEnabled, Path logFile) {
    Log.info("Starting " + name() + " v" + version() + "\n"
        + "  command: " + String.join(" ", args) + "\n"
        + "  config: debug=" + debugEnabled + ", log-file=" + (logFile == null ? "(none)" : logFile));
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
