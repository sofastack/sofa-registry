package com.alipay.sofa.registry.observer;


/**
 * @author zhuchen
 * @date Nov 25, 2020, 11:16:14 AM
 */
public interface Observable {

    void addObserver(Observer observer);

    void removeObserver(Observer observer);
}
