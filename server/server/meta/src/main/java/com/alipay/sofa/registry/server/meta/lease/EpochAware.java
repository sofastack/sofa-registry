package com.alipay.sofa.registry.server.meta.lease;

/**
 * @author chen.zhu
 * <p>
 * Nov 26, 2020
 */
public interface EpochAware {
    void refreshEpoch(long newEpoch);
    long getEpoch();
}
