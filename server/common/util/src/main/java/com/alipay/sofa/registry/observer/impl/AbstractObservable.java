package com.alipay.sofa.registry.observer.impl;

import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.Observer;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * @author zhuchen
 * @date Nov 25, 2020, 11:16:06 AM
 */
public abstract class AbstractObservable implements Observable {

    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private List<Observer> observers = new ArrayList<>();

    private Executor executors = MoreExecutors.directExecutor();

    public AbstractObservable() {
    }

    public AbstractObservable(Executor executors) {
        this.executors = executors;
    }

    public void setExecutors(Executor executors) {
        this.executors = executors;
    }

    @Override
    public void addObserver(Observer observer) {
        try {
            lock.writeLock().lock();
            observers.add(observer);
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        try {
            lock.writeLock().lock();
            observers.remove(observer);
        }finally {
            lock.writeLock().unlock();
        }
    }
}
