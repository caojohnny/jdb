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

/**
 * The superinterface implemented by command processing
 * handlers.
 */
public interface CmdProcessor {
    /**
     * Obtains the "full" name of the command.
     *
     * @return the command name
     */
    String name();

    /**
     * Obtains the help message for the command.
     *
     * @return the help message
     */
    String help();

    /**
     * Obtains the short-hand aliases for the command. May
     * be left unimplemented.
     *
     * @return the command aliases
     */
    default String[] aliases() {
        return new String[0];
    }

    /**
     * Called by the command handler to process the command
     * when it is called.
     *
     * @param alias the alias used by the user
     * @param args the command arguments
     */
    void process(String alias, String[] args);
}