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
import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.jraft.processor.SnapshotProcess;
import com.alipay.sofa.registry.observer.impl.AbstractObservable;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeRemoved;
import com.alipay.sofa.registry.server.meta.lease.EpochAware;
import com.alipay.sofa.registry.server.meta.lease.Lease;
import com.alipay.sofa.registry.server.meta.lease.LeaseManager;
import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;
import com.alipay.sofa.registry.util.FileUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.glassfish.jersey.internal.guava.Sets;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */
public class DefaultLeaseManager<T extends Node> extends AbstractObservable implements
                                                                           LeaseManager<T>,
                                                                           EpochAware, SnapshotProcess {

    private static final String                     EVICT_BETWEEN_MILLI = "evict.between.milli";

    private final int                               evictBetweenMilli   = Integer
                                                                            .parseInt(System
                                                                                .getProperty(
                                                                                    EVICT_BETWEEN_MILLI,
                                                                                    "60000"));

    protected final AtomicLong                      currentEpoch        = new AtomicLong();

    //Map[ip-address, Lease{node, duration, timestamp}]
    protected final ConcurrentMap<String, Lease<T>> repo                = Maps.newConcurrentMap();

    protected final ReentrantReadWriteLock          lock                = new ReentrantReadWriteLock();

    private final AtomicLong                        lastEvictTime       = new AtomicLong();

    private final String snapshotFilePrefix;

    private final String epochSnapshotPath  = String.format("%s-version", getClass().getSimpleName());

    private final String storageSnapshotPath = String.format("%s-storage", getClass().getSimpleName());

    public DefaultLeaseManager(String snapshotFilePrefix) {
        this.snapshotFilePrefix = snapshotFilePrefix;
    }

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
        notifyObservers(new NodeAdded<T>(renewal));
    }

    @Override
    public boolean cancel(T renewal) {
        if (renewal == null) {
            throw new IllegalArgumentException("[cancel]NullPointer of renewal");
        }
        // read lock for concurrent modification, and mutext for renew/register operations
        lock.readLock().lock();
        boolean result = true;
        try {
            if (logger.isInfoEnabled()) {
                logger.info("[cancel][begin] {}", renewal);
            }
            Lease<T> lease = repo.remove(renewal.getNodeUrl().getIpAddress());
            if (lease != null && logger.isInfoEnabled()) {
                logger.info("[cancel][end] {} {} {}", lease.getRenewal(),
                    lease.getBeginTimestamp(), lease.getLastUpdateTimestamp());
            }
            result = lease != null;
        } finally {
            lock.readLock().unlock();
        }
        if (result) {
            notifyObservers(new NodeRemoved<>(renewal));
        }
        return result;
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
                    logger.warn("[renew][node not exist, register: {}-{}]", renewal.getNodeType(),
                        renewal.getNodeUrl().getIpAddress());
                }
                register(renewal, leaseDuration);
                return false;
            }
            int validLeaseDuration = leaseDuration > 0 ? leaseDuration
                : Lease.DEFAULT_DURATION_SECS;
            if (logger.isInfoEnabled()) {
                logger.info("[renew][renew lease] node: {}-{}, extends: {}s",
                    renewal.getNodeType(), renewal.getNodeUrl().getIpAddress(), validLeaseDuration);
            }
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

    @Override
    public void refreshEpoch(long newEpoch) {
        currentEpoch.set(newEpoch);
    }

    @Override
    public boolean save(String path) {
        if(path.equalsIgnoreCase(epochSnapshotPath)) {
            return save(path, currentEpoch.get());
        } else if (path.equalsIgnoreCase(storageSnapshotPath)) {
            return save(path, repo);
        }
        throw new IllegalArgumentException("Unknown path: " + path);
    }

    private boolean save(String path, Object values) {
        try {
            FileUtils.writeByteArrayToFile(new File(path), CommandCodec.encodeCommand(values),
                    false);
            return true;
        } catch (IOException e) {
            logger.error("Fail to save snapshot", e);
            return false;
        }
    }

    @Override
    public boolean load(String path) {
        if(path.equalsIgnoreCase(epochSnapshotPath)) {
            try {
                long epoch = load(path, Long.class);
                refreshEpoch(epoch);
            } catch (IOException e) {
                logger.error("Load lease manager epoch data error!", e);
                return false;
            }
        } else if(path.equalsIgnoreCase(storageSnapshotPath)) {
            try {
                Map<String, Lease<T>> leaseStore = load(path, repo.getClass());
                synchronized (this) {
                    this.repo.putAll(leaseStore);
                }
            } catch (IOException e) {
                logger.error("Load lease manager data error!", e);
                return false;
            }
        }
        throw new IllegalArgumentException("Unknown path: " + path);
    }

    private  <T> T load(String path, Class<T> clazz) throws IOException {
        byte[] bs = FileUtils.readFileToByteArray(new File(path));
        if (bs.length > 0) {
            return CommandCodec.decodeCommand(bs, clazz);
        }
        throw new IOException("Fail to load snapshot from " + path + ", content: "
                + Arrays.toString(bs));
    }

    @Override
    public SnapshotProcess copy() {
        DefaultLeaseManager<T> leaseManager = copyMySelf();
        leaseManager.repo.putAll(this.repo);
        leaseManager.refreshEpoch(this.currentEpoch.get());
        return leaseManager;
    }

    @Override
    public Set<String> getSnapshotFileNames() {
        Set<String> set = Sets.newHashSet();
        set.add(epochSnapshotPath);
        set.add(storageSnapshotPath);
        return set;
    }

    protected DefaultLeaseManager<T> copyMySelf() {
        return new DefaultLeaseManager<>(snapshotFilePrefix);
    }

}
