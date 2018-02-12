/*
 * JDB - Java Debugger
 * Copyright 2017 Johnny Cao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gmail.woodyc40.topics;

import com.gmail.woodyc40.topics.cmd.*;
import com.gmail.woodyc40.topics.infra.JvmContext;
import com.gmail.woodyc40.topics.infra.Platform;
import com.gmail.woodyc40.topics.infra.command.CmdManager;
import com.gmail.woodyc40.topics.protocol.SignalRegistry;
import com.gmail.woodyc40.topics.server.AgentServer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Program entry-point, handles command line input/output,
 * sets up JLine reader and registers commands.
 */
public final class Main {
    /**
     * --spawnjoin [xyz.bat]
     * -sj [xyz.bat]
     * Spawns a process and joins, allowing for the program
     * to exit before continuing with the debugger.
     */
    private static final ArgParser SPAWN_PROC_JOIN = ArgParser.newParser("spawnjoin", "sj", s -> {
        Path path = Paths.get(s);
        if (!Files.exists(path)) {
            System.out.println(s + " does not point to a file");
            return;
        }

        try {
            System.out.print("Started process to " + s + "... ");
            if (Platform.isWindows()) {
                new ProcessBuilder(s).start().waitFor();
            } else {
                new ProcessBuilder("sh", s).start().waitFor();
            }

            System.out.println("Completed.");
            System.out.println();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    });
    /**
     * --spawn [xyz.bat]
     * -s [xyz.bat]
     * Spawns a process before beginning the debugger
     */
    private static final ArgParser SPAWN_PROC = ArgParser.newParser("spawn", "s", s -> {
        Path path = Paths.get(s);
        if (!Files.exists(path)) {
            System.out.println(s + " does not point to a file");
            return;
        }

        try {
            if (Platform.isWindows()) {
                new ProcessBuilder(s).start();
            } else {
                new ProcessBuilder("sh", s).start();
            }

            System.out.println("Started process to " + s);
            System.out.println();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });
    /**
     * (Flag)
     * --closeondetach
     * -c
     * <p>If this flag is available, then the JVM which is
     * attached to the debugger will call exit(3) when
     * detached rather than waiting for a new connection.
     * </p>
     * <p>If the JVM is started via the SPAWN_PROC* command
     * line arguments, then it is recommended to have this
     * flag as well.</p>
     */
    // private static final ArgParser CLOSE_ON_DETACH = ArgParser.newFlag("closeondetach", "c",
    //        JvmContext.getContext()::setCloseOnDetach);
    /**
     * --sourcepath [path]
     * -sp [path]
     * The source path(s) to add to the debugger
     */
    private static final ArgParser SP = ArgParser.newParser("sourcepath", "sp", s -> {
        String[] paths = s.split(";");
        CmdManager.getInstance().getCmdByType(SourcePath.class).process(null, paths);
        System.out.println();
    });
    /**
     * (Flag)
     * --getprocess
     * -p
     * Prints out the available process IDs when starting
     */
    private static final ArgParser PRINT_PROCS = ArgParser.newFlag("getprocess", "p", flag -> {
        if (flag) {
            System.out.println("Available processes:");
            CmdManager.getInstance().getCmdByType(LsJvm.class).process(null, null);
            System.out.println();
        }
    });
    /**
     * (Flag)
     * --print-signals
     * -ps
     * Prints all the signals
     */
    private static final ArgParser PRINT_SIGNALS = ArgParser.newFlag("print-signals", "ps", s -> SignalRegistry.print());
    /** The terminal interface being used */
    private static final Terminal TERM;
    /** The terminal line reader */
    private static final LineReader READER;
    /**
     * (Flag)
     * --auto
     * -a
     * Enabling this flag allows the debugger to find the
     * first (non-deterministic) JVM that supports debugging
     * and attaches to it.
     */
    private static final ArgParser AUTO_ATTACH = ArgParser.newFlag("auto", "a", flag -> {
        if (flag) {
            try {
                System.out.print("Attempting to attach automatically... ");
                Map<Integer, String> pids = LsJvm.getAvailablePids();
                for (Map.Entry<Integer, String> entry : pids.entrySet()) {
                    if (entry.getValue().contains("-agentlib:jdwp")) {
                        System.out.println();
                        JvmContext.getContext().attach(entry.getKey());
                        break;
                    }
                }

                synchronized (JvmContext.getContext()) {
                    if (JvmContext.getContext().getCurrentPid() == -1) {
                        System.out.println("Failed.");
                    }
                }

                System.out.println();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    });
    private static int port = 5000;
    /**
     * --port
     * -p
     * Sets the port on which to begin the agent server
     */
    private static final ArgParser SERVER_PORT = ArgParser.newParser("port", "port",
            s -> port = Integer.parseInt(s));

    static {
        try {
            TERM = TerminalBuilder.
                    builder().
                    jansi(true).
                    build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        READER = LineReaderBuilder.
                builder().
                terminal(TERM).
                build();
    }

    /**
     * Main entry method.
     */
    public static void main(String[] args) {
        // Register commands
        CmdManager manager = CmdManager.getInstance();
        manager.register(new LsJvm());
        manager.register(new Help());
        manager.register(new SourcePath());
        manager.register(new Attach());
        manager.register(new Detach());
        manager.register(new BreakAfter());
        manager.register(new Enter());
        manager.register(new Step());
        manager.register(new Inspect());
        manager.register(new ClearBreaks());
        manager.register(new Exit());
        manager.register(new InspectVar());
        manager.register(new Returns());
        manager.register(new Dump());
        manager.register(new Proceed());
        manager.register(new See());

        PRINT_SIGNALS.parse(args);
        SPAWN_PROC_JOIN.parse(args);
        SPAWN_PROC.parse(args);
        SERVER_PORT.parse(args);

        AgentServer client = AgentServer.initServer(port);
        JvmContext.init(client);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Process terminating");
            JvmContext.getContext().detach();
            client.close();
        }, "Detach hook"));

        PrintStream out = new PrintStream(TERM.output());
        System.setOut(out);

        SP.parse(args);
        // CLOSE_ON_DETACH.parse(args);
        PRINT_PROCS.parse(args);
        AUTO_ATTACH.parse(args);

        // CLI Handling
        while (true) {
            String line = READER.readLine("(jdb) ");

            if (line == null || line.isEmpty()) {
                continue;
            }

            if (line.equals("e") || line.equals("exit")) {
                JvmContext.getContext().detach();
                client.close();
                return;
            }

            manager.dispatch(line);
            System.out.println();
        }
    }

    /**
     * Prints an a message asynchronously without messing
     * with the command prompt.
     *
     * @param line the line to print
     */
    public static void printAsync(String line) {
        READER.callWidget(LineReader.CLEAR);
        READER.getTerminal().writer().println(line);
        READER.callWidget(LineReader.REDRAW_LINE);
        READER.callWidget(LineReader.REDISPLAY);
        READER.getTerminal().writer().flush();
    }

    /**
     * Prompts the CLI for input and returns the entered
     * String.
     *
     * @param query the query string
     * @return the input string
     */
    public static String prompt(String query) {
        while (true) {
            String line = READER.readLine(query);
            if (line != null && !line.isEmpty()) {
                return line;
            }
        }
    }
}