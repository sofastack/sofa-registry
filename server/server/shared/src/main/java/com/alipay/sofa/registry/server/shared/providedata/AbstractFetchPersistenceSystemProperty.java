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
package com.alipay.sofa.registry.server.shared.providedata;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang.StringUtils;

/**
 * @author xiaojian.xj
 * @version : AbstractFetchPersistenceSystemPropertyService.java, v 0.1 2021年07月22日 22:02
 *     xiaojian.xj Exp $
 */
public abstract class AbstractFetchPersistenceSystemProperty<
        T extends SystemDataStorage, E extends SystemDataStorage>
    implements FetchSystemPropertyService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchSystemPropertyService.class);

  protected static final long INIT_VERSION = -1L;

  private final String dataInfoId;

  protected final AtomicReference<T> storage = new AtomicReference<>();

  private final AtomicBoolean watcherInited = new AtomicBoolean(false);

  protected final WatchDog watchDog = new WatchDog();

  public AbstractFetchPersistenceSystemProperty(String dataInfoId, T t) {
    ParaCheckUtil.checkNotNull(t, "storage is null");
    this.dataInfoId = dataInfoId;
    storage.set(t);
  }

  protected final class WatchDog extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      doFetchData();
    }

    @Override
    public int getWaitingMillis() {
      return getSystemPropertyIntervalMillis();
    }
  }

  protected abstract int getSystemPropertyIntervalMillis();

  protected boolean doFetchData() {
    T expect = storage.get();
    E fetchData = fetchFromPersistence();

    if (fetchData == null) {
      LOGGER.error(
          "Fetch persistence system data={}, currentVersion={}, updateVersion={}",
          dataInfoId,
          expect,
          fetchData.getVersion());
      return false;
    }

    if (fetchData.getVersion() < expect.getVersion()) {
      LOGGER.warn(
          "Fetch persistence system data={}, currentVersion={}, updateVersion={}",
          dataInfoId,
          expect,
          fetchData.getVersion());
      return false;
    } else if (fetchData.getVersion() == expect.getVersion()) {
      return true;
    }

    return doProcess(expect, fetchData);
  }

  @Override
  public boolean support(String dataInfoId) {
    return StringUtils.equals(this.dataInfoId, dataInfoId);
  }

  @Override
  public boolean doFetch() {
    watchDog.wakeup();
    return true;
  }

  @Override
  public boolean start() {
    doFetchData();
    if (watcherInited.compareAndSet(false, true)) {
      ConcurrentUtils.createDaemonThread(
              StringFormatter.format("FetchPersistenceSystemProperty-{}", dataInfoId), watchDog)
          .start();
    }

    return watcherInited.get();
  }

  protected boolean compareAndSet(T expect, T update) {
    return storage.compareAndSet(expect, update);
  }

  protected abstract E fetchFromPersistence();

  protected abstract boolean doProcess(T expect, E data);
}
