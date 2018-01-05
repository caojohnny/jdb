package com.gmail.woodyc40.topics.protocol;

import lombok.Data;

import javax.annotation.concurrent.Immutable;
import java.nio.ByteBuffer;

@Data
@Immutable
public class OutDataWrapper {
    private final ByteBuffer data;
    private final int id;
}