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
package com.gmail.woodyc40.topics.infra;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Platform details.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Platform {
    /**
     * Checks to determine whether the platform which this
     * debugger is running is Windows.
     *
     * @return {@code true} if windows
     */
    public static boolean isWindows() {
        String systemProp = System.getProperty("os.name");
        return systemProp.toLowerCase().contains("win");
    }
}