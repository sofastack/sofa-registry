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
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService.SystemDataStorage;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * @author xiaojian.xj
 * @version $Id: AbstractFetchSystemPropertyService.java, v 0.1 2021年05月16日 13:32 xiaojian.xj Exp $
 */
public abstract class AbstractFetchSystemPropertyService<T extends SystemDataStorage>
    implements FetchSystemPropertyService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchSystemPropertyService.class);

  protected static final long INIT_VERSION = -1L;

  private final String dataInfoId;

  protected final AtomicReference<T> storage = new AtomicReference<>();

  private final AtomicBoolean watcherInited = new AtomicBoolean(false);

  protected final WatchDog watchDog = new WatchDog();

  @Autowired protected MetaServerService metaNodeService;

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
    FetchSystemPropertyResult response =
        metaNodeService.fetchSystemProperty(dataInfoId, expect.version);

    ParaCheckUtil.checkNotNull(response, "fetchSystemPropertyResult");
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "[FetchSystemProperty]dataInfoId:{}, version:{}, response:{}",
          dataInfoId,
          expect,
          response);
    }
    if (!response.isVersionUpgrade()) {
      return true;
    }

    // do process
    return processorData(response.getProvideData(), expect);
  }

  public AbstractFetchSystemPropertyService(String dataInfoId) {
    this.dataInfoId = dataInfoId;
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
              StringFormatter.format("FetchSystemProperty-{}", dataInfoId), watchDog)
          .start();
    }

    return watcherInited.get();
  }

  private boolean processorData(ProvideData data, T expect) {
    Assert.isTrue(
        data != null,
        StringFormatter.format(
            "[FetchSystemProperty]dataInfoId:{}, versionUpgrade:{}, but provideData is null.",
            dataInfoId,
            true));

    try {
      if (data.getVersion() <= expect.version) {
        LOGGER.warn(
            "Fetch system data={}, currentVersion={}, updateVersion={}",
            dataInfoId,
            expect,
            data.getVersion());
        return false;
      }
    } catch (Throwable e) {
      LOGGER.error("Fetch session stopPushSwitch error.", e);
      return false;
    }

    // do process
    return doProcess(expect, data);
  }

  @Override
  public boolean support(String dataInfoId) {
    return StringUtils.equals(this.dataInfoId, dataInfoId);
  }

  protected boolean compareAndSet(T expect, T update) {
    return storage.compareAndSet(expect, update);
  }

  protected abstract boolean doProcess(T expect, ProvideData data);

  public abstract class SystemDataStorage {
    final long version;

    public SystemDataStorage(long version) {
      this.version = version;
    }

    /**
     * Getter method for property <tt>version</tt>.
     *
     * @return property value of version
     */
    public long getVersion() {
      return version;
    }
  }

  /**
   * Setter method for property <tt>metaNodeService</tt>.
   *
   * @param metaNodeService value to be assigned to property metaNodeService
   */
  @VisibleForTesting
  public void setMetaNodeService(MetaServerService metaNodeService) {
    this.metaNodeService = metaNodeService;
  }
}
