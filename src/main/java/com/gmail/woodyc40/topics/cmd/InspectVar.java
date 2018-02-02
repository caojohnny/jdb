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
    /**
     * Finds the variable by the given name in the context
     * of a breakpoint.
     *
     * @param varName the name of the variable to find
     * @throws IncompatibleThreadStateException probably
     * a thread that is unsuspended or something lol
     */
    private static Var findVar(String varName) throws IncompatibleThreadStateException, AbsentInformationException, ClassNotLoadedException {
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
        ObjectReference obj = frame.thisObject();
        ReferenceType ref = location.declaringType();
        String className = Inspect.trimPackage(ref.name());

        if (!varName.startsWith("this.") || !varName.startsWith(className + '.')) {
            String var = firstVar(varName);
            varName = varName.replace(var, "");

            for (LocalVariable variable : frame.visibleVariables()) {
                if (variable.name().equals(var)) {
                    return nextVar(Var.newVar(variable, frame), varName);
                }
            }

            for (Field field : ref.allFields()) {
                if (field.name().equals(var)) {
                    return nextVar(Var.newVar(obj, field, frame), varName);
                }
            }
            System.out.println("No varname for " + var);
        } else {
            varName = varName.replaceFirst("this.", "");
            varName = varName.replaceFirst(className + '.', "");
            String var = firstVar(varName);
            varName = var.replaceFirst(var, "");

            for (Field field : ref.allFields()) {
                if (field.name().equals(var)) {
                    return nextVar(Var.newVar(null, field, frame), varName);
                }
            }
            System.out.println("No varname for " + var);
        }

        return null;
    }

    private static Var nextVar(Var value, String varName) throws ClassNotLoadedException {
        if (varName.isEmpty()) {
            return value;
        }

        if (value.getType() instanceof PrimitiveType) {
            System.out.println("Cannot inspect deeper within primitive type");
            return null;
        }

        String var = firstVar(varName);
        varName = var.replaceFirst(var, "");

        for (Field field : ((ReferenceType) value.getType()).allFields()) {
            if (field.name().equals(var)) {
                return nextVar(Var.newVar((ObjectReference) value.getValue(), field, value.getFrame()), varName);
            }
        }
        System.out.println("No varname for " + var);
        return null;
    }

    /**
     * Inspects the given variable, printing its state to
     * the CLI.
     *
     * @param var the variable to inspect
     * @param scope the scope to inspect the variables
     */
    private static void inspect(Var var, String scope) {
        System.out.println("Inspecting " +
                var.getType().name() + ' ' +
                var.getName() + " @ " +
                Inspect.trimPackage(var.getLocation().declaringType().name()) + ':' +
                var.getLocation().lineNumber());
        System.out.println();

        if (var.getValue() instanceof PrimitiveValue) {
            System.out.println(var.getName() + " = " + Inspect.processValue(var.getValue()));
        } else {
            ObjectReference obj = (ObjectReference) var.getValue();
            for (Field field : obj.referenceType().allFields()) {
                if (field.isStatic()) {
                    if (scope.equals("inst") || scope.equals("i")) {
                        continue;
                    }
                } else {
                    if (scope.equals("class") || scope.equals("cls") || scope.equals("c")) {
                        continue;
                    }
                }

                System.out.println(Inspect.inspect(field, obj.referenceType(), obj));
            }
        }
    }

    /**
     * Obtains the first variable name in the given name
     * string which separates period notation.
     *
     * @param varName the varName to find
     * @return the single variable name
     */
    private static String firstVar(String varName) {
        int sepIdx = varName.indexOf('.');
        if (sepIdx == -1) {
            return varName;
        } else {
            return varName.substring(0, sepIdx);
        }
    }

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
            } else if (args.length == 1) {
                Var var = findVar(args[0]);
                if (var != null) {
                    inspect(var, "all");
                }
            } else {
                Var var = findVar(args[0]);
                if (var != null) {
                    inspect(var, args[1]);
                }
            }
        } catch (IncompatibleThreadStateException | AbsentInformationException | ClassNotLoadedException e) {
            throw new RuntimeException(e);
        }
    }
}