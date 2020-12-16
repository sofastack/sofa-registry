/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.meta.lease;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.observer.impl.AbstractLifecycleObservable;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeRemoved;
import com.alipay.sofa.registry.server.meta.lease.impl.DefaultLeaseManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.annotations.VisibleForTesting;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfiguration.SCHEDULED_EXECUTOR;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 *
 * epoch is updated through three scenerios:
 * 1. register event
 * 2. cancel event
 * 3. evict event
 */
public abstract class AbstractRaftEnabledLeaseManager<T extends Node> extends
                                                                      AbstractLifecycleObservable
                                                                                                 implements
                                                                                                 LeaseManager<T> {

    @Resource(name = SCHEDULED_EXECUTOR)
    private ScheduledExecutorService scheduled;

    private ScheduledFuture<?>       future;

    private final AtomicLong         lastEvictTime = new AtomicLong();

    protected final ReadWriteLock    lock          = new ReentrantReadWriteLock();

    @Override
    protected void doStart() throws StartException {
        super.doStart();
        future = scheduled.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (isRaftLeader()) {
                    evict();
                }
            }
        }, getIntervalMilli(), getIntervalMilli(), TimeUnit.MILLISECONDS);
    }

    protected abstract long getIntervalMilli();

    @Override
    protected void doStop() throws StopException {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        super.doStop();
    }

    protected abstract DefaultLeaseManager<T> getLocalLeaseManager();

    protected abstract LeaseManager<T> getRaftLeaseManager();

    @Override
    public long getEpoch() {
        if (isRaftLeader()) {
            return getLocalLeaseManager().getEpoch();
        }
        return getRaftLeaseManager().getEpoch();
    }

    @Override
    public List<T> getClusterMembers() {
        if (isRaftLeader()) {
            return getLocalLeaseManager().getClusterMembers();
        }
        return getRaftLeaseManager().getClusterMembers();
    }

    @Override
    public void register(Lease<T> lease) {
        throw new UnsupportedOperationException(
            "register is not supported through AbstractRaftEnabledLeaseManager");
    }

    @Override
    public boolean cancel(T renewal) {
        boolean result = getRaftLeaseManager().cancel(renewal);
        if (result) {
            notifyObservers(new NodeRemoved<>(renewal));
        }
        return result;
    }

    @Override
    public boolean renew(T renewal, int leaseDuration) {
        int validLeaseDuration = leaseDuration > 0 ? leaseDuration : Lease.DEFAULT_DURATION_SECS;
        Lease<T> lease = null;
        if (!isRaftLeader()) {
            lease = getRaftLeaseManager().getLease(renewal);
        } else {
            lock.readLock().lock();
            try {
                lease = getLocalLeaseManager().getLease(renewal);
            } finally {
                lock.readLock().unlock();
            }
        }

        /*
         * no exist lease, try register the node to all meta-servers through raft
         * */
        if (lease == null) {
            getRaftLeaseManager().register(new Lease<>(renewal, validLeaseDuration));
            notifyObservers(new NodeAdded<>(renewal));
        } else {
            if (isRaftLeader()) {
                getLocalLeaseManager().renew(renewal, leaseDuration);
            } else {
                getRaftLeaseManager().renew(renewal, leaseDuration);
            }
        }
        return true;
    }

    @Override
    public Lease<T> getLease(T renewal) {
        lock.readLock().lock();
        try {
            return getLocalLeaseManager().getLease(renewal);
        } finally {
            lock.readLock().unlock();
        }
    }

    protected abstract long getEvictBetweenMilli();

    @Override
    public boolean evict() {
        if (Math.abs(System.currentTimeMillis() - lastEvictTime.get()) < getEvictBetweenMilli()) {
            logger.warn("[evict][too quick] last evict time: {}", lastEvictTime.get());
            return false;
        }
        long lastTime = lastEvictTime.get();
        if (!lastEvictTime.compareAndSet(lastTime, System.currentTimeMillis())) {
            if (logger.isWarnEnabled()) {
                logger.warn("[evict][concurrent evict, quit] last evict time: {}", lastTime);
            }
            return false;
        }
        List<Lease<T>> expirations = getLocalLeaseManager().getExpiredLeases();
        if (expirations.isEmpty()) {
            return false;
        }
        boolean refreshEpoch = false;
        lock.writeLock().lock();
        try {
            for (Lease<T> lease : expirations) {
                Lease<T> doubleCheck = getLocalLeaseManager().getLease(lease.getRenewal());
                // at this point of view, entry might be deleted through cancel method
                if (doubleCheck == null) {
                    continue;
                }
                if (doubleCheck.isExpired()) {
                    refreshEpoch = true;
                    getRaftLeaseManager().cancel(lease.getRenewal());
                }
            }
            if (refreshEpoch) {
                getLocalLeaseManager().refreshEpoch(DatumVersionUtil.nextId());
            }
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void refreshEpoch(long newEpoch) {
        getLocalLeaseManager().refreshEpoch(newEpoch);
    }

    protected boolean isRaftLeader() {
        return ServiceStateMachine.getInstance().isLeader();
    }

    /**
     * for unit test easier set, not an optional for other calls
     * */
    @VisibleForTesting
    public AbstractRaftEnabledLeaseManager<T> setScheduled(ScheduledExecutorService scheduled) {
        this.scheduled = scheduled;
        return this;
    }
}
