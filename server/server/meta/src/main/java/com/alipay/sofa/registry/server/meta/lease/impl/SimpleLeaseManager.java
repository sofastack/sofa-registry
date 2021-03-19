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
import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.observer.impl.AbstractLifecycleObservable;
import com.alipay.sofa.registry.server.meta.lease.LeaseManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author chen.zhu
 *     <p>Mar 09, 2021
 */
public class SimpleLeaseManager<T extends Node> extends AbstractLifecycleObservable
    implements LeaseManager<T> {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected final AtomicLong currentEpoch = new AtomicLong();
  // Map[ip-address, Lease{node, duration, timestamp}]
  protected final ConcurrentMap<String, Lease<T>> localRepo = Maps.newConcurrentMap();

  protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  protected void register(Lease<T> lease) {
    if (lease == null) {
      throw new IllegalArgumentException("[register]NullPointer of lease");
    }
    logger.info("[register] register node: {}", lease.getRenewal());

    lock.writeLock().lock();
    try {
      String nodeIp = lease.getRenewal().getNodeUrl().getIpAddress();
      localRepo.putIfAbsent(nodeIp, lease);
      refreshEpoch(DatumVersionUtil.nextId());
    } finally {
      lock.writeLock().unlock();
    }
  }

  protected boolean cancel(Lease<T> lease) {
    if (lease == null) {
      throw new IllegalArgumentException("[cancel]NullPointer of renewal");
    }
    // read lock for concurrent modification, and mutext for renew/register operations
    lock.writeLock().lock();
    boolean result = true;
    try {
      logger.info("[cancel][begin] {}", lease);
      result = localRepo.remove(lease.getRenewal().getNodeUrl().getIpAddress(), lease);
      logger.info("[cancel][end] {}", result);
      refreshEpoch(DatumVersionUtil.nextId());
    } finally {
      lock.writeLock().unlock();
    }
    return result;
  }

  @Override
  public boolean renew(T renewal, int leaseDuration) {
    if (renewal == null) {
      throw new IllegalArgumentException("[renew]NullPointer of renewal");
    }
    int validLeaseDuration = leaseDuration > 0 ? leaseDuration : Lease.DEFAULT_DURATION_SECS;

    lock.writeLock().lock();
    try {
      Lease<T> lease = localRepo.get(renewal.getNodeUrl().getIpAddress());
      if (lease == null) {
        logger.warn(
            "[renew][node not exist, register: {}-{}]",
            renewal.getNodeType(),
            renewal.getNodeUrl().getIpAddress());

        register(new Lease<>(renewal, validLeaseDuration));
        return false;
      }
      logger.info(
          "[renew][renew lease] node: {}-{}, extends: {}s",
          renewal.getNodeType(),
          renewal.getNodeUrl().getIpAddress(),
          validLeaseDuration);

      lease.renew(validLeaseDuration);
    } finally {
      lock.writeLock().unlock();
    }
    return true;
  }

  @Override
  public Lease<T> getLease(T renewal) {
    return localRepo.get(renewal.getNodeUrl().getIpAddress());
  }

  @Override
  public VersionedList<Lease<T>> getLeaseMeta() {
    lock.readLock().lock();
    try {
      return new VersionedList<>(getEpoch(), getClusterMembers());
    } finally {
      lock.readLock().unlock();
    }
  }

  @VisibleForTesting
  protected boolean refreshEpoch(long newEpoch) {
    if (currentEpoch.get() < newEpoch) {
      logger.info("[refreshEpoch] epoch change from {} to {}", currentEpoch.get(), newEpoch);
      currentEpoch.set(newEpoch);
      return true;
    } else {
      logger.info(
          "[refreshEpoch] epoch change not able, current {}, request {}",
          currentEpoch.get(),
          newEpoch);
      return false;
    }
  }

  private long getEpoch() {
    return currentEpoch.get();
  }

  private List<Lease<T>> getClusterMembers() {
    List<Lease<T>> result = Lists.newArrayList();
    try {
      lock.readLock().lock();
      result.addAll(localRepo.values());
      return result;
    } finally {
      lock.readLock().unlock();
    }
  }
}
