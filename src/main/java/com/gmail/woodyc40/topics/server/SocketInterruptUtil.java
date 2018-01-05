package com.gmail.woodyc40.topics.server;
import sun.nio.ch.Interruptible;

import java.lang.reflect.Field;
import java.nio.channels.SocketChannel;

/**
 * Utility class used to modify {@link SocketChannel}s in
 * order to override the default behavior and allow for I/O
 * threads to capture {@link Signal}s passed while waiting
 * for input.
 *
 * <p>Be aware that this is an extremely egregious hack.
 * For the most part, it is a toy. I don't expect that
 * anyone would seriously consider using this in production,
 * but if there is any case where that occurs, I am not
 * responsible for what happens. Use at your own risk. You
 * have been warned.</p>
 *
 * <p>To add further to the risks associated with this
 * class, one must <strong>NEVER</strong> call {@link
 * Thread#interrupt()} on an I/O thread. Doing so may
 * result in undefined behavior. Capturing a {@link Signal}
 * also means that the I/O thread must use
 * {@link Thread#interrupted()} in order to clear the
 * interrupt state before the next signal. Finally,
 * {@link Thread#interrupt()} is used in order to propagate
 * signals to the I/O thread, and therefore, if any methods
 * that are interruptible must catch the exception and run
 * {@link Thread#interrupted()}.</p>
 */
public final class SocketInterruptUtil {
    /** The cached field used to hack the SocketChannel */
    private static final Field INTERRUPTOR;
    /** The signal used to notify readers */
    private static final Signal SIGNAL = new Signal();

    static {
        try {
            Class<?> cls = Class.forName("java.nio.channels.spi.AbstractInterruptibleChannel");
            INTERRUPTOR = cls.getDeclaredField("interruptor");
            INTERRUPTOR.setAccessible(true);
        } catch (NoSuchFieldException e) {
            System.out.println("No such field: interruptor");
            System.out.println("Perhaps not running Oracle HotSpot?");
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            System.out.println("No such class: AbstractInterruptibleChannel");
            System.out.println("Perhaps not running Oracle HotSpot?");
            throw new RuntimeException(e);
        }
    }

    // Suppress instantiation
    private SocketInterruptUtil() {
    }

    /**
     * Prepares the {@link SocketChannel} to receive
     * {@link Signal}s dispatched by another thread. This
     * is required in order for this to work correctly.
     *
     * @param ch the channel to prepare
     */
    public static void prepare(SocketChannel ch) {
        try {
            INTERRUPTOR.set(ch, (Interruptible) thread -> {
                throw SIGNAL;
            });
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Signals the given I/O thread to unblock from ALL
     * blocking methods and proceed and throws a
     * {@link Signal} to be handled by the thread.
     *
     * @param thread the thread to unblock
     */
    public static void signal(Thread thread) {
        try {
            // Thread probably not blocked on I/O
            if (thread.getState() != Thread.State.RUNNABLE) {
                return;
            }
            thread.interrupt();
        } catch (Signal ignored) {
        }
    }

    /**
     * A signal dispatched by another thread to a target
     * I/O thread in order for targets to respond to
     * notifications.
     */
    public static class Signal extends RuntimeException {
        private static final long serialVersionUID = -220295899772322553L;
    }
}