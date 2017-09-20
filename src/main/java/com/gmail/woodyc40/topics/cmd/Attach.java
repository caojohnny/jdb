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

public class Attach implements CmdProcessor {
    @Override
    public String name() {
        return "attach";
    }

    @Override
    public String help() {
        return "Attaches to a currently running JVM at the given PID";
    }

    @Override
    public void process(String alias, String[] args) {
        if (args.length != 1) {
            System.out.println("attach [PID]");
            return;
        }

        int pid = -1;
        try {
            pid = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("not a number: " + args[0]);
        }

        JvmContext.getContext().attach(pid);
    }
}