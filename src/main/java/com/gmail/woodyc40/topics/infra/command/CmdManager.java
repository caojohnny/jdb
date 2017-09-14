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
package com.gmail.woodyc40.topics.infra.command;

import com.google.common.collect.Maps;
import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class CmdManager {
    private static final String NOT_FOUND = "command not found";
    private static final Class<?>[] CMD_SIGNATURE =
            new Class[] { String.class, String[].class };
    private final Map<String, CmdProcessor> cmdMap =
            Maps.newHashMap();

    public void register(CmdProcessor processor) {
        Method[] methods = processor.getClass().getDeclaredMethods();
        for (Method me : methods) {
            if (me.getName().equals("process") &&
                    Arrays.equals(me.getParameterTypes(), CMD_SIGNATURE)) {
                Annotation[] annos = me.getDeclaredAnnotations();

                String name = null;
                for (Annotation annotation : annos) {
                    if (annotation instanceof Cmd) {
                        name = ((Cmd) annotation).name();
                    }

                    if (annotation instanceof Alias) {
                        for (String alias : ((Alias) annotation).value()) {
                            this.cmdMap.put(alias, processor);
                        }
                    }
                }

                if (name == null) {
                    throw new IllegalStateException("jdb could not register all commands");
                }

                this.cmdMap.put(name, processor);
            }
        }
    }

    public void dispatch(@NonNull String line) {
        String[] spl = line.split(" ");

        if (spl.length == 1) {
            String name = spl[0];
            CmdProcessor processor = this.cmdMap.get(name);
            if (processor == null) {
                System.out.println(NOT_FOUND);
            } else {
                processor.process(name, new String[0]);
            }
        } else {
            String name = spl[0];
            CmdProcessor processor = this.cmdMap.get(name);
            if (processor == null) {
                System.out.println(NOT_FOUND);
            } else {
                String[] args = new String[spl.length - 1];
                System.arraycopy(spl, 1, args, 0, args.length);

                processor.process(name, args);
            }
        }
    }
}