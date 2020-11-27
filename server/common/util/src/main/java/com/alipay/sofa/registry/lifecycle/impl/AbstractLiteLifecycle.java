package com.alipay.sofa.registry.lifecycle.impl;

import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.lifecycle.LiteLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public abstract class AbstractLiteLifecycle implements LiteLifecycle {

    private AtomicBoolean isStarted = new AtomicBoolean(false);

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void start() throws StartException {


        if(isStarted.compareAndSet(false, true)){
            logger.info("[start]{}", this);
            doStart();
        }else{
            logger.warn("[start][already started]");
        }
    }

    protected abstract void doStart() throws StartException;


    @Override
    public void stop() throws StopException {
        if(isStarted.compareAndSet(true, false)){
            logger.info("[stop]{}", this);
            doStop();
        }else{
            logger.warn("[stop][already stopped]");
        }
    }

    protected abstract void doStop() throws StopException;


    public boolean isStarted() {
        return isStarted.get();
    }
}
