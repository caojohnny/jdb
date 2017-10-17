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

import com.gmail.woodyc40.topics.infra.command.CmdManager;
import com.gmail.woodyc40.topics.infra.command.CmdProcessor;

public class Help implements CmdProcessor {
    @Override public String name() {
        return "help";
    }

    @Override public String help() {
        return "Displays helps messages for commands";
    }

    @Override public String[] aliases() {
        return new String[] { "h" };
    }

    @Override public void process(String alias, String[] args) {
        CmdManager manager = CmdManager.getInstance();
        if (args.length == 0) {
            manager.getCmdMap().forEach((k, v) -> {
                String name = v.name();
                StringBuilder aliases = new StringBuilder();
                String[] procAliases = v.aliases();
                for (String s : procAliases) {
                    if (s.equals(k)) {
                        return;
                    }
                    aliases.append(s).append(',');
                }
                if (procAliases.length > 0) {
                    aliases.deleteCharAt(aliases.lastIndexOf(","));
                }

                System.out.println(name + (procAliases.length > 0 ? " (" + aliases + ") - " : " - ") + v.help());
            });
        } else {
            String cmdName = args[0];
            CmdProcessor processor = manager.getCmdMap().get(cmdName);
            if (processor == null) {
                System.out.println("No command found: " + cmdName);
            } else {
                String name = processor.name();
                StringBuilder aliases = new StringBuilder();
                String[] procAliases = processor.aliases();
                for (String s : procAliases) {
                    aliases.append(s).append(',');
                }
                if (procAliases.length > 0) {
                    aliases.deleteCharAt(aliases.lastIndexOf(","));
                }

                System.out.println(name + (procAliases.length > 0 ? " (" + aliases + ") - " : " - ") + processor.help());
            }
        }
    }
}