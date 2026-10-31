/**
 * Split into single-purpose classes rather than one grab-bag: {@link myproject.Cli} owns argument
 * parsing and is the only place that talks to {@link java.io.PrintStream}/exit codes, split into a testable
 * {@code run(args, out, err)} entry point and a thin {@code main} that just wires it to real
 * {@code System.out}/{@code System.exit}; {@link myproject.Core} holds the actual business logic, kept free of
 * CLI/IO concerns so it's usable and testable on its own; {@link myproject.Scaffold} is the self-copy-and-rename
 * logic behind the {@code create} subcommand, kept separate since it's template-specific machinery rather than
 * business logic a project built from this template would keep; {@link myproject.Log} is a minimal,
 * dependency-free reference logging implementation (formatted lines, {@code --debug}/{@code --log-file})
 * deliberately kept as a single global sink rather than a named-logger SPI — grow into one (see
 * support.java's {@code com.pellicciotta.support.logging} package for that shape) only once a template-derived project
 * actually needs multiple back-ends or per-component loggers.
 *
 * <p>If your own project's packages grow non-obvious design decisions worth recording, this is the place —
 * next to the code it describes, rather than a README fragment that drifts out of sync.
 */
package myproject;
