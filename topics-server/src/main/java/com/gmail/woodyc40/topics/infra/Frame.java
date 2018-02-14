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

import com.sun.jdi.Location;
import com.sun.jdi.Value;
import lombok.Data;

/**
 * A frame of execution that is used to capture the moment
 * in the stack at which a method is called.
 */
@Data
public class Frame {
    private final Location resultLocation;
    private final Value result;

    private final String callerSignature;
    private final long time;
}