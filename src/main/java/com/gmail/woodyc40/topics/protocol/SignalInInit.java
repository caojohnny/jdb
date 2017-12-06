package com.gmail.woodyc40.topics.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * SCHEMA:
 * - int:pid
 */
public class SignalInInit extends Signal {
    @Override
    public void read(DataInputStream inputStream) throws Exception {
        int pid = inputStream.readInt();
    }

    @Override
    public void write(DataOutputStream outputStream) throws Exception {
    }
}