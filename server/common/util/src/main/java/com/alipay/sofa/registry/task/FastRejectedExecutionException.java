package com.alipay.sofa.registry.task;

import java.util.concurrent.RejectedExecutionException;

public class FastRejectedExecutionException extends RejectedExecutionException {
    public FastRejectedExecutionException(String message) {
        super(message);
    }

    @Override
    public Throwable fillInStackTrace() {
        // not fill the stack trace
        return this;
    }

}
