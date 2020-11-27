package com.alipay.sofa.registry.observer.impl;

import com.alipay.sofa.registry.lifecycle.Lifecycle;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.Observer;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author chen.zhu
 * <p>
 * Nov 25, 2020
 */
public class AbstractLifecycleObservable extends AbstractLifecycle implements Observable, Lifecycle {

    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private List<Observer> observers = new ArrayList<>();

    private Executor executors = MoreExecutors.directExecutor();

    public AbstractLifecycleObservable() {
    }

    public AbstractLifecycleObservable(Executor executors) {
        this.executors = executors;
    }

    public void setExecutors(Executor executors) {
        this.executors = executors;
    }

    @Override
    public void addObserver(Observer observer) {
        try{
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

    protected void notifyObservers(final Object message) {
        Object []tmpObservers;

        try {
            lock.readLock().lock();
            tmpObservers = observers.toArray();
        }finally {
            lock.readLock().unlock();
        }

        for(final Object observer : tmpObservers){

            executors.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        ((Observer)observer).update(AbstractLifecycleObservable.this, message);
                    }catch(Exception e){
                        logger.error("[notifyObservers][{}]", observer, e);
                    }
                }
            });
        }
    }
}
