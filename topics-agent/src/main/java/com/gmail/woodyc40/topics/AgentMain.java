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

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.Socket;

public class AgentMain {
    private static final ByteArrayOutputStream accumulator =
            new ByteArrayOutputStream();

    public static void premain(String arg, Instrumentation inst) throws IOException {
        Socket socket = new Socket("127.0.0.1", 5000);

        new Thread(() -> {
            try {
                InputStream is = socket.getInputStream();

                int headerLen = 0;
                int payloadLen = 0;
                int target = -1;
                byte[] header = new byte[4];
                byte[] payload = new byte[1024];
                while (!socket.isClosed()) {
                    headerLen += is.read(header);
                    if (headerLen >= 4) {
                        accumulator.write(header, 4, headerLen - 4);

                        target = (header[0] << 24) +
                                (header[1] << 16) +
                                (header[2] << 8) +
                                header[3];
                    }

                    if (target != -1) {
                        int len;
                        if ((len = is.read(payload)) > -1) {
                            payloadLen += len;
                            accumulator.write(payload, 0, len);
                            if (payloadLen >= target) {
                                DataInputStream in = new DataInputStream(
                                        new ByteArrayInputStream(accumulator.toByteArray()));
                                int id = in.readInt();

                            }
                        } else {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}