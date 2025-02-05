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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.sessionserver.GrayOpenPushSwitchRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.providedata.FetchGrayPushSwitchService.GrayPushSwitchStorage;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class FetchGrayPushSwitchService
    extends AbstractFetchSystemPropertyService<GrayPushSwitchStorage> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FetchGrayPushSwitchService.class);

  @Autowired private SessionServerConfig sessionServerConfig;

  public FetchGrayPushSwitchService() {
    super(
        ValueConstants.PUSH_SWITCH_GRAY_OPEN_DATA_ID,
        new GrayPushSwitchStorage(INIT_VERSION, Collections.emptySet()));
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return sessionServerConfig.getSystemPropertyIntervalMillis();
  }

  @Override
  protected boolean doProcess(GrayPushSwitchStorage expect, ProvideData provideData) {
    if (provideData == null) {
      LOGGER.info("fetch session gray pushSwitch null");
      return true;
    }
    final String data = ProvideData.toString(provideData);

    GrayOpenPushSwitchRequest req = new GrayOpenPushSwitchRequest();
    if (!StringUtils.isBlank(data)) {
      ObjectMapper mapper = JsonUtils.getJacksonObjectMapper();
      try {
        req = mapper.readValue(data, GrayOpenPushSwitchRequest.class);
      } catch (IOException e) {
        throw new RuntimeException(String.format("parse gray open switch failed: %s", data), e);
      }
    }
    GrayPushSwitchStorage update =
        new GrayPushSwitchStorage(provideData.getVersion(), req.getIps());
    if (!compareAndSet(expect, update)) {
      return false;
    }

    LOGGER.info("fetch session gray pushSwitch={}, prev={}", req, expect.openIps);
    return true;
  }

  protected static class GrayPushSwitchStorage extends SystemDataStorage {
    final Set<String> openIps;

    public GrayPushSwitchStorage(long version, Collection<String> openIps) {
      super(version);
      this.openIps = openIps == null ? Collections.emptySet() : Sets.newHashSet(openIps);
    }
  }

  public Collection<String> getOpenIps() {
    return storage.get().openIps;
  }

  protected boolean canPush() {
    return CollectionUtils.isNotEmpty(storage.get().openIps);
  }

  @VisibleForTesting
  public void setOpenIps(long version, Collection<String> openIps) {
    storage.set(new GrayPushSwitchStorage(version, openIps));
  }

  /**
   * Setter method for property <tt>sessionServerConfig</tt>.
   *
   * @param sessionServerConfig value to be assigned to property sessionServerConfig
   * @return FetchGrayPushSwitchService
   */
  @VisibleForTesting
  protected FetchGrayPushSwitchService setSessionServerConfig(
      SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
    return this;
  }
}
