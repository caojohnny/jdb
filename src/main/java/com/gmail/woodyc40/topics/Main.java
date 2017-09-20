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

import com.gmail.woodyc40.topics.cmd.Attach;
import com.gmail.woodyc40.topics.cmd.Help;
import com.gmail.woodyc40.topics.cmd.LsJvm;
import com.gmail.woodyc40.topics.cmd.SourcePath;
import com.gmail.woodyc40.topics.infra.command.CmdManager;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Program entry-point, handles command line input/output,
 * sets up JLine reader and registers commands.
 */
public final class Main {
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
        // TODO args
        // Register commands
        CmdManager manager = CmdManager.getInstance();
        manager.register(new LsJvm());
        manager.register(new Help());
        manager.register(new SourcePath());
        manager.register(new Attach());

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