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
package com.gmail.woodyc40.topics.infra;

import com.gmail.woodyc40.topics.Main;
import com.gmail.woodyc40.topics.cmd.LsJvm;
import com.google.common.collect.Sets;
import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.tools.jdi.ProcessAttachingConnector;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds the current state of the JVM which is being
 * debugged, i.e. breakpoints and source paths.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JvmContext {
    /** The singleton instance of the JVM context */
    @Getter private static final JvmContext context = new JvmContext();

    /** Currently attached JVM PID */
    @Getter private int currentPid = -1;
    /** The virtual machine that is currently attached */
    @Getter private VirtualMachine vm;
    /** The listener thread for VM breakpoints */
    private Thread breakpointListener;
    /** The collection of paths leading to class sources */
    @Getter private final Set<Path> sourcePath = Sets.newHashSet();

    /** The class that is currently being modified */
    @Getter @Setter private ReferenceType currentRef;

    /** The previous breakpoint frames */
    private final Queue<List<StackFrame>> previousFrames = new ConcurrentLinkedQueue<>();
    /** The current breakpoint that is active on the VM */
    @Getter private final AtomicReference<BreakpointEvent> currentBreakpoint = new AtomicReference<>();

    /**
     * Sets the current JVM context to that of a JVM running
     * at the given PID number.
     *
     * @param pid the process ID to attach
     */
    public void attach(int pid) {
        if (pid < 0) {
            System.out.println("failed");
            return;
        }

        if (this.currentPid == pid) {
            System.out.println("process already attached");
            return;
        }

        if (this.currentPid > 0) {
            System.out.println();
            System.out.println("Currently attached to " + this.currentPid);
            String yn = Main.prompt("Do you really want to attach [Y/n]? ");
            if (!yn.toLowerCase().equals("y") && !yn.toLowerCase().equals("yes")) {
                System.out.println("abort");
                return;
            }
        }

        String procData;
        try {
            procData = LsJvm.getAvailablePids().get(pid);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (procData == null) {
            System.out.println("no JVM with PID " + pid);
            return;
        }

        System.out.println("Attaching to " + pid + ": " + procData + "...");
        this.currentPid = pid;

        List<AttachingConnector> connectors = Bootstrap.virtualMachineManager().attachingConnectors();
        ProcessAttachingConnector pac = null;
        for (AttachingConnector connector : connectors) {
            if (connector.name().equals("com.sun.jdi.ProcessAttach")) {
                pac = (ProcessAttachingConnector) connector;
            }
        }

        if (pac == null) {
            System.out.println("ProcessAttach not found");
            return;
        }

        Map<String, Connector.Argument> args = pac.defaultArguments();
        Connector.Argument arg = args.get("pid");
        if (arg == null) {
            System.out.println("corrupt transport");
            return;
        }

        arg.setValue(String.valueOf(pid));
        try {
            this.vm = pac.attach(args);
            this.breakpointListener = new Thread(new Runnable() {
                final EventQueue queue = JvmContext.this.vm.eventQueue();
                @Override public void run() {
                    while (true) {
                        try {
                            EventSet eventSet = this.queue.remove();

                            for (Event event : eventSet) {
                                if (event instanceof BreakpointEvent) {
                                    BreakpointEvent e = (BreakpointEvent) event;

                                    List<StackFrame> frames = new ArrayList<>();
                                    for (int i = 0; i < Integer.MAX_VALUE; i++) {
                                        try {
                                            frames.add(e.thread().frame(i));
                                        } catch (IncompatibleThreadStateException e1) {
                                            e1.printStackTrace();
                                        } catch (IndexOutOfBoundsException e1) {
                                            break;
                                        }
                                    }

                                    // TODO fix jline lol
                                    JvmContext.this.currentBreakpoint.set(e);
                                    System.out.println("Hit breakpoint " + e.location().sourceName() + ":" + e.location().lineNumber());
                                }
                            }
                        } catch (AbsentInformationException e) {
                            throw new RuntimeException(e);
                        } catch (VMDisconnectedException e) {
                            JvmContext.this.breakpointListener = null;
                            JvmContext.this.detach();
                            break;
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            });
            this.breakpointListener.start();
        } catch (IOException | IllegalConnectorArgumentsException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Successfully attached to " + pid);
    }

    /**
     * Detaches from the currently attached JVM, or fails.
     */
    public void detach() {
        try {
            if (this.breakpointListener != null) {
                this.breakpointListener.interrupt();
                this.breakpointListener.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.vm.dispose();
        this.vm = null;
        System.out.println("Detached from " + this.currentPid);
        this.currentPid = -1;
    }
}