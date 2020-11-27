package com.alipay.sofa.registry.observer;


/**
 * @author zhuchen
 * @date Nov 25, 2020, 11:16:20 AM
 */
public interface Observer {

    void update(Observable source, Object message);
}
