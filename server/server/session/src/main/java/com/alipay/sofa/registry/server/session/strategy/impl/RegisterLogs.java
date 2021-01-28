package com.alipay.sofa.registry.server.session.strategy.impl;

import com.alipay.sofa.registry.core.model.BaseRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;

public final class RegisterLogs {
    public static final Logger REGISTER_LOGGER = LoggerFactory.getLogger("REGISTER");

    private RegisterLogs() {
    }

    public static void logError(BaseRegister register, String name,
                                RegisterResponse registerResponse, Throwable e) {
        if (e instanceof RequestChannelClosedException) {
            REGISTER_LOGGER.warn("{} register error, Type {}, {}", name, register.getEventType(),
                e.getMessage());
        } else {
            REGISTER_LOGGER.error("{} register error, Type {}", name, register.getEventType(), e);
        }
        registerResponse.setSuccess(false);
        registerResponse.setMessage(name + " register failed!Type:" + register.getEventType());
    }

}
