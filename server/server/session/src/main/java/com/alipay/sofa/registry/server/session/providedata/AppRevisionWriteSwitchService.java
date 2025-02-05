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
import com.alipay.sofa.registry.jdbc.convertor.AppRevisionDomainConvertor;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.util.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/** Author dzdx @Date 2022/8/8 15:11 @Version 1.0 */
public class AppRevisionWriteSwitchService
    extends AbstractFetchSystemPropertyService<AppRevisionWriteSwitchService.SwitchStorage> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppRevisionWriteSwitchService.class);
  @Autowired private SessionServerConfig sessionServerConfig;

  public AppRevisionWriteSwitchService() {
    super(
        ValueConstants.APP_REVISION_WRITE_SWITCH_DATA_ID,
        new SwitchStorage(INIT_VERSION, new AppRevisionDomainConvertor.EnableConfig()));
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return sessionServerConfig.getSystemPropertyIntervalMillis();
  }

  @Override
  protected boolean doProcess(SwitchStorage expect, ProvideData data) {
    final String switchString = ProvideData.toString(data);

    AppRevisionDomainConvertor.EnableConfig enableConfig =
        new AppRevisionDomainConvertor.EnableConfig();
    if (StringUtils.isNotBlank(switchString)) {
      try {
        enableConfig = JsonUtils.read(switchString, AppRevisionDomainConvertor.EnableConfig.class);
      } catch (Throwable e) {
        LOGGER.error("Decode appRevision write switch failed", e);
        return false;
      }
    }
    SwitchStorage update = new SwitchStorage(data.getVersion(), enableConfig);
    if (!compareAndSet(expect, update)) {
      return false;
    }
    AppRevisionDomainConvertor.setEnableConfig(enableConfig);
    LOGGER.info(
        "Fetch appRevision write switch, prev={}, current={}", expect.enableConfig, switchString);
    return true;
  }

  protected static class SwitchStorage extends SystemDataStorage {
    protected final AppRevisionDomainConvertor.EnableConfig enableConfig;

    public SwitchStorage(long version, AppRevisionDomainConvertor.EnableConfig enableConfig) {
      super(version);
      this.enableConfig = enableConfig;
    }
  }
}
