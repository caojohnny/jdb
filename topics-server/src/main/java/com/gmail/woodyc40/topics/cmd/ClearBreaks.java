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
import com.sun.jdi.request.BreakpointRequest;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

public class ClearBreaks implements CmdProcessor {
    /**
     * Removes and disables all of the breakpoints from the
     * scope defined by the filter function.
     *
     * @param filter whether or not to disable a given
     * breakpoint
     */
    private static void clearBreaks(Function<BreakpointRequest, Boolean> filter) {
        Map<String, BreakpointRequest> breakpoints = JvmContext.getContext().getBreakpoints();
        for (Iterator<Map.Entry<String, BreakpointRequest>> it = breakpoints.entrySet().iterator();
             it.hasNext(); ) {
            Map.Entry<String, BreakpointRequest> next = it.next();
            if (filter.apply(next.getValue())) {
                next.getValue().disable();
                it.remove();
                System.out.println("Clear break at " + next.getKey());
            }
        }
    }

    @Override
    public String name() {
        return "clearbreaks";
    }

    @Override
    public String help() {
        return "Clears a breakpoint a given point, file, or all";
    }

    @Override
    public String[] aliases() {
        return new String[] { "cb" };
    }

    @Override
    public void process(String alias, String[] args) {
        if (args.length == 0) {
            ReferenceType current = JvmContext.getContext().getCurrentRef();
            if (current != null) {
                System.out.println("Clear break from " + current.name());
                clearBreaks(req -> req.location().declaringType().equals(current));
            } else {
                System.out.println("Clear all breaks");
                clearBreaks(req -> true);
            }
        } else if (args.length == 1) {
            String scope = args[0];

            if (scope.equals("all")) {
                System.out.println("Clear all breaks");
                clearBreaks(req -> true);
            } else {
                if (scope.contains(":")) {
                    BreakpointRequest rem = JvmContext.getContext().getBreakpoints().remove(scope);
                    if (rem != null) {
                        System.out.println("Clear break at " + scope);
                        rem.disable();
                    } else {
                        System.out.println("No break found at " + scope);
                    }
                } else {
                    int lnumber = -1;
                    try {
                        lnumber = Integer.parseInt(scope);
                    } catch (NumberFormatException e) {
                    }

                    if (lnumber == -1) {
                        ReferenceType reference = Enter.getReference(scope);
                        if (reference == null) {
                            System.out.println("no reference for " + scope);
                        } else {
                            System.out.println("Clear breaks from " + reference.name());
                            clearBreaks(req -> req.location().declaringType().equals(reference));
                        }
                    } else {
                        ReferenceType current = JvmContext.getContext().getCurrentRef();
                        if (current != null) {
                            String key = current.name() + ':' + lnumber;
                            BreakpointRequest rem = JvmContext.getContext().getBreakpoints().remove(key);
                            if (rem != null) {
                                System.out.println("Clear break from " + key);
                                rem.disable();
                            }
                        } else {
                            System.out.println("no class entered");
                        }
                    }
                }
            }
        } else {
            System.out.println("clearbreaks");
        }
    }
}