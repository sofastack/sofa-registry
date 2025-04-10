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
package com.alipay.sofa.registry.server.session.providedata;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.acceptor.ClientOffWriteDataRequest;
import com.alipay.sofa.registry.server.session.acceptor.WriteDataAcceptor;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.providedata.FetchDataInfoIDBlackListService.DataInfoIDBlacklistStorage;
import com.alipay.sofa.registry.server.session.store.SessionDataStore;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huicha
 * @date 2024/12/13
 */
public class FetchDataInfoIDBlackListService
    extends AbstractFetchSystemPropertyService<DataInfoIDBlacklistStorage> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FetchDataInfoIDBlackListService.class, "[DataInfoIDBlackList]");

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private SessionDataStore sessionDataStore;

  @Autowired private WriteDataAcceptor writeDataAcceptor;

  private AtomicBoolean start;

  private WatchDog watchDog;

  public FetchDataInfoIDBlackListService() {
    super(
        ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID,
        new DataInfoIDBlacklistStorage(INIT_VERSION, Collections.emptySet()));
    this.start = new AtomicBoolean(false);
    this.watchDog = new WatchDog();
  }

  @Override
  public boolean start() {
    if (this.start.compareAndSet(false, true)) {
      ConcurrentUtils.createDaemonThread("ProcessDataInfoIDBlackListThread", watchDog).start();
    }
    return super.start();
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return this.sessionServerConfig.getSystemPropertyIntervalMillis();
  }

  @Override
  protected boolean doProcess(DataInfoIDBlacklistStorage oldStorage, ProvideData provideData) {
    try {
      // 收到新数据，开始处理
      // 1. 首先解析出新的被拉黑的 dataInfoID 列表
      String data = ProvideData.toString(provideData);
      Set<String> newDataInfoIds = JsonUtils.read(data, new TypeReference<Set<String>>() {});

      // 2. 首先覆盖保存，让新的 Publisher 可以被忽略，这样可以避免新增的 Publisher 被遗漏清理
      if (!this.getStorage()
          .compareAndSet(
              oldStorage,
              new DataInfoIDBlacklistStorage(provideData.getVersion(), newDataInfoIds))) {
        // 覆盖失败了那么可能是有并发冲突，跳过处理
        return false;
      }

      // 3. 唤醒 WatchDog 清理数据
      this.watchDog.wakeup();

      return true;
    } catch (Throwable throwable) {
      LOGGER.error("Process new dataInfoId black list exception", throwable);
      return false;
    }
  }

  public boolean isInBlackList(String dataInfoId) {
    DataInfoIDBlacklistStorage storage = this.storage.get();
    if (null == storage) {
      return false;
    }

    Set<String> dataInfoIds = storage.getDataInfoIds();
    if (CollectionUtils.isEmpty(dataInfoIds)) {
      return false;
    }

    return dataInfoIds.contains(dataInfoId);
  }

  private void safeCleanPublisher() {
    try {
      // 这里主要是为了防止多线程并发来清理 Publisher 的数据，因此使用一个单独的线程来进行清理工作
      // 并且这样也可以增加一个兜底清理的机制，防止清理失败后再没有机会补偿

      // 1. 查询出当前需要拉黑的 dataInfoId
      DataInfoIDBlacklistStorage storage = this.getStorage().get();
      if (null == storage) {
        return;
      }

      Set<String> dataInfoIds = storage.getDataInfoIds();
      if (CollectionUtils.isEmpty(dataInfoIds)) {
        return;
      }

      // 2. 进行清理
      // 查询出当前 Session 上所有发布了 dataInfoId 的 Publisher，并按照和 Data 的链接 id 分组
      Map<ConnectId, List<Publisher>> publisherMap =
          dataInfoIds.stream()
              .flatMap(dataInfoId -> this.sessionDataStore.getDatas(dataInfoId).stream())
              .collect(Collectors.groupingBy(Publisher::connectId));

      // 3. 遍历清理 Publisher 数据
      for (Map.Entry<ConnectId, List<Publisher>> entry : publisherMap.entrySet()) {
        ConnectId connectId = entry.getKey();
        List<Publisher> publishers = entry.getValue();
        this.safeCleanData(connectId, publishers);
      }
    } catch (Throwable throwable) {
      LOGGER.error("[WatchDog] clean publisher exception", throwable);
    }
  }

  private void safeCleanData(ConnectId connectId, List<Publisher> publishers) {
    try {
      // 通知 Data 清理数据
      this.writeDataAcceptor.accept(new ClientOffWriteDataRequest(connectId, publishers));
      for (Publisher publisher : publishers) {
        // 移除 Session 内存中的 Publisher
        this.safeDeletePublisher(publisher);
      }
    } catch (Throwable throwable) {
      LOGGER.error("Clean data exception", throwable);
    }
  }

  private void safeDeletePublisher(Publisher publisher) {
    try {
      this.sessionDataStore.deleteById(publisher.getRegisterId(), publisher.getDataInfoId());
    } catch (Throwable throwable) {
      LOGGER.error("Delete publisher exception", throwable);
    }
  }

  protected final class WatchDog extends WakeUpLoopRunnable {
    @Override
    public int getWaitingMillis() {
      // 由于数据发生变化的时候，会有一个通知机制主动唤醒这个 WatchDog 执行
      // Publisher 清理任务，而这个扫描绝大多数时候的执行都是不会找到可执行的 Publisher 的
      // 因此这里的自动执行间隔需要配置的更大一些，不需要那么频繁
      return FetchDataInfoIDBlackListService.this.sessionServerConfig
          .getScanPublisherInDataInfoIdBlackListIntervalMillis();
    }

    @Override
    public void runUnthrowable() {
      FetchDataInfoIDBlackListService.this.safeCleanPublisher();
    }
  }

  protected static class DataInfoIDBlacklistStorage extends SystemDataStorage {

    private Set<String> dataInfoIds;

    public DataInfoIDBlacklistStorage(Long version, Set<String> dataInfoIds) {
      super(version);
      this.dataInfoIds = dataInfoIds;
    }

    public Set<String> getDataInfoIds() {
      return dataInfoIds;
    }
  }

  @VisibleForTesting
  public FetchDataInfoIDBlackListService setSessionServerConfig(
      SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
    return this;
  }

  @VisibleForTesting
  public FetchDataInfoIDBlackListService setSessionDataStore(SessionDataStore sessionDataStore) {
    this.sessionDataStore = sessionDataStore;
    return this;
  }

  @VisibleForTesting
  public FetchDataInfoIDBlackListService setWriteDataAcceptor(WriteDataAcceptor writeDataAcceptor) {
    this.writeDataAcceptor = writeDataAcceptor;
    return this;
  }
}
