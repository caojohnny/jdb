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

import com.gmail.woodyc40.topics.infra.JvmContext;
import com.google.common.base.Charsets;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodExitRequest;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A signal response in which the payload is the bytecode
 * data of the class to inspect for methods.
 * Schema:
 * data
 * int:size
 * byte[] data
 * meName
 * int:strLen
 * byte[]:strData
 * desc
 * int:strLen
 * byte[]:strData
 */
public class SignalInRespMethod implements SignalIn {
    private static final Set<String> EV_CACHE =
            new HashSet<>();

    private static String readString(DataInputStream in) throws IOException {
        int strLen = in.readInt();
        byte[] data = new byte[strLen];
        in.readFully(data);
        return new String(data, Charsets.UTF_8);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        int size = in.readInt();
        byte[] data = new byte[size];
        in.readFully(data);

        String name = readString(in);
        String desc = readString(in);

        ClassReader reader = new ClassReader(data);
        reader.accept(new ClassVisitor(Opcodes.ASM6) {
            @Override
            public MethodVisitor visitMethod(int access, String n, String d, String signature, String[] exceptions) {
                if (!name.equals(n) || !desc.equals(d)) {
                    return null;
                }

                return new MethodVisitor(this.api) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String na, String de, boolean itf) {
                        if (!EV_CACHE.contains(owner)) {
                            EV_CACHE.add(owner);
                        } else {
                            return;
                        }

                        VirtualMachine vm = JvmContext.getContext().getVm();
                        EventRequestManager erm = vm.eventRequestManager();
                        ReferenceType type = vm.classesByName(owner.replaceAll("/", "\\.")).get(0);
                        for (Method method : type.methodsByName(na)) {
                            if (method.signature().equals(de) &&
                                    !method.isConstructor()) {
                                if (method.returnTypeName().equals("void")) {
                                    continue;
                                }

                                MethodExitRequest req = erm.createMethodExitRequest();
                                req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
                                req.addClassFilter(type);
                                req.enable();
                            }
                        }
                    }
                };
            }
        }, ClassReader.EXPAND_FRAMES);
    }
}
