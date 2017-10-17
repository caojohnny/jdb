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
        ObjectReference obj = frame.thisObject();
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
                    System.out.println(inspect(var, frame));
                }

                for (Field field : ref.allFields()) {
                    if (!canInspectInst(frame)) {
                        continue;
                    }

                    System.out.println(inspect(field, ref, obj));
                }
                break;
            case "s":
            case "stack":
                for (Map.Entry<LocalVariable, Value> entry : frame.getValues(frame.visibleVariables()).entrySet()) {
                    LocalVariable var = entry.getKey();
                    if (entry.getValue() == null) {
                        continue;
                    }
                    System.out.println(inspect(var, frame));
                }
                break;
            case "c":
            case "cls":
            case "class":
                for (Field field : ref.allFields()) {
                    if (!field.isStatic()) {
                        continue;
                    }

                    System.out.println(inspect(field, ref, obj));
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

                    System.out.println(inspect(field, ref, obj));
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
        return method != null && !method.isStatic() &&
                !method.isStaticInitializer() && frame.thisObject() != null;
    }

    /**
     * Obtains a debug String for the given local variable.
     *
     * @param var the variable to debug
     * @param frame the frame to which the variable belongs
     * @return the formatted debug String
     */
    private static String inspect(LocalVariable var, StackFrame frame) {
        return trimPackage(var.typeName()) + ' ' + var.name() + " = " + frame.getValue(var);
    }

    /**
     * Obtains a String formatted inspect line displaying
     * the information associated with a given field.
     *
     * @param field the field to inspect
     * @param type the type containing the field
     * @param obj the instance in question
     * @return a formatted debug String
     */
    public static String inspect(Field field, ReferenceType type, ObjectReference obj) {
        ReferenceType declaringType = field.declaringType();
        if (declaringType.equals(type)) {
            if (field.isStatic()) {
                return "static " + trimPackage(field.typeName()) + ' ' + field.name() + " = " + processValue(type.getValue(field));
            } else {
                return trimPackage(field.typeName()) + ' ' + field.name() + " = " + processValue(obj.getValue(field));
            }
        } else {
            if (field.isStatic()) {
                return "static (" + trimPackage(declaringType.name()) + ") " + trimPackage(field.typeName()) + ' ' + field.name() + " = " + processValue(type.getValue(field));
            } else {
                return '(' + trimPackage(declaringType.name()) + ") " + trimPackage(field.typeName()) + ' ' + field.name() + " = " + processValue(obj.getValue(field));
            }
        }
    }

    public static String processValue(Value value) {
        if (value instanceof StringReference) {
            String str = ((StringReference) value).value();
            StringBuilder builder = new StringBuilder();
            builder.append('"');

            for (int i = 0; i < str.length(); i++) {
                String c = Character.toString(str.charAt(i));
                if (c.equals("\n")) {
                    c = "\\n";
                } else if (c.equals("\b")) {
                    c = "\\b";
                } else if (c.equals("\t")) {
                    c = "\\t";
                } else if (c.equals("\r")) {
                    c = "\\r";
                } else if (c.equals("\f")) {
                    c = "\\f";
                } else if (c.equals("\"")) {
                    c = "\\\"";
                } else if (c.equals("\'")) {
                    c = "\\\'";
                }

                builder.append(c);
            }

            return builder.append('"').toString();
        }

        return String.valueOf(value);
    }
}