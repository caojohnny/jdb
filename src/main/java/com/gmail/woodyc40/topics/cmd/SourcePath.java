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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SourcePath implements CmdProcessor {
    @Override
    public String name() {
        return "sourcepath";
    }

    @Override
    public String help() {
        return "Sets the source path to find classes loaded by the JVM";
    }

    @Override
    public String[] aliases() {
        return new String[] { "sp" };
    }

    @Override
    public void process(String alias, String[] args) {
        if (args.length != 1) {
            System.out.println("sourcepath [path]");
            return;
        }

        Path path = Paths.get(args[0]);
        if (!Files.exists(path)) {
            System.out.println("no path found");
        } else {
            JvmContext.getContext().getSourcePath().add(path);
        }
    }
}