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
import com.sun.jdi.event.BreakpointEvent;

import java.util.concurrent.atomic.AtomicReference;

public class Step implements CmdProcessor {
    @Override
    public String name() {
        return "step";
    }

    @Override
    public String help() {
        return "Causes the thread suspended at a breakpoint to resume";
    }

    @Override
    public void process(String alias, String[] args) {
        AtomicReference<BreakpointEvent> bp = JvmContext.getContext().getCurrentBreakpoint();
        BreakpointEvent event = bp.get();
        if (event == null) {
            System.out.println("no breakpoint");
            return;
        }

        event.thread().resume();
        bp.set(null);
        System.out.println("resumed thread " + event.thread().name());
    }
}