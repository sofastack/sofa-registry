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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.observer.impl.AbstractObservable;
import com.alipay.sofa.registry.server.meta.lease.Lease;
import com.alipay.sofa.registry.server.meta.lease.LeaseManager;
import com.alipay.sofa.registry.store.api.annotation.NonRaftMethod;
import com.alipay.sofa.registry.store.api.annotation.RaftMethod;
import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;
import com.alipay.sofa.registry.util.DatumVersionUtil;
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
                                                                           SnapshotProcess {

    protected final Logger                          logger              = LoggerFactory
                                                                            .getLogger(getClass());

    private final AtomicLong                        currentEpoch        = new AtomicLong();
    //Map[ip-address, Lease{node, duration, timestamp}]
    protected final ConcurrentMap<String, Lease<T>> repo                = Maps.newConcurrentMap();

    protected final ReentrantReadWriteLock          lock                = new ReentrantReadWriteLock();

    private final String                            snapshotFilePrefix;

    private final String                            storageSnapshotPath = String.format(
                                                                            "%s-storage",
                                                                            getClass()
                                                                                .getSimpleName());

    public DefaultLeaseManager(String snapshotFilePrefix) {
        this.snapshotFilePrefix = snapshotFilePrefix;
        setLogger(logger);
    }

    @Override
    @RaftMethod
    public void register(Lease<T> lease) {
        if (lease == null) {
            throw new IllegalArgumentException("[register]NullPointer of lease");
        }
        lock.writeLock().lock();
        try {
            repo.putIfAbsent(lease.getRenewal().getNodeUrl().getIpAddress(), lease);
            refreshEpoch(DatumVersionUtil.nextId());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @RaftMethod
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
            refreshEpoch(DatumVersionUtil.nextId());
        }
        return result;
    }

    @Override
    @NonRaftMethod
    public boolean renew(T renewal, int leaseDuration) {
        if (renewal == null) {
            throw new IllegalArgumentException("[renew]NullPointer of renewal");
        }
        int validLeaseDuration = leaseDuration > 0 ? leaseDuration : Lease.DEFAULT_DURATION_SECS;

        lock.writeLock().lock();
        try {
            Lease<T> lease = repo.get(renewal.getNodeUrl().getIpAddress());
            if (lease == null) {
                if (logger.isWarnEnabled()) {
                    logger.warn("[renew][node not exist, register: {}-{}]", renewal.getNodeType(),
                        renewal.getNodeUrl().getIpAddress());
                }
                register(new Lease<>(renewal, validLeaseDuration));
                return false;
            }
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

    @Override
    public Lease<T> getLease(T renewal) {
        return repo.get(renewal.getNodeUrl().getIpAddress());
    }

    /**
     * First, get expired leases
     * Then, lock and chek again to delete,
     * lock to double check is !!!mandatory!!! as lease might be renewed during eviction
     * */
    @Override
    @NonRaftMethod
    public boolean evict() {
        throw new UnsupportedOperationException(
            "evict is not supported through DefaultLeaseManager");
    }

    public List<Lease<T>> getExpiredLeases() {
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
    @ReadOnLeader
    public List<T> getClusterMembers() {
        List<T> result = Lists.newLinkedList();
        lock.readLock().lock();
        try {
            this.repo.values()
                    .forEach(lease->result.add(lease.getRenewal()));
        } finally {
            lock.readLock().unlock();
        }
        return result;
    }

    @Override
    @NonRaftMethod
    public void refreshEpoch(long newEpoch) {
        if (currentEpoch.get() < newEpoch) {
            if (logger.isInfoEnabled()) {
                logger.info("[refreshEpoch] epoch change from {} to {}", currentEpoch.get(),
                    newEpoch);
            }
            currentEpoch.set(newEpoch);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("[refreshEpoch] epoch change not able, current {}, request {}",
                    currentEpoch.get(), newEpoch);
            }
        }
    }

    /**
     * Raft Snapshot Process Methods
     *
     * Not used for lease management
     *
     * For Raft callback only
     * */
    @Override
    public boolean save(String path) {
        return save(path, repo);
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
        try {
            Map<String, Lease<T>> leaseStore = load(path, repo.getClass());
            lock.writeLock().lock();
            try {
                this.repo.clear();
                this.repo.putAll(leaseStore);
                this.currentEpoch.set(DatumVersionUtil.nextId());
            } finally {
                lock.writeLock().unlock();
            }
        } catch (IOException e) {
            logger.error("Load lease manager data error!", e);
            return false;
        }
        return true;
    }

    private <T> T load(String path, Class<T> clazz) throws IOException {
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
        return leaseManager;
    }

    @Override
    public Set<String> getSnapshotFileNames() {
        Set<String> set = Sets.newHashSet();
        set.add(storageSnapshotPath);
        return set;
    }

    protected DefaultLeaseManager<T> copyMySelf() {
        return new DefaultLeaseManager<>(snapshotFilePrefix);
    }
}
