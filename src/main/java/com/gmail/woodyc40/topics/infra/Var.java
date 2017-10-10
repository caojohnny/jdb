package com.gmail.woodyc40.topics.infra;

import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import lombok.AllArgsConstructor;

/**
 * Represents a field or local variable value.
 */
@AllArgsConstructor
public class Var {
    /** The value of the variable */
    private final Value value;
    /** The location of the variable */
    private final Location location;
    /** The current stack frame */
    private final StackFrame frame;
    /** {@code true} if this is a localvariable */
    private final boolean isLocal;
}