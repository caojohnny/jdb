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

import com.sun.jdi.ThreadReference;

/**
 * Thread status enum for converting the ID values provided
 * by {@link ThreadReference#status()}.
 */
public enum Status {
    /**
     * Blocked on monitor
     */
    THREAD_STATUS_MONITOR(3),
    /**
     * New thread; has not started
     */
    THREAD_STATUS_NOT_STARTED(5),
    /**
     * Thread running or blocked on I/O
     */
    THREAD_STATUS_RUNNING(1),
    /**
     * Thread is currently in {@link Thread#sleep(long)}
     */
    THREAD_STATUS_SLEEPING(2),
    /**
     * State indeterminable
     */
    THREAD_STATUS_UNKNOWN(-1),
    /**
     * Currently parked (I believe)
     */
    THREAD_STATUS_WAIT(4),
    /**
     * Zombie thread, awaiting collection
     */
    THREAD_STATUS_ZOMBIE(0);

    private final int internalValue;

    Status(int internalValue) {
        this.internalValue = internalValue;
    }

    /**
     * Converts the ID given by the reference status to the
     * representation provided by this enum.
     *
     * @param i the status ID
     * @return the status representation
     */
    public static Status of(int i) {
        for (Status status : values()) {
            if (status.internalValue == i) {
                return status;
            }
        }

        throw new IllegalArgumentException("No state: " + i);
    }
}