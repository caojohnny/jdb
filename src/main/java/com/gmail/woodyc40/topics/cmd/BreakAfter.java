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
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;

public class BreakAfter implements CmdProcessor {
    @Override
    public String name() {
        return "ba";
    }

    @Override
    public String help() {
        return "Sets a breakpoint after a given line";
    }

    @Override
    public void process(String alias, String[] args) {
        if (args.length != 1) {
            System.out.println("ba <file:>[line]");
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

        VirtualMachine vm = JvmContext.getContext().getVm();
        EventRequestManager manager = vm.eventRequestManager();

        try {
            for (Location location : type.locationsOfLine(lineNumber)) {
                BreakpointRequest req = manager.createBreakpointRequest(location);
                req.enable();
                JvmContext.getContext().getBreakpoints().put(
                        location.sourceName() + ':' + lineNumber, req);

                System.out.println("Breakpoint after " + type.name() + ":" + lineNumber);
                System.out.println();

                String line = JvmContext.getContext().lookupLine(type.name(), lineNumber, 1);
                if (line != null && !line.isEmpty()) {
                    System.out.println("Code sample:");
                    System.out.println(line);
                }
            }
        } catch (AbsentInformationException e) {
            throw new RuntimeException(e);
        }
    }
}