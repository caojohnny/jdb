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
import java.util.List;

/*
 * Schema:
 * cls/mename
 *   int:stringLen
 *   byte[]:stringData
 * args
 *   int:len
 *     int:stringLen
 *     byte[]:stringData
 */
@RequiredArgsConstructor
public class SignalOutReqMethod implements SignalOut {
    private final String cls;
    private final String mename;
    private final List<String> args;

    @Override
    public void write(DataOutputStream out) throws IOException {
        writeString(out, this.cls);
        writeString(out, this.mename);

        out.writeInt(this.args.size());
        for (String arg : this.args) {
            writeString(out, arg);
        }
    }

    private static void writeString(DataOutputStream out, String s) throws IOException {
        out.writeInt(s.length());
        out.write(s.getBytes(Charsets.UTF_16));
    }
}