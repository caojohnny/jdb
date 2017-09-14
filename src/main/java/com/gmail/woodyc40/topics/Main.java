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

import com.gmail.woodyc40.topics.cmd.LsJvm;
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
public class Main {
    public static void main(String[] args) throws IOException {
        // TODO args
        // Register commands
        CmdManager manager = new CmdManager();
        manager.register(new LsJvm());

        Terminal terminal = TerminalBuilder.
                builder().
                jansi(true).
                build();
        LineReader reader = LineReaderBuilder.
                builder().
                terminal(terminal).
                build();
        PrintStream out = new PrintStream(terminal.output());
        System.setOut(out);

        // CLI Handling
        while (true) {
            String line = reader.readLine("(jdb) ");

            if (line == null || line.isEmpty()) {
                continue;
            }

            if (line.equals("e") || line.equals("exit")) {
                return;
            }

            manager.dispatch(line);
        }
    }
}