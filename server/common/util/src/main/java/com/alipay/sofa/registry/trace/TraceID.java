package com.alipay.sofa.registry.trace;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class TraceID implements Serializable {
    private transient static final UUID SEED = UUID.randomUUID();
    private transient static final AtomicLong SEQ = new AtomicLong();

    private final long mostSigBits;
    private final long leastSigBits;
    private final long seq;

    private transient volatile String string;

    private TraceID(long most, long least, long seq) {
        this.mostSigBits = most;
        this.leastSigBits = least;
        this.seq = seq;
    }

    public static TraceID newTraceID() {
        return new TraceID(SEED.getMostSignificantBits(), SEED.getLeastSignificantBits(),
                SEQ.incrementAndGet());
    }

    @Override
    public String toString() {
        if (string == null) {
            string = createString();
        }
        return string;
    }


    private String createString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append(Long.toHexString(mostSigBits)).append(Long.toHexString(leastSigBits)).append('-').append(seq);
        return sb.toString();
    }
}
