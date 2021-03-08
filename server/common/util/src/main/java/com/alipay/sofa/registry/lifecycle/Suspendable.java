package com.alipay.sofa.registry.lifecycle;

/**
 * @author chen.zhu
 * <p>
 * Mar 08, 2021
 */
public interface Suspendable {

    void suspend();

    void resume();

    boolean isSuspended();
}
