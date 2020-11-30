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
package com.alipay.sofa.registry.server.meta.lease.impl;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.lease.EpochAware;
import com.alipay.sofa.registry.server.meta.lease.Lease;
import com.alipay.sofa.registry.server.meta.lease.LeaseManager;
import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */
public class DefaultLeaseManager<T extends Node> implements LeaseManager<T>, EpochAware {

    private Logger                            logger              = LoggerFactory
                                                                      .getLogger(getClass());

    private static final String               EVICT_BETWEEN_MILLI = "evict.between.milli";

    private int                               evictBetweenMilli   = Integer.parseInt(System
                                                                      .getProperty(
                                                                          EVICT_BETWEEN_MILLI,
                                                                          "60000"));

    protected AtomicLong                      currentEpoch        = new AtomicLong();

    //Map[ip-address, Lease{node, duration, timestamp}]
    protected ConcurrentMap<String, Lease<T>> repo                = Maps.newConcurrentMap();

    protected final ReentrantReadWriteLock    lock                = new ReentrantReadWriteLock();

    private AtomicLong                        lastEvictTime       = new AtomicLong();

    public void register(T renewal, int leaseDuration) {
        if (renewal == null) {
            throw new IllegalArgumentException("[register]NullPointer of renewal");
        }
        lock.writeLock().lock();
        try {
            repo.putIfAbsent(renewal.getNodeUrl().getIpAddress(), new Lease<>(renewal,
                leaseDuration));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean cancel(T renewal) {
        if (renewal == null) {
            throw new IllegalArgumentException("[cancel]NullPointer of renewal");
        }
        // read lock for concurrent modification, and mutext for renew/register operations
        lock.readLock().lock();
        try {
            if (logger.isInfoEnabled()) {
                logger.info("[cancel][begin] {}", renewal);
            }
            Lease<T> lease = repo.remove(renewal.getNodeUrl().getIpAddress());
            if (lease != null && logger.isInfoEnabled()) {
                logger.info("[cancel][end] {} {} {}", lease.getRenewal(),
                    lease.getBeginTimestamp(), lease.getLastUpdateTimestamp());
            }
            return lease != null;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean renew(T renewal, int leaseDuration) {
        if (renewal == null) {
            throw new IllegalArgumentException("[renew]NullPointer of renewal");
        }
        lock.writeLock().lock();
        try {
            Lease<T> lease = repo.get(renewal.getNodeUrl().getIpAddress());
            if (lease == null) {
                if (logger.isWarnEnabled()) {
                    logger.warn("[renew][node not exist: {}]", renewal.getNodeUrl().getIpAddress());
                }
                register(renewal, leaseDuration);
                return false;
            }
            int validLeaseDuration = leaseDuration > 0 ? leaseDuration
                : Lease.DEFAULT_DURATION_SECS;
            lease.renew(validLeaseDuration);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * First, get expired leases
     * Then, lock and chek again to delete,
     * lock to double check is !!!mandatory!!! as lease might be renewed during eviction
     * */
    @Override
    @ReadOnLeader
    public boolean evict() {
        if (Math.abs(System.currentTimeMillis() - lastEvictTime.get()) < evictBetweenMilli) {
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
        List<Lease<T>> expires = getExpiredLeases();
        if (expires.isEmpty()) {
            return false;
        }
        lock.writeLock().lock();
        try {
            for (Lease<T> lease : expires) {
                Lease<T> doubleCheck = repo.get(lease.getRenewal().getNodeUrl().getIpAddress());
                // at this point of view, entry might be deleted through cancel method
                if (doubleCheck == null) {
                    continue;
                }
                if (doubleCheck.isExpired()) {
                    repo.remove(doubleCheck.getRenewal().getNodeUrl().getIpAddress());
                }
            }
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private List<Lease<T>> getExpiredLeases() {
        List<Lease<T>> expires = Lists.newLinkedList();
        for (Map.Entry<String, Lease<T>> entry : repo.entrySet()) {
            Lease<T> lease = entry.getValue();
            if (lease.isExpired()) {
                expires.add(lease);
            }
        }
        return expires;
    }

    @Override
    @ReadOnLeader
    public long getEpoch() {
        return currentEpoch.get();
    }

    /**
     * package private for not enable to reset logger outside scope
     * */
    void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void refreshEpoch(long newEpoch) {
        currentEpoch.set(newEpoch);
    }
}
