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

import com.google.common.base.Charsets;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.Socket;
import java.util.Arrays;

public class AgentMain {
    public static void premain(String arg, Instrumentation inst) throws IOException {
        Socket socket = new Socket("127.0.0.1", 5000);

        new Thread(() -> {
            try {
                ByteArrayOutputStream headerAccum =
                        new ByteArrayOutputStream();
                ByteArrayOutputStream accumulator =
                        new ByteArrayOutputStream();

                OutputStream os = socket.getOutputStream();
                DataOutputStream out = new DataOutputStream(os);
                wi(out);

                InputStream is = socket.getInputStream();

                int payloadLen = 0;
                int target = -1;
                byte[] header = new byte[4];
                byte[] payload = new byte[1024];
                while (!socket.isClosed()) {
                    int read = is.read(header);
                    if (read > -1) {
                        headerAccum.write(header, 0, read);
                    }

                    System.out.println(Arrays.toString(headerAccum.toByteArray()));
                    if (headerAccum.size() >= 4) {
                        header = headerAccum.toByteArray();
                        if (headerAccum.size() > 4) {
                            for (int i = 4; i < header.length; i++) {
                                accumulator.write(header[i]);
                            }
                        }

                        target = (int) header[0] << 24;
                        target |= (int) header[1] << 16;
                        target |= (int) header[2] << 8;
                        target |= header[3] & 0xFF;
                    }

                    if (target != -1) {
                        int len;
                        if ((len = is.read(payload)) > -1) {
                            payloadLen += len;
                            accumulator.write(payload, 0, len);
                            if (payloadLen >= target) {
                                DataInputStream in = new DataInputStream(
                                        new ByteArrayInputStream(accumulator.toByteArray()));
                                System.out.println(Arrays.toString(accumulator.toByteArray()));
                                int id = in.readInt();

                                switch (id) {
                                    case 5: // exit
                                        int code = in.readInt();
                                        String msg = rstr(in);
                                        System.out.println("EXIT: " + msg);
                                        System.exit(code);
                                    case 4: // req method
                                        System.out.println();
                                        String clsName = rstr(in);
                                        String meName = rstr(in);
                                        String[] params = new String[in.readInt()];
                                        for (int i = 0; i < params.length; i++) {
                                            params[i] = rstr(in);
                                        }

                                        System.out.println("RECV: Req method: " + clsName + "#" + meName + Arrays.toString(params));
                                        inst.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
                                            try {
                                                wrd(out, classfileBuffer, meName, params);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            return classfileBuffer;
                                        });
                                        inst.retransformClasses(Class.forName(clsName));

                                        break;
                                    case 2: // busy
                                        System.out.println("BUSY");
                                        break;
                                    case 1: // resp init
                                        System.out.println("CONNECTION SUCCESS");
                                        break;
                                    default:
                                        System.out.println("INVALID SIGNAL: " + id);
                                }

                                target = -1;
                                payloadLen = 0;
                                headerAccum = new ByteArrayOutputStream();
                                accumulator = new ByteArrayOutputStream();

                                int headerLen = 0;
                                while (in.available() > 0) {
                                    if (headerLen < 4) {
                                        headerLen += 1;
                                        headerAccum.write((int) in.readByte());
                                    } else {
                                        accumulator.write((int) in.readByte());
                                    }
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException | UnmodifiableClassException e) {
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

    private static String rstr(DataInputStream in) throws IOException {
        int i = in.readInt();
        byte[] bytes = new byte[i];
        in.read(bytes);

        return new String(bytes);
    }

    public static void wi(DataOutputStream out) throws IOException {
        out.writeInt(4);
        out.writeInt(0);
    }

    public static void wrd(DataOutputStream out, byte[] data, String method, String[] params) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(3);

        dos.writeInt(data.length);
        dos.write(data);

        dos.writeInt(method.length());
        dos.write(method.getBytes(Charsets.UTF_16));

        dos.writeInt(params.length);
        for (String param : params) {
            dos.writeInt(param.length());
            dos.write(param.getBytes(Charsets.UTF_16));
        }

        out.writeInt(baos.size());
        baos.writeTo(out);
    }
}