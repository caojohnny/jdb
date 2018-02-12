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

import com.gmail.woodyc40.topics.infra.JvmContext;
import com.gmail.woodyc40.topics.infra.command.CmdProcessor;
import com.sun.jdi.ReferenceType;

public class See implements CmdProcessor {
    @Override
    public String name() {
        return "see";
    }

    @Override
    public String help() {
        return "Peeks at the source for a file (if available)";
    }

    @Override
    public void process(String alias, String[] args) {
        if (args.length != 1) {
            System.out.println("see <file:>[linenumber]");
            return;
        }

        ReferenceType type;
        String parseLn;
        if (args[0].contains(":")) {
            String[] split = args[0].split(":");

            type = Enter.getReference(split[0]);
            parseLn = split[1];
        } else {
            type = JvmContext.getContext().getCurrentRef();
            if (type == null) {
                System.out.println("abort: no class entered");
            }
            parseLn = args[0];
        }

        if (type == null) {
            return;
        }

        int lineNumber;
        try {
            lineNumber = Integer.parseInt(parseLn);
        } catch (NumberFormatException e) {
            System.out.println("abort: " + parseLn + " not a number");
            return;
        }

        String line = JvmContext.getContext().lookupLine(type.name(), lineNumber, 5);
        if (line == null) {
            System.out.println("abort: no source");
            return;
        }

        System.out.println(line);
    }
}
