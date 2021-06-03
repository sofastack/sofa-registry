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

import com.alipay.sofa.registry.common.model.metaserver.FetchSystemPropertyResult;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.shared.config.ServerShareConfig;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.alipay.sofa.registry.util.StringFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * @author xiaojian.xj
 * @version $Id: AbstractFetchSystemPropertyService.java, v 0.1 2021年05月16日 13:32 xiaojian.xj Exp $
 */
public abstract class AbstractFetchSystemPropertyService
    implements FetchSystemPropertyService, ProvideDataProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchSystemPropertyService.class);

  private static final Long INIT_VERSION = -1L;

  protected ReadWriteLock lock = new ReentrantReadWriteLock();

  /** The Read lock. */
  protected Lock readLock = lock.readLock();

  /** The Write lock. */
  protected Lock writeLock = lock.writeLock();

  private final String dataInfoId;

  protected AtomicLong version = new AtomicLong(INIT_VERSION);

  private final WatchDog watchDog = new WatchDog();

  @Autowired private ServerShareConfig serverShareConfig;

  @Autowired protected MetaServerService metaNodeService;

  private final class WatchDog extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      doFetchData();
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(
          serverShareConfig.getSystemPropertyIntervalMillis(), TimeUnit.MILLISECONDS);
    }
  }

  private void doFetchData() {
    FetchSystemPropertyResult response =
        metaNodeService.fetchSystemProperty(dataInfoId, version.get());

    Assert.isTrue(
        response != null,
        StringFormatter.format("[FetchSystemProperty]dataInfoId:{} fetch data error.", dataInfoId));

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "[FetchSystemProperty]dataInfoId:{}, version:{}, response:{}",
          dataInfoId,
          version,
          response);
    }
    if (!response.isVersionUpgrade()) {
      return;
    }

    // do process
    processorData(response.getProvideData());
  }

  public AbstractFetchSystemPropertyService(String dataInfoId) {
    this.dataInfoId = dataInfoId;
  }

  @Override
  public void load() {
    doFetchData();
    ConcurrentUtils.createDaemonThread(
            StringFormatter.format("FetchSystemProperty-{}", dataInfoId), watchDog)
        .start();
  }

  @Override
  public boolean processorData(ProvideData data) {
    Assert.isTrue(
        data != null,
        StringFormatter.format(
            "[FetchSystemProperty]dataInfoId:{}, versionUpgrade:{}, but provideData is null.",
            dataInfoId,
            true));

    readLock.lock();
    try {
      if (data.getVersion() <= version.get()) {
        LOGGER.warn(
            "Fetch system data={}, currentVersion={}, updateVersion={}",
            dataInfoId,
            version.get(),
            data.getVersion());
        return false;
      }
    } catch (Throwable e) {
      LOGGER.error("Fetch session stopPushSwitch error.", e);
      return false;
    } finally {
      readLock.unlock();
    }

    // do process
    return doProcess(data);
  }

  @Override
  public boolean support(ProvideData provideData) {
    return StringUtils.equals(dataInfoId, provideData.getDataInfoId());
  }

  protected abstract boolean doProcess(ProvideData data);
}
