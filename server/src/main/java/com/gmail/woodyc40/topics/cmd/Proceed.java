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
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

public class Proceed implements CmdProcessor {
    @Override
    public String name() {
        return "proceed";
    }

    @Override
    public String help() {
        return "Causes the given thread to resume";
    }

    @Override
    public String[] aliases() {
        return new String[] { "resume", "cont" };
    }

    @Override
    public void process(String alias, String[] args) {
        if (args.length != 1) {
            args = new String[] { "main" };
        }

        JvmContext context = JvmContext.getContext();
        VirtualMachine vm = context.getVm();

        for (ThreadReference threadReference : vm.allThreads()) {
            if (threadReference.name().equals(args[0])) {
                threadReference.resume();
                System.out.println("Attempted to resume " + args[0]);
                return;
            }
        }

        System.out.println("No thread by the name: " + args[0]);
    }
}