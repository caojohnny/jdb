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
import com.gmail.woodyc40.topics.infra.Status;
import com.gmail.woodyc40.topics.infra.command.CmdProcessor;
import com.sun.jdi.*;

public class Dump implements CmdProcessor {
    @Override
    public String name() {
        return "dump";
    }

    @Override
    public String[] aliases() {
        return new String[] { "d" };
    }

    @Override
    public String help() {
        return "Dumps thread stacks";
    }

    @Override
    public void process(String alias, String[] args) {
        VirtualMachine vm = JvmContext.getContext().getVm();
        if (vm == null) {
            System.out.println("abort: no attached vm");
            return;
        }

        for (ThreadReference t : vm.allThreads()) {
            System.out.println(t.name() + " currently " + Status.of(t.status()));
            try {
                System.out.println("\tHolding lock " + t.currentContendedMonitor());
            } catch (IncompatibleThreadStateException e) {
                System.out.println("\tNo held locks");
            }
            System.out.println("\tSuspended? " + t.isSuspended() + " Breakpoint? " + t.isAtBreakpoint());
            System.out.println("\tStack frames:");
            try {
                for (StackFrame frame : t.frames()) {
                    Location loc = frame.location();
                    System.out.println("\t\t" + loc.declaringType().name() + "." +
                            loc.method().name() + ":" +
                            loc.lineNumber());
                }
            } catch (IncompatibleThreadStateException e) {
                System.out.println("\t\tNo frames");
            }

            System.out.println();
        }
    }
}