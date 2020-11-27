package com.alipay.sofa.registry.server.meta.metaserver.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.observer.impl.AbstractLifecycleObservable;
import com.alipay.sofa.registry.server.meta.MetaServer;
import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author chen.zhu
 * <p>
 * Nov 20, 2020
 */
public abstract class AbstractMetaServer extends AbstractLifecycleObservable implements MetaServer {

    protected List<MetaNode> metaServers = Lists.newCopyOnWriteArrayList();

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();


    @ReadOnLeader
    public List<MetaNode> getClusterMembers() {
        lock.readLock().lock();
        try {
            return Lists.newArrayList(metaServers);
        } finally {
            lock.readLock().unlock();
        }
    }

}
