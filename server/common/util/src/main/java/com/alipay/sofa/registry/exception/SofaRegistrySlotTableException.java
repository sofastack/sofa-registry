package com.alipay.sofa.registry.exception;

/**
 * @author chen.zhu
 * <p>
 * Jan 26, 2021
 */
public class SofaRegistrySlotTableException extends SofaRegistryRuntimeException {
    public SofaRegistrySlotTableException(String message) {
        super(message);
    }

    public SofaRegistrySlotTableException(String message, Throwable cause) {
        super(message, cause);
    }

    public SofaRegistrySlotTableException(Throwable cause) {
        super(cause);
    }

    public SofaRegistrySlotTableException(String message, Throwable cause,
                                          boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
