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
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.Value;
import com.sun.jdi.event.BreakpointEvent;

import java.util.Map;
import java.util.Queue;

public class Returns implements CmdProcessor {
    @Override
    public String name() {
        return "returns";
    }

    @Override
    public String[] aliases() {
        return new String[] { "r" };
    }

    @Override
    public String help() {
        return "Views return values before the current breakpoint";
    }

    @Override
    public void process(String alias, String[] args) {
        JvmContext context = JvmContext.getContext();
        BreakpointEvent breakpointEvent;
        synchronized (context.getLock()) {
            breakpointEvent = context.getCurrentBreakpoint();
        }

        if (breakpointEvent == null) {
            System.out.println("abort: no breakpoint");
            return;
        }

        Method method = breakpointEvent.location().method();
        if (method == null) {
            System.out.println("abort: breakpoint has no previous calls");
            return;
        }

        boolean found = false;
        Queue<Map.Entry<Location, Value>> returns = JvmContext.getContext().getReturns();
        for (Map.Entry<Location, Value> aReturn : returns) {
            Location loc = aReturn.getKey();
            if (true) {
                found = true;

                System.out.println(loc.declaringType().name() + " = " + aReturn.getValue());
            }
        }

        if (!found) {
            System.out.println("No returns found");
        }
    }
}