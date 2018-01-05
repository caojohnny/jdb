package com.gmail.woodyc40.topics.protocol;

import lombok.Data;

import javax.annotation.concurrent.Immutable;

/**
 * Wrapper for the raw input of a packet.
 */
@Data
@Immutable
public class InDataWrapper {
    private final byte[] data;
    private final int id;
}