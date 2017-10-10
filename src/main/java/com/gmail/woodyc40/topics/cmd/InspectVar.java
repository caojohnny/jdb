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
import com.gmail.woodyc40.topics.infra.Var;
import com.gmail.woodyc40.topics.infra.command.CmdProcessor;
import com.sun.jdi.*;
import com.sun.jdi.event.BreakpointEvent;

public class InspectVar implements CmdProcessor {
    @Override
    public String name() {
        return "inspectvar";
    }

    @Override
    public String help() {
        return "Inspects a variable accessible to the current breakpoint";
    }

    @Override
    public String[] aliases() {
        return new String[] { "iv" };
    }

    @Override
    public void process(String alias, String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("inspectvar [varname] <scope>");
            } if (args.length == 1) {
                Var var = findVar(args[0]);
            } else {

            }
        } catch (IncompatibleThreadStateException | AbsentInformationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds the variable by the given name in the context
     * of a breakpoint.
     *
     * @param varName the name of the variable to find
     * @throws IncompatibleThreadStateException probably
     * a thread that is unsuspended or something lol
     */
    private static Var findVar(String varName) throws IncompatibleThreadStateException, AbsentInformationException {
        BreakpointEvent event;
        synchronized (JvmContext.getContext().getLock()) {
            event = JvmContext.getContext().getCurrentBreakpoint();
        }

        if (event == null) {
            System.out.println("no breakpoint");
            return null;
        }

        StackFrame frame = event.thread().frame(0);
        if (frame == null) {
            throw new RuntimeException("invalid frame");
        }

        Location location = frame.location();
        ReferenceType ref = location.declaringType();
        String className = Inspect.trimPackage(ref.name());

        if (!varName.startsWith("this.") || !varName.startsWith(className + '.')) {
            for (LocalVariable variable : frame.visibleVariables()) {
                if (variable.name().equals(varName)) {
                    return new Var(frame.getValue(variable), location, frame , true);
                }
            }
        } else {
            varName = varName.replace("this.", "");
            varName = varName.replace(className + '.', "");
        }

        for (Field field : ref.allFields()) {
            if (field.name().equals(varName)) {
                if (field.isStatic()) {
                    return new Var(ref.getValue(field), location, frame, false);
                } else {
                    return new Var(frame.thisObject().getValue(field), location, frame, false);
                }
            }
        }

        System.out.println("no var or field for " + varName);
        return null;
    }

    /**
     * Inspects the given variable, printing its state to
     * the CLI.
     *
     * @param var the variable to inspect
     */
    private static void inspect(Var var) {
    }
}