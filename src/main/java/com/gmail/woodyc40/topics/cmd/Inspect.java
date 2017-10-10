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

        StackFrame frame = event.thread().frame(0);
        if (frame == null) {
            throw new RuntimeException("invalid frame");
        }

        Location location = frame.location();
        ReferenceType ref = location.declaringType();
        System.out.println("Inspect breakpoint @ " + location.method() + ':' + location.lineNumber());
        System.out.println();

        switch (scope) {
            case "a":
            case "all":
                for (Map.Entry<LocalVariable, Value> entry : frame.getValues(frame.visibleVariables()).entrySet()) {
                    LocalVariable var = entry.getKey();
                    if (entry.getValue() == null) {
                        continue;
                    }
                    System.out.println(trimPackage(var.typeName()) + ' ' + var.name() + " = " + entry.getValue());
                }

                for (Field field : ref.allFields()) {
                    ReferenceType type = field.declaringType();
                    if (!field.isStatic() && !canInspectInst(frame)) {
                        continue;
                    }

                    if (!type.equals(ref)) {
                        System.out.println('(' + trimPackage(type.name()) + ") " + (field.isStatic() ? "static " : "") + trimPackage(field.typeName()) +
                                ' ' + field.name() + " = " + type.getValue(field));
                    } else {
                        System.out.println((field.isStatic() ? "static " : "") + trimPackage(field.typeName()) + ' ' + field.name() + " = " + ref.getValue(field));
                    }
                }
                break;
            case "s":
            case "stack":
                for (Map.Entry<LocalVariable, Value> entry : frame.getValues(frame.visibleVariables()).entrySet()) {
                    LocalVariable var = entry.getKey();
                    if (entry.getValue() == null) {
                        continue;
                    }
                    System.out.println(trimPackage(var.typeName()) + ' ' + var.name() + " = " + entry.getValue());
                }
                break;
            case "c":
            case "cls":
            case "class":
                for (Field field : ref.allFields()) {
                    if (!field.isStatic()) {
                        continue;
                    }

                    ReferenceType type = field.declaringType();
                    if (!type.equals(ref)) {
                        System.out.println('(' + trimPackage(type.name()) + ") static " + trimPackage(field.typeName()) +
                                ' ' + field.name() + " = " + type.getValue(field));
                    } else {
                        System.out.println("static " + trimPackage(field.typeName()) + ' ' + field.name() + " = " + ref.getValue(field));
                    }
                }
                break;
            case "inst":
            case "instance":
                if (!canInspectInst(frame)) {
                    System.out.println("abort: cannot inspect instance from static context");
                    break;
                }

                for (Field field : ref.allFields()) {
                    if (field.isStatic()) {
                        continue;
                    }

                    ReferenceType type = field.declaringType();
                    if (!type.equals(ref)) {
                        System.out.println('(' + trimPackage(type.name()) + ") " + trimPackage(field.typeName()) +
                                ' ' + field.name() + " = " + type.getValue(field));
                    } else {
                        System.out.println(trimPackage(field.typeName()) + ' ' + field.name() + " = " + ref.getValue(field));
                    }
                }
                break;
            default:
                System.out.println("unrecognized scope " + scope);
                break;
        }
    }

    /**
     * Removes the package from an FQN name, if it exists.
     *
     * @param typeName the type name to trim
     * @return the trimmed string
     */
    public static String trimPackage(String typeName) {
        int idx = typeName.lastIndexOf('.');
        return idx > -1 ? typeName.substring(idx + 1) : typeName;
    }

    /**
     * Determines whether or not a given frame can be
     * inspected for instance variables.
     *
     * @param frame the frame to check
     * @return {@code true} if instance fields can be
     * inspected
     */
    public static boolean canInspectInst(StackFrame frame) {
        Method method = frame.location().method();
        return method != null && !method.isStatic() && !method.isStaticInitializer();
    }
}