package com.alipay.sofa.registry.exception;

/**
 * @author chen.zhu
 * <p>
 * Jan 06, 2021
 */
public class SofaRegistryRaftException extends SofaRegistryRuntimeException {

    public SofaRegistryRaftException(String message) {
        super(message);
    }

    public SofaRegistryRaftException(String message, Throwable cause) {
        super(message, cause);
    }

    public SofaRegistryRaftException(Throwable cause) {
        super(cause);
    }

    public SofaRegistryRaftException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
