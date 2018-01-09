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
import com.gmail.woodyc40.topics.protocol.*;
import com.google.common.collect.Queues;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.concurrent.GuardedBy;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The local server handler
 */
public class AgentServer {
    /** The timeout for the server to shutdown */
    private static final long TIMEOUT_MS = 5000L;

    /** Currently waiting for connection */
    private static final int WAITING_FOR_CON = 0;
    /** Currently connected */
    private static final int ACTIVE = 1;
    /** Shutting down */
    private static final int SHUTDOWN = 2;

    /** The server that has been started */
    @Getter(AccessLevel.PRIVATE)
    private final ServerSocketChannel server;

    /** The current state of the server */
    @Getter
    private final AtomicInteger state = new AtomicInteger();
    /** The current client */
    @Getter
    @Setter
    @GuardedBy("lock")
    private SocketChannel conn;
    /** Lock used to protect the connection */
    @Getter
    private final Lock lock = new ReentrantLock();
    /** Condition used to notify of a connection */
    @Getter
    private final Condition hasConnection = this.lock.newCondition();

    /** The signal send queue */
    @Getter
    private final BlockingQueue<SignalOut> out = Queues.newLinkedBlockingQueue();
    @Getter
    private final Queue<OutDataWrapper> outgoing = Queues.newConcurrentLinkedQueue();
    /** The signal process queue */
    @Getter
    private final BlockingQueue<InDataWrapper> incoming = Queues.newLinkedBlockingQueue();
    @Getter
    private final AtomicReference<Thread> duplex = new AtomicReference<>();

    /** The threads used for Net IO */
    private final ExecutorService ioThreads;

    private AgentServer(int port, ExecutorService ioThreads) {
        this.ioThreads = ioThreads;
        try {
            this.server = ServerSocketChannel.open();
            this.server.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static AgentServer initServer(int port) {
        ExecutorService ioThreads = Executors.newFixedThreadPool(4);
        AgentServer server = new AgentServer(port, ioThreads);

        // Socket duplex
        ioThreads.execute(() -> {
            server.getDuplex().set(Thread.currentThread());

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    server.getLock().lockInterruptibly();
                    try {
                        while (server.getState().get() != AgentServer.ACTIVE) {
                            server.getHasConnection().await();
                        }

                        SocketChannel sock = server.getConn();
                        SocketInterruptUtil.prepare(sock);

                        ByteArrayOutputStream accumulator = new ByteArrayOutputStream();
                        ByteBuffer header = ByteBuffer.allocate(4); // DIS = 4 byte int size
                        ByteBuffer payload = ByteBuffer.allocate(1024);
                        int payloadLen = -1;
                        while (true) {
                            try {
                                if (sock.read(header) == 0) {
                                    header.flip();
                                    payloadLen = (header.getInt() << 24) +
                                            (header.getInt() << 16) +
                                            (header.getInt() << 8) +
                                            header.getInt();
                                }

                                if (payloadLen != -1) {
                                    int len;
                                    while ((len = sock.read(payload)) > 0) {
                                        int accumulate = Math.min(accumulator.size() - payloadLen, len);

                                        payload.flip();
                                        accumulator.write(payload.array(), 0, accumulate);
                                        payload.reset();
                                    }
                                }

                                if (accumulator.size() == payloadLen) {
                                    ByteArrayInputStream in = new ByteArrayInputStream(accumulator.toByteArray());
                                    DataInputStream stream = new DataInputStream(in);
                                    int id = stream.readInt();
                                    byte[] bs = new byte[payloadLen - 1];
                                    InDataWrapper wrapper = new InDataWrapper(bs, id);
                                    server.getIncoming().add(wrapper);

                                    payloadLen = -1;
                                    header.reset();
                                }
                            } catch (SocketInterruptUtil.Signal signal) {
                                OutDataWrapper out;
                                while ((out = server.getOutgoing().poll()) != null) {
                                    ByteBuffer buf = out.getData();

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    DataOutputStream dos = new DataOutputStream(baos);
                                    dos.write(buf.limit() + 4);
                                    dos.write(out.getId());

                                    sock.write(ByteBuffer.wrap(baos.toByteArray()));
                                    sock.write(buf);
                                }

                                Thread.interrupted();
                            }
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (SocketInterruptUtil.Signal signal) {
                        Thread.interrupted();
                    } finally {
                        server.getLock().unlock();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        // Write processor
        ioThreads.execute(() -> {
            while (true) {
                try {
                    SignalOut take = server.getOut().take();

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(out);
                    take.write(dos);

                    ByteBuffer buffer = ByteBuffer.wrap(out.toByteArray());
                    OutDataWrapper wrapper = new OutDataWrapper(buffer, SignalRegistry.writeSignal(take));

                    server.getOutgoing().add(wrapper);
                    SocketInterruptUtil.signal(server.getDuplex().get());
                } catch (InterruptedException e) {
                    break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Read processor
        ioThreads.execute(() -> {
            while (true) {
                try {
                    InDataWrapper data = server.getIncoming().take();
                    SignalIn signal = SignalRegistry.readSignal(data.getId());
                    signal.read(new DataInputStream(new ByteArrayInputStream(data.getData())));
                } catch (InterruptedException e) {
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Socket listener
        ioThreads.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SocketChannel ch = server.getServer().accept();
                    if (server.getState().compareAndSet(AgentServer.WAITING_FOR_CON, AgentServer.ACTIVE)) {
                        server.getLock().lockInterruptibly();
                        try {
                            server.setConn(ch);
                            server.getHasConnection().signalAll();
                        } finally {
                            server.getLock().unlock();
                        }
                    } else {
                        AgentServer.writeSignal(new SignalOutBusy(), ch);
                        ch.close();
                    }
                } catch (IOException | InterruptedException e) {
                    break;
                }
            }
        });

        return server;
    }

    public void close() {
        this.state.set(AgentServer.SHUTDOWN);
        try {
            this.server.close();
            this.outgoing.clear();

            if (JvmContext.getContext().isCloseOnDetach()) {
                this.out.add(new SignalOutExit(3, "JDB Exit"));
            }

            this.ioThreads.shutdown();
            this.ioThreads.awaitTermination(AgentServer.TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(SignalOut signal) {
        this.out.add(signal);
        SocketInterruptUtil.signal(this.duplex.get());
    }

    private static void writeSignal(SignalOut sig, SocketChannel ch) throws IOException {
        int id = SignalRegistry.writeSignal(sig);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(out);
        sig.write(stream);

        int size = out.size() + 4;

        ByteBuffer buf = ByteBuffer.allocate(size + 4);
        buf.putInt(size);
        buf.putInt(id);
        buf.put(out.toByteArray());

        ch.write(buf);
    }
}