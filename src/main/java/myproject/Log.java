package myproject;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Minimal reference logging implementation, just enough to satisfy the cross-project logging guideline:
 * formatted log lines to stdout/stderr and an optional append-only log file, gated by a configurable
 * {@link Level} threshold (defaulting to {@link Level#INFO}) and supporting stderr-only routing for JSON output.
 * Deliberately a single global sink rather than a named-logger SPI (see support.java's
 * {@code com.pellicciotta.support.logging} package for that) - grow into one only once a template-derived project
 * actually needs multiple back-ends or per-component loggers.
 */
public final class Log {
  private Log() { }

  /** Severity of a log line, from most to least severe. */
  public enum Level {
    /** A failure that stops the operation. */
    ERROR,
    /** A problem the operation recovered from. */
    WARN,
    /** Normal operational progress. */
    INFO,
    /** Diagnostic detail, emitted only when the threshold is {@code DEBUG}. */
    DEBUG
  }

  private static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

  private static volatile Level threshold = Level.INFO;
  private static volatile Path logFile = null;
  private static volatile PrintStream out = System.out;
  private static volatile PrintStream err = System.err;
  private static volatile boolean stderrOnly = false;
  private static volatile Clock clock = Clock.systemUTC();

  /**
   * (Re)configure the sink; called once per {@code Cli.run} so state never leaks across invocations.
   *
   * @param debug when {@code true}, sets threshold to {@link Level#DEBUG}; otherwise {@link Level#INFO}
   * @param file optional log file to append to, or {@code null}
   * @param outStream standard output stream
   * @param errStream standard error stream
   */
  public static void configure(boolean debug, Path file, PrintStream outStream, PrintStream errStream) {
    configure(debug ? Level.DEBUG : Level.INFO, file, outStream, errStream, false);
  }

  /**
   * (Re)configure the sink with an explicit level threshold and optional stderr-only console routing.
   *
   * @param minLevel minimum severity emitted to console and {@code file} (defaults to {@link Level#INFO} if {@code null})
   * @param file optional log file to append to, or {@code null}
   * @param outStream standard output stream
   * @param errStream standard error stream
   * @param allToErr when {@code true}, routes {@code INFO} and {@code DEBUG} console lines to {@code errStream}
   */
  public static void configure(Level minLevel, Path file, PrintStream outStream, PrintStream errStream, boolean allToErr) {
    configure(minLevel, file, outStream, errStream, allToErr, Clock.systemUTC());
  }

  /**
   * (Re)configure the sink with a debug switch and an explicit clock; timestamps are formatted in UTC regardless of clock zone.
   *
   * @param debug when {@code true}, sets threshold to {@link Level#DEBUG}; otherwise {@link Level#INFO}
   * @param file optional log file to append to, or {@code null}
   * @param outStream standard output stream
   * @param errStream standard error stream
   * @param timeSource clock supplying log file timestamps
   */
  public static void configure(
      boolean debug, Path file, PrintStream outStream, PrintStream errStream, Clock timeSource) {
    configure(debug ? Level.DEBUG : Level.INFO, file, outStream, errStream, false, timeSource);
  }

  /**
   * (Re)configure every sink setting; timestamps are formatted in UTC regardless of clock zone.
   *
   * @param minLevel minimum severity emitted to console and {@code file} (defaults to {@link Level#INFO} if {@code null})
   * @param file optional log file to append to, or {@code null}
   * @param outStream standard output stream
   * @param errStream standard error stream
   * @param allToErr when {@code true}, routes {@code INFO} and {@code DEBUG} console lines to {@code errStream}
   * @param timeSource clock supplying log file timestamps
   */
  public static void configure(
      Level minLevel, Path file, PrintStream outStream, PrintStream errStream, boolean allToErr, Clock timeSource) {
    threshold = (minLevel != null) ? minLevel : Level.INFO;
    logFile = file;
    out = outStream;
    err = errStream;
    stderrOnly = allToErr;
    clock = Objects.requireNonNull(timeSource, "timeSource");
  }

  /**
   * Parses a case-insensitive log level name ({@code DEBUG}, {@code INFO}, {@code WARN}, {@code ERROR}).
   *
   * @param value level text to parse
   * @return the matching {@link Level}, or {@code null} if unrecognized
   */
  public static Level parseLevel(String value) {
    if (value == null) {
      return null;
    }
    return switch (value.trim().toUpperCase(Locale.ROOT)) {
      case "ERROR" -> Level.ERROR;
      case "WARN" -> Level.WARN;
      case "INFO" -> Level.INFO;
      case "DEBUG" -> Level.DEBUG;
      default -> null;
    };
  }

  /** Logs an error message. */
  public static void error(String message) {
    write(Level.ERROR, null, message);
  }

  /** Logs an error message attributed to an origin. */
  public static void error(String origin, String message) {
    write(Level.ERROR, origin, message);
  }

  /** Logs a warning message. */
  public static void warn(String message) {
    write(Level.WARN, null, message);
  }

  /** Logs a warning message attributed to an origin. */
  public static void warn(String origin, String message) {
    write(Level.WARN, origin, message);
  }

  /** Logs an informational message. */
  public static void info(String message) {
    write(Level.INFO, null, message);
  }

  /** Logs an informational message attributed to an origin. */
  public static void info(String origin, String message) {
    write(Level.INFO, origin, message);
  }

  /** Logs a debug message; discarded when the threshold is above {@link Level#DEBUG}. */
  public static void debug(String message) {
    write(Level.DEBUG, null, message);
  }

  /** Logs a debug message attributed to an origin; discarded when the threshold is above {@link Level#DEBUG}. */
  public static void debug(String origin, String message) {
    write(Level.DEBUG, origin, message);
  }

  // Fixed-width (11 chars) so all four line up once followed by a single separating space.
  private static String indicator(Level level) {
    return switch (level) {
      case ERROR -> "**[ERROR]**";
      case WARN -> "**[WARN]** ";
      case INFO -> "**[INFO]** ";
      case DEBUG -> "**[DEBUG]**";
    };
  }

  // Continuation lines are indented to the column where the first line's message text started.
  private static String withIndentedContinuations(String prefix, String message) {
    if (message.indexOf('\n') < 0) {
      return prefix + message;
    }
    String[] lines = message.split("\n", -1);
    String continuationIndent = " ".repeat(prefix.length());
    StringBuilder sb = new StringBuilder(prefix).append(lines[0]);
    for (int i = 1; i < lines.length; i++) {
      sb.append('\n').append(continuationIndent).append(lines[i]);
    }
    return sb.toString();
  }

  private static synchronized void write(Level level, String origin, String message) {
    if (level.compareTo(threshold) > 0) {
      return;
    }
    String originTag = (origin == null || origin.isEmpty()) ? "" : "<" + origin + "> ";

    // Console: no timestamp, and INFO carries no severity indicator at all.
    String consolePrefix = (level == Level.INFO) ? originTag : (indicator(level) + " " + originTag);
    PrintStream stream = (stderrOnly || level == Level.ERROR || level == Level.WARN) ? err : out;
    stream.println(withIndentedContinuations(consolePrefix, message));

    Path file = logFile;
    if (file != null) {
      String timestamp = "[" + TIMESTAMP_FORMAT.format(clock.instant()) + "] ";
      String filePrefix = timestamp + indicator(level) + " " + originTag;
      String fileLine = withIndentedContinuations(filePrefix, message);
      try {
        Files.writeString(file, fileLine + System.lineSeparator(), StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      }
      catch (IOException e) {
        throw new UncheckedIOException("failed to append to log file " + file, e);
      }
    }
  }
}
