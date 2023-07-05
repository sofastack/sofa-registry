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
package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: DefaultProvideDataService.java, v 0.1 2021年04月06日 16:17 xiaojian.xj Exp $
 */
public class DefaultProvideDataService implements ProvideDataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProvideDataService.class);

  private Map<String, PersistenceData> provideDataCache = new ConcurrentHashMap<>();

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private final ProvideDataRefresh refresher = new ProvideDataRefresh();

  @Autowired MetaLeaderService metaLeaderService;

  @Autowired private ProvideDataRepository provideDataRepository;

  @PostConstruct
  public void init() {
    ConcurrentUtils.createDaemonThread("provideData_refresh", refresher).start();
    metaLeaderService.registerListener(this);
  }

  private final class ProvideDataRefresh extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      provideDataRefresh();
    }

    @Override
    public int getWaitingMillis() {
      return 3000;
    }
  }

  private void provideDataRefresh() {
    if (!metaLeaderService.amILeader()) {
      return;
    }
    Map<String, PersistenceData> provideDatas = provideDataRepository.getAll();

    LOGGER.info(
        "refresh provide data, old size: {}, new size: {}",
        provideDataCache.size(),
        provideDatas.size());
    lock.writeLock().lock();
    try {
      provideDataCache = provideDatas;
    } catch (Throwable t) {
      LOGGER.error("refresh provide data error.", t);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void becomeLeader() {
    refresher.wakeup();
  }

  @Override
  public void loseLeader() {}

  /**
   * save or update provideData
   *
   * @param persistenceData persistenceData
   * @return boolean
   */
  @Override
  public boolean saveProvideData(PersistenceData persistenceData) {

    long expectVersion = getExpectVersion(PersistenceDataBuilder.getDataInfoId(persistenceData));
    return saveProvideData(persistenceData, expectVersion);
  }

  /**
   * save or update provideData with expectVersion
   *
   * @param persistenceData persistenceData
   * @return boolean
   */
  @Override
  public boolean saveProvideData(PersistenceData persistenceData, long expectVersion) {
    if (persistenceData.getVersion() <= expectVersion) {
      LOGGER.error(
          "save provide data: {} fail. new version: {} is smaller than old version: {}",
          persistenceData,
          persistenceData.getVersion(),
          expectVersion);
      return false;
    }

    // save with cas
    boolean success = provideDataRepository.put(persistenceData, expectVersion);

    if (success) {
      lock.writeLock().lock();
      try {
        // update local cache
        provideDataCache.put(
            PersistenceDataBuilder.getDataInfoId(persistenceData), persistenceData);
      } catch (Throwable t) {
        LOGGER.error("save provide data: {} error.", persistenceData, t);
        return false;
      } finally {
        lock.writeLock().unlock();
      }
    }
    return success;
  }

  /**
   * get except version
   *
   * @param key key
   * @return long
   */
  private long getExpectVersion(String key) {
    long expectVersion = 0;
    DBResponse<PersistenceData> response = queryProvideData(key);

    if (response.getEntity() != null) {
      expectVersion = response.getEntity().getVersion();
    }
    return expectVersion;
  }

  /**
   * query provideData by key
   *
   * @param key key
   * @return DBResponse
   */
  @Override
  public DBResponse<PersistenceData> queryProvideData(String key) {
    PersistenceData data = null;
    lock.readLock().lock();
    try {
      data = provideDataCache.get(key);
    } catch (Throwable t) {
      LOGGER.error("query provide data: {} error.", key, t);
    } finally {
      lock.readLock().unlock();
    }

    if (data == null && !provideDataCache.containsKey(key)) {
      return DBResponse.notfound().build();
    }
    if (data == null) {
      // not expect to visit this code;
      // if happen, query from persistent and print error log;
      data = provideDataRepository.get(key);
      LOGGER.error("dataKey: {} not exist in cache, response from persistent: {}", key, data);
    }
    return data == null ? DBResponse.notfound().build() : DBResponse.ok(data).build();
  }

  @Override
  public boolean removeProvideData(String key) {
    long expectVersion = getExpectVersion(key);

    boolean success = provideDataRepository.remove(key, expectVersion);

    if (success) {
      lock.writeLock().lock();
      try {
        provideDataCache.remove(key);
      } catch (Throwable t) {
        LOGGER.error("remove provide data: {} error.", key, t);
        return false;
      } finally {
        lock.writeLock().unlock();
      }
    }
    return success;
  }
}
