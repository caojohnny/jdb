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
package com.gmail.woodyc40.topics.server;

import com.gmail.woodyc40.topics.protocol.Signal;
import com.google.common.collect.Queues;
import lombok.AccessLevel;
import lombok.Getter;
import org.omg.PortableInterceptor.TRANSPORT_RETRY;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class AgentServer {
    @Getter(AccessLevel.PRIVATE) private final ServerSocket socket;

    @Getter(AccessLevel.PRIVATE) private final BlockingQueue<Socket> conn = Queues.newArrayBlockingQueue(1);
    private final BlockingQueue<Signal> outgoing = Queues.newLinkedBlockingQueue();
    private final BlockingQueue<Signal> incoming = Queues.newLinkedBlockingQueue();

    public AgentServer(int port) {
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.socket = socket;
    }

    public static AgentServer initServer(int port) {
        AgentServer server = new AgentServer(port);
        int bossCount = 4;
        int workerCount = 4;
        ExecutorService boss = Executors.newFixedThreadPool(4);
        ExecutorService workers = Executors.newFixedThreadPool(4);

        boss.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    server.getConn().add(server.getSocket().accept());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        for (int i = 0; i < bossCount - 3; i++) {
            boss.execute(() -> {
                Socket sock = null;
                while (!Thread.currentThread().isInterrupted()) {
                    if (sock == null) {
                        sock = server.getConn().
                        continue;
                    }
                }
            });
        }

        for (int i = 0; i < workerCount; i++) {
            workers.execute(() -> {
                while (!Thread.currentThread().isInterrupted()) {

                }
            });
        }
    }

    public void close() {
        try {
            this.socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(Signal signal) {
        this.outgoing.add(signal);
    }
}