package com.alipay.sofa.registry.refresh;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public interface Refreshable {

    void refresh();

    long getRefreshCount();

    long getLastUpdateTime();
}
