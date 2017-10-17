package com.gmail.woodyc40.topics.infra;

import com.sun.jdi.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a field or local variable value.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Var {
    /** The name of the variable */
    private final String name;
    /** The variable type */
    private final Type type;
    /** The value of the variable */
    private final Value value;
    /** The location of the variable */
    private final Location location;
    /** The current stack frame */
    private final StackFrame frame;
    /** {@code true} if this is a localvariable */
    private final boolean isLocal;

    public static Var newVar(ObjectReference obj, Field field, StackFrame frame) throws ClassNotLoadedException {
        if (field.isStatic()) {
            return new Var(
                    field.name(),
                    field.type(),
                    frame.location().declaringType().getValue(field),
                    frame.location(),
                    frame,
                    false
            );
        } else {
            return new Var(
                    field.name(),
                    field.type(),
                    obj.getValue(field),
                    frame.location(),
                    frame,
                    false
            );
        }
    }

    public static Var newVar(LocalVariable variable, StackFrame frame) throws ClassNotLoadedException {
        return new Var(
                variable.name(),
                variable.type(),
                frame.getValue(variable),
                frame.location(),
                frame,
                true
        );
    }
}