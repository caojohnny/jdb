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
import com.gmail.woodyc40.topics.protocol.SignalOut;
import com.gmail.woodyc40.topics.protocol.SignalOutBusy;
import com.gmail.woodyc40.topics.protocol.SignalOutExit;
import com.google.common.collect.Queues;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.concurrent.GuardedBy;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class AgentServer {
    private static final long TIMEOUT_MS = 5000L;

    private static final int WAITING_FOR_CON = 0;
    private static final int ACTIVE = 1;
    private static final int SHUTDOWN = 2;

    @Getter(AccessLevel.PRIVATE) private final ServerSocket socket;

    @Getter
    private final AtomicInteger state = new AtomicInteger();
    @Getter
    @Setter
    @GuardedBy("lock")
    private Socket conn;
    @Getter
    private final Lock lock = new ReentrantLock();
    @Getter
    private final Condition hasConnection = this.lock.newCondition();
    @Getter
    private final BlockingQueue<SignalOut> signals = Queues.newLinkedBlockingQueue();

    private final ExecutorService ioThreads;
    @Getter
    private final Thread handler;

    private AgentServer(int port, ExecutorService ioThreads, Thread handler) {
        this.ioThreads = ioThreads;
        this.handler = handler;
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.socket = socket;
    }

    public static AgentServer initServer(int port) {
        ExecutorService ioThreads = Executors.newFixedThreadPool(3);
        AtomicReference<Thread> ref = new AtomicReference<>();
        ioThreads.execute(() -> {
            ref.set(Thread.currentThread());

            // TODO: Condition
            while (true) {
                LockSupport.park();
            }
        });
        AgentServer server = new AgentServer(port, ioThreads, ref.get());


        ioThreads.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                server.getLock().lock();
                try {
                    while (server.getState().get() != ACTIVE) {
                        server.getHasConnection().await();
                    }

                    Socket sock = server.getConn();
                    DataInputStream in = new DataInputStream(sock.getInputStream());
                    DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                    while (!sock.isClosed() && sock.isConnected()) {
                        SignalOut sig = server.getSignals().take();
                        LockSupport.unpark(server.getHandler());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        ioThreads.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket socket = server.getSocket().accept();
                    if (server.getState().compareAndSet(WAITING_FOR_CON, ACTIVE)) {
                        server.getLock().lock();
                        try {
                            server.setConn(socket);
                            server.getHasConnection().signal();
                        } finally {
                            server.getLock().unlock();
                        }
                    } else {
                        writeSignal(new SignalOutBusy(), socket);
                        socket.close();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        });

        return server;
    }

    public void close() {
        this.state.set(SHUTDOWN);
        try {
            this.socket.close();
            this.signals.clear();

            if (JvmContext.getContext().isCloseOnDetach()) {
                this.signals.add(new SignalOutExit(3, "JDB Exit"));
            }

            this.ioThreads.shutdown();
            this.ioThreads.awaitTermination(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(SignalOut signal) {
        this.signals.add(signal);
        LockSupport.unpark(this.handler);
    }

    private static void writeSignal(SignalOut sig, Socket sock) {
    }
}