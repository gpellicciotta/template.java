package myproject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogTest {
  private final ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errBuf = new ByteArrayOutputStream();

  private void configure(boolean debug, Path logFile) throws Exception {
    Log.configure(debug, logFile, new PrintStream(outBuf, true, "UTF-8"), new PrintStream(errBuf, true, "UTF-8"));
  }

  private void configure(boolean debug, Path logFile, Clock clock) throws Exception {
    Log.configure(debug, logFile, new PrintStream(outBuf, true, "UTF-8"), new PrintStream(errBuf, true, "UTF-8"), clock);
  }

  @AfterEach
  void resetToRealStreams() {
    Log.reset();
  }

  @Test
  void infoOmitsIndicatorAndTimestampOnConsole() throws Exception {
    configure(false, null);
    Log.info("hello there");
    assertEquals("hello there" + System.lineSeparator(), outBuf.toString(StandardCharsets.UTF_8));
  }

  @Test
  void warnAndErrorKeepIndicatorOnConsoleButNoTimestamp() throws Exception {
    configure(false, null);
    Log.warn("careful");
    Log.error("boom");
    String errOut = errBuf.toString(StandardCharsets.UTF_8);
    assertTrue(errOut.contains("**[WARN]**  careful"));
    assertTrue(errOut.contains("**[ERROR]** boom"));
    assertFalse(errOut.matches("(?s).*\\d{4}-\\d{2}-\\d{2}.*"));
  }

  @Test
  void debugIsDiscardedUnlessEnabled() throws Exception {
    configure(false, null);
    Log.debug("hidden");
    assertEquals("", outBuf.toString(StandardCharsets.UTF_8));

    configure(true, null);
    Log.debug("visible");
    assertTrue(outBuf.toString(StandardCharsets.UTF_8).contains("**[DEBUG]** visible"));
  }

  @Test
  void fileEntriesArePrefixedWithTimestampAndIndicator(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("app.log");
    configure(false, logFile);
    Log.info("started up");

    String logged = Files.readString(logFile, StandardCharsets.UTF_8);
    assertTrue(logged.matches("(?s)^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\] \\*\\*\\[INFO\\]\\*\\*\\s+started up.*"));
  }

  @Test
  void fileEntriesUseUtcTimestampsRegardlessOfClockZone(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("app.log");
    Clock nonUtcClock = Clock.fixed(Instant.parse("2026-11-11T23:15:30Z"), ZoneOffset.ofHours(2));
    configure(false, logFile, nonUtcClock);
    Log.info("started up");

    String logged = Files.readString(logFile, StandardCharsets.UTF_8);
    assertEquals("[2026-11-11 23:15:30] **[INFO]**  started up" + System.lineSeparator(), logged);
  }

  @Test
  void multiLineMessagesIndentContinuationsToMatchFirstLine() throws Exception {
    configure(false, null);
    Log.warn("first line\nsecond line");
    String[] lines = errBuf.toString(StandardCharsets.UTF_8).split("\\R");
    assertEquals("**[WARN]**  first line", lines[0]);
    int indent = lines[0].indexOf("first line");
    assertEquals(" ".repeat(indent) + "second line", lines[1]);
  }

  @Test
  void originIsRenderedInAngleBrackets(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("app.log");
    configure(false, logFile);
    Log.info("Scaffold", "copying files");

    String logged = Files.readString(logFile, StandardCharsets.UTF_8);
    assertTrue(logged.contains("<Scaffold> copying files"));
  }

  @Test
  void levelThresholdFiltersConsoleAndFileIdentically(@TempDir Path tmpDir) throws Exception {
    Path logFile = tmpDir.resolve("app.log");
    Log.configure(Log.Level.WARN, logFile, new PrintStream(outBuf, true, "UTF-8"),
        new PrintStream(errBuf, true, "UTF-8"), false);

    Log.debug("debug detail");
    Log.info("info detail");
    Log.warn("warn detail");
    Log.error("error detail");

    assertEquals("", outBuf.toString(StandardCharsets.UTF_8));
    String errOut = errBuf.toString(StandardCharsets.UTF_8);
    assertTrue(errOut.contains("**[WARN]**  warn detail"));
    assertTrue(errOut.contains("**[ERROR]** error detail"));
    assertFalse(errOut.contains("info detail"));
    assertFalse(errOut.contains("debug detail"));

    String fileOut = Files.readString(logFile, StandardCharsets.UTF_8);
    assertTrue(fileOut.contains("**[WARN]**  warn detail"));
    assertTrue(fileOut.contains("**[ERROR]** error detail"));
    assertFalse(fileOut.contains("info detail"));
    assertFalse(fileOut.contains("debug detail"));
  }

  @Test
  void allToStderrRoutesInfoAndDebugToErrorStream() throws Exception {
    Log.configure(Log.Level.DEBUG, null, new PrintStream(outBuf, true, "UTF-8"),
        new PrintStream(errBuf, true, "UTF-8"), true);

    Log.info("info on err");
    Log.debug("debug on err");

    assertEquals("", outBuf.toString(StandardCharsets.UTF_8));
    String errOut = errBuf.toString(StandardCharsets.UTF_8);
    assertTrue(errOut.contains("info on err"));
    assertTrue(errOut.contains("**[DEBUG]** debug on err"));
  }

  @Test
  void concurrentConfigurationsStayIsolatedAcrossThreads(@TempDir Path tmpDir) throws Exception {
    int threads = 4;
    CyclicBarrier barrier = new CyclicBarrier(threads);
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    try {
      List<Callable<Void>> tasks = new ArrayList<>();
      for (int i = 0; i < threads; i++) {
        int id = i;
        tasks.add(() -> {
          ByteArrayOutputStream threadOut = new ByteArrayOutputStream();
          ByteArrayOutputStream threadErr = new ByteArrayOutputStream();
          Path threadLog = tmpDir.resolve("thread-" + id + ".log");
          boolean debugMode = (id % 2 == 0);
          try {
            Log.configure(debugMode, threadLog,
                new PrintStream(threadOut, true, "UTF-8"),
                new PrintStream(threadErr, true, "UTF-8"));
            barrier.await();
            Log.info("msg-" + id);
            Log.debug("dbg-" + id);
            barrier.await();

            String console = threadOut.toString(StandardCharsets.UTF_8);
            String file = Files.readString(threadLog, StandardCharsets.UTF_8);
            assertTrue(console.contains("msg-" + id));
            assertEquals(debugMode, console.contains("dbg-" + id));
            for (int other = 0; other < threads; other++) {
              if (other != id) {
                assertFalse(console.contains("msg-" + other));
                assertFalse(file.contains("msg-" + other));
              }
            }
          }
          finally {
            Log.reset();
          }
          return null;
        });
      }
      for (Future<Void> future : pool.invokeAll(tasks)) {
        future.get();
      }
    }
    finally {
      pool.shutdownNow();
    }
  }
}
