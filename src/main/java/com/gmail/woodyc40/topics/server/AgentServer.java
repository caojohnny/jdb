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

import com.gmail.woodyc40.topics.infra.JvmContext;
import com.gmail.woodyc40.topics.protocol.SignalIn;
import com.gmail.woodyc40.topics.protocol.SignalOut;
import com.gmail.woodyc40.topics.protocol.SignalOutBusy;
import com.gmail.woodyc40.topics.protocol.SignalOutExit;
import com.google.common.collect.Queues;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AgentServer {
    private static final long TIMEOUT_MS = 5000L;

    @Getter(AccessLevel.PRIVATE) private final ServerSocket socket;

    @Getter
    @Setter
    @GuardedBy("lock")
    private Socket curConnection;
    @Getter
    private final Object lock = new Object();

    private final BlockingQueue<SignalOut> outgoing = Queues.newLinkedBlockingQueue();
    private final BlockingQueue<SignalIn> incoming = Queues.newLinkedBlockingQueue();

    private final ExecutorService bossPool;
    private final ExecutorService workerPool;

    private AgentServer(int port, ExecutorService bossPool, ExecutorService workerPool) {
        this.bossPool = bossPool;
        this.workerPool = workerPool;
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.socket = socket;
    }

    public static AgentServer initServer(int port) {
        int bossCount = 4;
        int workerCount = 4;
        ExecutorService boss = Executors.newFixedThreadPool(4);
        ExecutorService workers = Executors.newFixedThreadPool(4);
        AgentServer server = new AgentServer(port, boss, workers);

        boss.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket sock = server.getSocket().accept();
                    synchronized (server.getLock()) {
                        if (server.getCurConnection() == null) {
                            server.setCurConnection(sock);
                        } else {
                            writeSignal(new SignalOutBusy(), sock);
                            sock.close();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        for (int i = 0; i < bossCount - 3; i++) {
            boss.execute(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                }
            });
        }

        for (int i = 0; i < workerCount; i++) {
            workers.execute(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                }
            });
        }

        return server;
    }

    public void close() {
        try {
            this.socket.close();
            this.incoming.clear();

            if (JvmContext.getContext().isCloseOnDetach()) {
                this.outgoing.add(new SignalOutExit(3, "JDB Exit"));
            } else {
                this.outgoing.clear();
            }

            this.workerPool.shutdown();
            this.bossPool.shutdown();
            this.workerPool.awaitTermination(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            this.bossPool.awaitTermination(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(SignalOut signal) {
        this.outgoing.add(signal);
    }

    private static void writeSignal(SignalOut sig, Socket sock) {
    }
}