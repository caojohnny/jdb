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
package com.gmail.woodyc40.topics.protocol;

import com.google.common.base.Charsets;
import lombok.RequiredArgsConstructor;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Sent to the client in order to request for the JVM to
 * exit.
 * SCHEMA:
 * - int:exitCode
 * - int:messageLength
 * - byte[]:message
 */
@RequiredArgsConstructor
public class SignalOutExit implements SignalOut {
    private final int exitCode;
    private final String message;

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(this.exitCode);
        out.writeInt(this.message.length());
        out.write(this.message.getBytes(Charsets.UTF_16));
    }
}