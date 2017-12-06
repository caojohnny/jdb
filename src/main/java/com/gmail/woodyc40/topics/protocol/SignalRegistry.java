package com.gmail.woodyc40.topics.protocol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@ThreadSafe
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SignalRegistry {
    private static int ID_COUNTER;
    private static final Map<Class<? extends Signal>, Integer> OUT_SIGNALS = new HashMap<>();
    private static final Map<Integer, Constructor<? extends Signal>> IN_SIGNALS = new HashMap<>();

    static {
        init(SignalInInit.class);
    }

    private static void init(Class<? extends Signal> signal) {
        try {
            int id = ID_COUNTER++;
            IN_SIGNALS.put(id, signal.getDeclaredConstructor());
            OUT_SIGNALS.put(signal, id);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Signal readSignal(int id) {
        Constructor<? extends Signal> constructor = IN_SIGNALS.get(id);
        if (constructor == null) {
            throw new RuntimeException("No signal: IN " + id);
        }

        try {
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static int writeSignal(Signal out) {
        Integer integer = OUT_SIGNALS.get(out.getClass());
        if (integer == null) {
            throw new IllegalStateException("No signal OUT: " + out.getClass().getSimpleName());
        }

        return integer;
    }
}