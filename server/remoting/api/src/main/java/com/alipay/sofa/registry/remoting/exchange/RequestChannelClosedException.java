package com.alipay.sofa.registry.remoting.exchange;

import com.alipay.sofa.registry.remoting.exchange.message.Request;

public class RequestChannelClosedException extends RequestException{
    public RequestChannelClosedException(String message, Request request) {
        super(message, request);
    }
}
