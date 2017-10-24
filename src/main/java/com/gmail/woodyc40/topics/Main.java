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
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Program entry-point, handles command line input/output,
 * sets up JLine reader and registers commands.
 */
public final class Main {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    });
    private static final ArgParser SP = ArgParser.newParser("sourcepath", "sp", s -> {
        String[] paths = s.split(";");
        CmdManager.getInstance().getCmdByType(SourcePath.class).process(null, paths);
    });
    private static final ArgParser PRINT_PROCS = ArgParser.newFlag("getprocess", "pp", flag -> {
        if (flag) {
            System.out.println("Available processes:");
            CmdManager.getInstance().getCmdByType(LsJvm.class).process(null, null);
            System.out.println();
        }
    });

    /** The terminal interface being used */
    private static final Terminal TERM;
    /** The terminal line reader */
    private static final LineReader READER;

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

        SPAWN_PROC.parse(args);
        SP.parse(args);
        PRINT_PROCS.parse(args);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> JvmContext.getContext().detach(true)));

        PrintStream out = new PrintStream(TERM.output());
        System.setOut(out);

        // CLI Handling
        while (true) {
            String line = READER.readLine("(jdb) ");

            if (line == null || line.isEmpty()) {
                continue;
            }

            if (line.equals("e") || line.equals("exit")) {
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