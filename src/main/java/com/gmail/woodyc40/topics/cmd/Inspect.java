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
        if (args.length == 0) {
            try {
                inspect("all");
            } catch (IncompatibleThreadStateException | AbsentInformationException e) {
                throw new RuntimeException(e);
            }
        } else {
            for (String s : args) {
                try {
                    inspect(s);
                } catch (IncompatibleThreadStateException | AbsentInformationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static void inspect(String scope) throws IncompatibleThreadStateException, AbsentInformationException {
        BreakpointEvent event = JvmContext.getContext().getCurrentBreakpoint().get();
        if (event == null) {
            System.out.println("no breakpoint");
            return;
        }

        // TODO
        if (scope.equals("all")) {
            StackFrame frame = event.thread().frame(0);
            for (Map.Entry<LocalVariable, Value> entry : frame.getValues(frame.visibleVariables()).entrySet()) {
                System.out.println(entry.getKey().name() + " = " + entry.getValue());
            }
        } else if (scope.equals("stack")) {

        } else if (scope.equals("class")) {

        } else if (scope.equals("inst") || scope.equals("instance")) {

        } else {
            System.out.println("unrecognized scope " + scope);
        }
    }
}
