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

import com.gmail.woodyc40.topics.Main;
import com.gmail.woodyc40.topics.infra.JvmContext;
import com.gmail.woodyc40.topics.infra.command.CmdProcessor;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;

import java.util.ArrayList;
import java.util.List;

public class Enter implements CmdProcessor {
    @Override
    public String name() {
        return "enter";
    }

    @Override
    public String help() {
        return "Enters a class of the given name";
    }

    @Override
    public void process(String alias, String[] args) {
        if (args.length != 1) {
            System.out.println("enter [file]");
            return;
        }

        ReferenceType type = getReference(args[0]);
        if (type != null) {
            System.out.println("enter: " + type.name());
            JvmContext.getContext().setCurrentRef(type);
        }
    }

    /**
     * Obtains a class reference type from the given name.
     *
     * @param name the name of the reference to find
     * @return the reference type as represented in the JDI
     */
    public static ReferenceType getReference(String name) {
        VirtualMachine vm = JvmContext.getContext().getVm();
        List<ReferenceType> matches = new ArrayList<>();
        for (ReferenceType type : vm.allClasses()) {
            if (type.name().contains(name)) {
                matches.add(type);
            }
        }

        if (matches.isEmpty()) {
            System.out.println("abort: no class for " + name);
        } else if (matches.size() == 1) {
            return matches.get(0);
        } else {
            System.out.println("multiple matches for " + name);
            for (int i = 0; i < matches.size(); i++) {
                System.out.println(i + ": " + matches.get(i).name());
            }

            System.out.println();
            String num = Main.prompt("Select class from above: ");
            int idx = Integer.parseInt(num);
            if (idx >= matches.size() || idx < 0) {
                System.out.println("abort: index out of bounds");
            } else {
                return matches.get(idx);
            }
        }
        return null;
    }
}