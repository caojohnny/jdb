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
package com.gmail.woodyc40.topics.cmd;

import com.gmail.woodyc40.topics.infra.command.CmdProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LsJvm implements CmdProcessor {
    @Override
    public String name() {
        return "lsjvm";
    }

    @Override
    public String help() {
        return "Displays running JVM Processes";
    }

    @Override public void process(String alias, String[] args) {
        String systemProp = System.getProperty("os.name");
        boolean windows = systemProp.toLowerCase().contains("win");

        try {
            String[] cmd = windows ? new String[] { "wmic", "process", "where", "\"name='java.exe'\"", "get", "commandline,processid" } :
                    new String[] { "ps", "-e" }; // TODO grep
            Process ls = new ProcessBuilder().
                    command(cmd).
                    start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(ls.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty() || line.startsWith("CommandLine")) {
                        continue;
                    }

                    line = line.trim();
                    int point = line.lastIndexOf("  ");
                    if (point > 100) {
                        System.out.println(line.substring(point + 2, line.length()) + " - " + line.substring(0, 100) + "...  ");
                    } else {
                        System.out.println(line);
                    }
                }
            }


            ls.waitFor();
            ls.destroy();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}