package com.gmail.woodyc40.topics.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Represents a packet that is may be sent across a local
 * network in order to communicate with the debugger and
 * the client agent.
 */
public abstract class Signal {
    public abstract void read(DataInputStream inputStream) throws Exception;
    public abstract void write(DataOutputStream outputStream) throws Exception;
}