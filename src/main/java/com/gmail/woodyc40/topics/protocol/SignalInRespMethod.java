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

import com.gmail.woodyc40.topics.cmd.Enter;
import com.gmail.woodyc40.topics.infra.JvmContext;
import com.google.common.base.Charsets;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequestManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Schema:
 * int:size
 * byte[] data
 * int:strLen
 * byte[]:strData
 * int:listSize
 *   int:strLen
 *   byte:strData
 */
public class SignalInRespMethod implements SignalIn {
    @Override
    public void read(DataInputStream in) throws IOException {
        int size = in.readInt();
        byte[] data = new byte[size];
        in.read(data);

        String meName = readString(in);

        List<String> args = new ArrayList<>();
        int listSize = in.readInt();
        for (int i = 0; i < listSize; i++) {
            args.add(readString(in));
        }

        ClassReader reader = new ClassReader(data);
        reader.accept(new ClassVisitor(Opcodes.ASM6) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(this.api) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        System.out.println(owner + " owns " + name + ": desc=" + desc);

                        VirtualMachine vm = JvmContext.getContext().getVm();
                        EventRequestManager erm = vm.eventRequestManager();
                        ReferenceType reference = Enter.getReference(owner);
                    }
                };
            }
        }, ClassReader.EXPAND_FRAMES);
    }

    private static String readString(DataInputStream in) throws IOException {
        int strLen = in.readInt();
        byte[] data = new byte[strLen];
        in.read(data);
        return new String(data, Charsets.UTF_16);
    }
}
