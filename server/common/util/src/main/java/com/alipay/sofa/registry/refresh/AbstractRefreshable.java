package com.alipay.sofa.registry.refresh;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public abstract class AbstractRefreshable implements Refreshable {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicLong counter = new AtomicLong();

    private final AtomicLong timestamp = new AtomicLong();

    @Override
    public void refresh() {
        counter.incrementAndGet();
        timestamp.set(System.currentTimeMillis());
        if(logger.isInfoEnabled()) {
            logger.info("[refresh][times-{}] start", getRefreshCount());
        }
        doRefresh();
        if(logger.isInfoEnabled()) {
            logger.info("[refresh][times-{}] end", getRefreshCount());
        }
    }

    protected abstract void doRefresh();

    @Override
    public long getRefreshCount() {
        return counter.get();
    }

    @Override
    public long getLastUpdateTime() {
        return timestamp.get();
    }
}
