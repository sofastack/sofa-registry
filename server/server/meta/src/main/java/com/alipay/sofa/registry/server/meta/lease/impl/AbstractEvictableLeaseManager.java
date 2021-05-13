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
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.lease.Evictable;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author chen.zhu
 *     <p>Mar 09, 2021
 */
public abstract class AbstractEvictableLeaseManager<T extends Node>
    extends LeaderAwareLeaseManager<T> implements Evictable {
  private static final Logger EVICT_LOG = LoggerFactory.getLogger("EVICT");

  private final AtomicLong lastEvictTime = new AtomicLong();

  private final EvictTask evictTask = new EvictTask();

  @Override
  public void doInitialize() throws InitializeException {
    super.doInitialize();
    ConcurrentUtils.createDaemonThread(getClass().getSimpleName(), evictTask).start();
  }

  @Override
  public void doDispose() throws DisposeException {
    evictTask.close();
    super.doDispose();
  }

  @Override
  public void evict() {
    if (lastEvictTime.get() + getEvictBetweenMilli() > System.currentTimeMillis()) {
      logger.warn("[evict][too quick] last evict time: {}", lastEvictTime.get());
      return;
    }
    lastEvictTime.set(System.currentTimeMillis());
    List<Lease<T>> expirations = getExpiredLeases();
    if (expirations.isEmpty()) {
      return;
    }

    for (Lease<T> lease : expirations) {
      Lease<T> doubleCheck = getLease(lease.getRenewal());
      if (doubleCheck.isExpired()) {
        EVICT_LOG.info("[evict]{},{}", doubleCheck.getRenewal().getNodeType(), doubleCheck);
        try {
          cancel(lease);
        } catch (Throwable th) {
          logger.error("[evict] node cancel failure", th);
        }
      }
    }
  }

  protected List<Lease<T>> getExpiredLeases() {
    List<Lease<T>> expires = Lists.newLinkedList();
    for (Lease<T> lease : getLeaseMeta().getClusterMembers()) {
      if (lease.isExpired()) {
        expires.add(lease);
      }
    }
    return expires;
  }

  protected abstract int getEvictBetweenMilli();

  protected abstract int getIntervalMilli();

  private final class EvictTask extends WakeUpLoopRunnable {

    @Override
    public int getWaitingMillis() {
      return getIntervalMilli();
    }

    @Override
    public void runUnthrowable() {
      if (amILeader() && metaLeaderService.amIStableAsLeader()) {
        logger.info("[evict] begin");
        evict();
      }
    }
  }
}
