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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * Command infrastructure management class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CmdManager {
    //
    // CONSTANTS
    //
    /** Command not found constant String */
    private static final String NOT_FOUND = "command not found: ";
    /** Command handling method signature */
    private static final Class<?>[] CMD_SIGNATURE =
            new Class[] { String.class, String[].class };
    /** Singleton instance of the command manager */
    @Getter private static final CmdManager instance = new CmdManager();

    //
    // Instance fields
    //
    /**
     * The collection of registered commands/command
     * aliases mapped to their respective processor
     */
    @Getter private final Map<String, CmdProcessor> cmdMap =
            Maps.newHashMap();

    /**
     * Registers the given command processor to handle CLI
     * input.
     *
     * @param processor the command processor to register
     */
    public void register(CmdProcessor processor) {
        Method[] methods = processor.getClass().getDeclaredMethods();
        for (Method me : methods) {
            if (me.getName().equals("process") &&
                    Arrays.equals(me.getParameterTypes(), CMD_SIGNATURE)) {
                String name = processor.name();
                this.cmdMap.put(name, processor);

                for (String alias : processor.aliases()) {
                    this.cmdMap.put(alias, processor);
                }
            }
        }
    }

    /**
     * Dispatches the given command line and passes it to
     * the command processor.
     *
     * @param line the line to dispatch
     */
    public void dispatch(@NonNull String line) {
        String[] spl = line.split(" ");

        if (spl.length == 1) {
            String name = spl[0];
            CmdProcessor processor = this.cmdMap.get(name);
            if (processor == null) {
                System.out.println(NOT_FOUND + name);
            } else {
                processor.process(name, new String[0]);
            }
        } else {
            String name = spl[0];
            CmdProcessor processor = this.cmdMap.get(name);
            if (processor == null) {
                System.out.println(NOT_FOUND + name);
            } else {
                String[] args = new String[spl.length - 1];
                System.arraycopy(spl, 1, args, 0, args.length);

                processor.process(name, args);
            }
        }
    }

    /**
     * Gets a command by the given type.
     *
     * @param cls the class type to obtain the command
     * @param <T> the command type
     * @return the command class
     */
    @NonNull
    public <T extends CmdProcessor> T getCmdByType(Class<T> cls) {
        for (CmdProcessor processor : this.cmdMap.values()) {
            if (cls.isInstance(processor)) {
                return (T) processor;
            }
        }

        throw new IllegalStateException();
    }
}