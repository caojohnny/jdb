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
package com.gmail.woodyc40.topics;

import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

/**
 * Command-line argument parser that takes in a type/alias
 * with the associated value or flag to produce runtime
 * effects.
 */
@RequiredArgsConstructor
public class ArgParser {
    /** The name using double dashes */
    private final String argName;
    /** The alias, single dash */
    private final String alias;
    /** The parser for String arguments */
    private final Consumer<String> parser;
    /** The consumer for flag/exists? arguments */
    private final Consumer<Boolean> flagConsumer;

    /**
     * Creates a new parser with a given name and alias that
     * handles a String input.
     *
     * @param argName the name of the argument
     * @param alias the argument alias
     * @param parser the parser that takes in a String input
     * @return the new arg parser
     */
    public static ArgParser newParser(String argName, String alias, Consumer<String> parser) {
        return new ArgParser(argName, alias, parser, null);
    }

    /**
     * Creates a new flag parser that checks for existence
     * of a flag.
     *
     * @param argName the argument name
     * @param alias the argument alias
     * @param flagConsumer the consumer handler for the flag
     * @return the new argument parser
     */
    public static ArgParser newFlag(String argName, String alias, Consumer<Boolean> flagConsumer) {
        return new ArgParser(argName, alias, null, flagConsumer);
    }

    public void parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String potentialArg = args[i];
            if (potentialArg.startsWith("-") && this.alias.equals(potentialArg.substring(1))
                    || potentialArg.startsWith("--") && this.argName.equals(potentialArg.substring(2))) {
                if (this.alias.equals(potentialArg.substring(1))) {
                    if (this.parser == null) {
                        this.flagConsumer.accept(true);
                    } else {
                        if (i + 1 == args.length) {
                            System.out.println("Invalid arg for " + potentialArg);
                            throw new RuntimeException();
                        }

                        StringBuilder builder = new StringBuilder();
                        for (int j = i + 1; j < args.length; j++) {
                            String arg = args[j];
                            if (!arg.startsWith("-") && !arg.startsWith("--")) {
                                builder.append(arg).append(' ');
                            }
                        }
                        this.parser.accept(builder.toString().trim());
                    }
                }
            }
        }

        if (this.parser == null) {
            this.flagConsumer.accept(false);
        }
    }
}