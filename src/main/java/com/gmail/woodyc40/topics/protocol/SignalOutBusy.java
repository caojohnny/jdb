package com.gmail.woodyc40.topics.protocol;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Sent to the client whenever the server is not able to
 * handle a request to connect.
 *
 * SCHEMA:
 * Empty
 */
public class SignalOutBusy implements SignalOut {
    @Override
    public void write(DataOutputStream out) throws IOException {
    }
}