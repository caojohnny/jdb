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

import com.gmail.woodyc40.topics.infra.Platform;
import com.gmail.woodyc40.topics.infra.command.CmdProcessor;
import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

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
        try {
            Map<Integer, String> availablePids = getAvailablePids();
            for (Map.Entry<Integer, String> entry : availablePids.entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtains the available JVM PIDs running on the current
     * system.
     *
     * @return a mapping of PIDs to process path
     * @throws IOException if the system cannot be polled
     * @throws InterruptedException if the process did not
     * exit
     */
    public static Map<Integer, String> getAvailablePids() throws IOException, InterruptedException {
        Map<Integer, String> availablePids = Maps.newHashMap();
        String[] cmd = Platform.isWindows() ? new String[] { "wmic", "process", "where", "\"name='java.exe'\"", "get", "commandline,processid" } :
                new String[] { "/bin/sh", "-c", "ps -e --format pid,args | grep java" };
        Process ls = new ProcessBuilder().
                command(cmd).
                start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ls.getInputStream()))) {
            if (Platform.isWindows()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty() || line.startsWith("CommandLine")) {
                        continue;
                    }

                    line = line.trim();
                    int point = line.lastIndexOf("  ");
                    if (point > 100) {
                        String pid = line.substring(point + 2, line.length());
                        availablePids.put(Integer.parseInt(pid), line.substring(0, 100) + "...  ");
                    } else {
                        String pid = line.substring(point + 2, line.length());
                        availablePids.put(Integer.parseInt(pid), line.substring(0, point));
                    }
                }
            } else {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty() || line.contains("grep java")) {
                        continue;
                    }

                    line = line.trim();
                    if (line.length() > 100) {
                        int firstSeparator = line.indexOf(' ');
                        String pid = line.substring(0, firstSeparator);
                        availablePids.put(Integer.parseInt(pid), line.substring(firstSeparator + 1, 100) + "...  ");
                    } else {
                        int firstSeparator = line.indexOf(' ');
                        String pid = line.substring(0, firstSeparator);
                        availablePids.put(Integer.parseInt(pid), line.substring(firstSeparator + 1));
                    }
                }
            }
        }

        ls.waitFor();
        ls.destroy();
        return availablePids;
    }
}