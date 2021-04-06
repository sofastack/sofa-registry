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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: DefaultProvideDataService.java, v 0.1 2021年04月06日 16:17 xiaojian.xj Exp $
 */
public class DefaultProvideDataService implements ProvideDataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProvideDataService.class);

  private Map<String, DBResponse> provideDataCache = new ConcurrentHashMap<>();

  private static final ReadWriteLock lock = new ReentrantReadWriteLock();

  private ProvideDataRefresh refresher;

  @Autowired private ProvideDataRepository provideDataRepository;

  private final class ProvideDataRefresh extends WakeUpLoopRunnable {

    @Override
    public int getWaitingMillis() {
      return 3000;
    }

    @Override
    public void runUnthrowable() {
      provideDataRefresh();
    }
  }

  private void provideDataRefresh() {
    Map<String, DBResponse> provideDataMap = provideDataRepository.getAll();
    try {
      LOGGER.info(
          "refresh provide data, old size: {}, new size: {}",
          provideDataCache.size(),
          provideDataMap.size());
      lock.writeLock().lock();
      provideDataCache = provideDataMap;
    } catch (Throwable t) {
      LOGGER.error("refresh provide data error.", t);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void becomeLeader() {
    refresher = new ProvideDataRefresh();
    ConcurrentUtils.createDaemonThread("provideData_refresh", refresher).start();
  }

  @Override
  public void loseLeader() {
    refresher.close();
  }

  @Override
  public boolean saveProvideData(String key, String value) {

    try {
      provideDataRepository.put(key, value);
      lock.writeLock().lock();
      provideDataCache.put(key, DBResponse.ok(value).build());
    } catch (Throwable t) {
      LOGGER.error("save provide data: {} error.", key, t);
      return false;
    } finally {
      lock.writeLock().unlock();
    }
    return true;
  }

  @Override
  public DBResponse queryProvideData(String key) {
    DBResponse response = null;
    try {
      lock.readLock().lock();
      response = provideDataCache.get(key);
    } catch (Throwable t) {
      LOGGER.error("query provide data: {} error.", key, t);
    } finally {
      lock.readLock().unlock();
    }

    if (response == null && !provideDataCache.containsKey(key)) {
      return DBResponse.notfound().build();
    } else if (response == null) {
      response = provideDataRepository.get(key);
    }
    return response;
  }

  @Override
  public boolean removeProvideData(String key) {
    try {
      provideDataRepository.remove(key);
      lock.writeLock().lock();
      provideDataCache.remove(key);
    } catch (Throwable t) {
      LOGGER.error("remove provide data: {} error.", key, t);
      return false;
    } finally {
      lock.writeLock().unlock();
    }
    return true;
  }
}
