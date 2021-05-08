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
package com.alipay.sofa.registry.server.session.provideData.processor;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.sessionserver.GrayOpenPushSwitchRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.provideData.ProvideDataProcessor;
import com.alipay.sofa.registry.server.session.push.PushSwitchService;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class GrayPushSwitchProvideDataProcessor implements ProvideDataProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(StopPushProvideDataProcessor.class);

  @Autowired private PushSwitchService pushSwitchService;

  @Autowired private Registry sessionRegistry;

  @Override
  public void changeDataProcess(ProvideData provideData) {
    if (provideData == null) {
      LOGGER.info("fetch session gray pushSwitch null");
      return;
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
    Collection<String> prev = pushSwitchService.getOpenIps();
    pushSwitchService.setOpenIPs(req.getIps());
    if (pushSwitchService.isGlobalPushSwitchStopped() && pushSwitchService.canPush()) {
      sessionRegistry.fetchChangDataProcess();
    }
    LOGGER.info("fetch session gray pushSwitch={}, prev={}", req, prev);
  }

  @Override
  public void fetchDataProcess(ProvideData provideData) {
    changeDataProcess(provideData);
  }

  @Override
  public boolean support(ProvideData provideData) {
    return ValueConstants.PUSH_SWITCH_GRAY_OPEN_DATA_ID.equals(provideData.getDataInfoId());
  }
}
