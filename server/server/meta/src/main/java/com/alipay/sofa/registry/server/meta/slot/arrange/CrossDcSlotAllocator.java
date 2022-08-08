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
package com.alipay.sofa.registry.server.meta.slot.arrange;

import com.alipay.sofa.registry.common.model.metaserver.GetSlotTableRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.datacenter.DataCenterAware;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.metaserver.CrossDcMetaServer;
import com.alipay.sofa.registry.server.meta.slot.SlotAllocator;
import com.alipay.sofa.registry.server.meta.slot.SlotTableAware;
import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author chen.zhu
 *     <p>Nov 20, 2020
 */
public class CrossDcSlotAllocator extends AbstractLifecycle
    implements SlotAllocator, DataCenterAware, SlotTableAware {

  private final String dcName;

  private final ScheduledExecutorService scheduled;

  private final CrossDcMetaServer metaServer;

  private volatile ScheduledFuture<?> future;

  private final AtomicReference<SlotTable> currentSlotTable = new AtomicReference<>();

  private final Exchange exchange;

  private final MetaLeaderService metaLeaderService;

  private final AtomicInteger index = new AtomicInteger(0);

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * Constructor.
   *
   * @param dcName the dc name
   * @param scheduled the scheduled
   * @param exchange the exchange
   * @param metaServer the meta server
   */
  public CrossDcSlotAllocator(
      String dcName,
      ScheduledExecutorService scheduled,
      Exchange exchange,
      CrossDcMetaServer metaServer,
      MetaLeaderService metaLeaderService) {
    this.dcName = dcName;
    this.scheduled = scheduled;
    this.exchange = exchange;
    this.metaServer = metaServer;
    this.metaLeaderService = metaLeaderService;
  }

  /**
   * Gets get slot table.
   *
   * @return the get slot table
   */
  @Override
  public SlotTable getSlotTable() {
    if (!getLifecycleState().isStarted() && !getLifecycleState().isStarting()) {
      throw new IllegalStateException("[RemoteDcSlotAllocator]not available not");
    }
    return currentSlotTable.get();
  }

  /**
   * Gets get dc.
   *
   * @return the get dc
   */
  @Override
  public String getDc() {
    return dcName;
  }

  @Override
  protected void doInitialize() throws InitializeException {
    super.doInitialize();
  }

  @Override
  protected void doStart() throws StartException {
    future =
        scheduled.scheduleWithFixedDelay(
            new Runnable() {
              @Override
              public void run() {
                if (metaLeaderService.amIStableAsLeader()) {
                  refreshSlotTable(0);
                }
              }
            },
            getIntervalMilli(),
            getIntervalMilli(),
            TimeUnit.MILLISECONDS);
  }

  @Override
  protected void doStop() throws StopException {
    if (future != null) {
      future.cancel(true);
      future = null;
    }
  }

  @Override
  protected void doDispose() throws DisposeException {
    super.doDispose();
  }

  /**
   * Sets slot table.
   *
   * @param slotTable the slot table
   */
  public void setSlotTable(SlotTable slotTable) {
    currentSlotTable.set(slotTable);
  }

  @VisibleForTesting
  protected long getIntervalMilli() {
    return 60 * 1000;
  }

  @VisibleForTesting
  protected void refreshSlotTable(int retryTimes) {
    if (retryTimes > 3) {
      logger.warn("[refreshSlotTable]try timeout, more than {} times", 3);
      return;
    }
    final long currentEpoch =
        currentSlotTable.get() == null ? -1 : currentSlotTable.get().getEpoch();
    GetSlotTableRequest request = new GetSlotTableRequest(currentEpoch, null, false);
    MetaNode metaNode = getRemoteMetaServer();
    exchange
        .getClient(Exchange.META_SERVER_TYPE)
        .sendCallback(
            metaNode.getNodeUrl(),
            request,
            new CallbackHandler() {
              @Override
              @SuppressWarnings("unchecked")
              public void onCallback(Channel channel, Object message) {
                if (!(message instanceof SlotTable)) {
                  logger.error(
                      "[refreshSlotTable]wanted SlotTable, but receive: [{}]{}",
                      message.getClass(),
                      message);
                  return;
                }
                SlotTable slotTable = (SlotTable) message;
                lock.writeLock().lock();
                try {
                  if (currentEpoch < slotTable.getEpoch()) {
                    if (logger.isWarnEnabled()) {
                      logger.warn(
                          "[refreshSlotTable] remote slot table changed, \n prev: {} \n change to: {}",
                          currentSlotTable.get(),
                          slotTable);
                    }
                    currentSlotTable.set(slotTable);
                  }
                } finally {
                  lock.writeLock().unlock();
                }
              }

              @Override
              public void onException(Channel channel, Throwable exception) {
                if (logger.isErrorEnabled()) {
                  logger.error(
                      "[refreshSlotTable][{}]Bolt Request Failure, remote: {}, will try other meta-server",
                      getDc(),
                      channel == null ? "unknown" : channel.getRemoteAddress().getHostName(),
                      exception);
                }
                index.set(index.incrementAndGet() % metaServer.getClusterMembers().size());
                // if failure, try again with another meta server.
                // good luck with that. :)
                refreshSlotTable(retryTimes + 1);
              }

              @Override
              public Executor getExecutor() {
                return scheduled;
              }
            },
            5000);
  }

  private MetaNode getRemoteMetaServer() {
    return metaServer.getClusterMembers().get(index.get());
  }

  @Override
  public String toString() {
    return "CrossDcSlotAllocator{"
        + "dcName='"
        + dcName
        + '\''
        + ", metaServer="
        + metaServer
        + '}';
  }
}
