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
import com.sun.jdi.*;
import com.sun.jdi.event.BreakpointEvent;

import java.util.Map;

public class Inspect implements CmdProcessor {
    @Override
    public String name() {
        return "inspect";
    }

    @Override
    public String help() {
        return "Inspects the current object state at a breakpoint";
    }

    @Override
    public String[] aliases() {
        return new String[] { "i" };
    }

    @Override
    public void process(String alias, String[] args) {
        try {
            if (args.length == 0) {
                inspect("all");
            } else {
                for (String s : args) {
                    inspect(s);
                }
            }
        } catch (IncompatibleThreadStateException | AbsentInformationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Inspects the variables at the current breakpoint with
     * the given scope between all, stack, and instance
     * visible.
     *
     * @param scope the scope to inspect
     * @throws IncompatibleThreadStateException not sure
     * what this means but it shouldn't happen lol
     * @throws AbsentInformationException if the JVM is not
     * setup to send stack info
     */
    private static void inspect(String scope) throws IncompatibleThreadStateException, AbsentInformationException {
        BreakpointEvent event;
        synchronized (JvmContext.getContext().getLock()) {
            event = JvmContext.getContext().getCurrentBreakpoint();
        }

        if (event == null) {
            System.out.println("no breakpoint");
            return;
        }

        // TODO
        switch (scope) {
            case "all":
                StackFrame frame = event.thread().frame(0);
                for (Map.Entry<LocalVariable, Value> entry : frame.getValues(frame.visibleVariables()).entrySet()) {
                    System.out.println(entry.getKey().name() + " = " + entry.getValue());
                }
                break;
            case "stack":

                break;
            case "class":

                break;
            case "inst":
            case "instance":

                break;
            default:
                System.out.println("unrecognized scope " + scope);
                break;
        }
    }
}