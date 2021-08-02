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
package com.alipay.sofa.registry.server.session.strategy.impl;

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.core.constants.EventTypeConstants;
import com.alipay.sofa.registry.core.model.ConfiguratorRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltUtil;
import com.alipay.sofa.registry.server.session.converter.SubscriberConverter;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.strategy.WatcherHandlerStrategy;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultWatcherHandlerStrategy implements WatcherHandlerStrategy {
  private static final Logger WATCH_LOGGER = LoggerFactory.getLogger("WATCH-RECEIVE");

  @Autowired protected Registry sessionRegistry;

  @Override
  public void handleConfiguratorRegister(
      Channel channel,
      ConfiguratorRegister configuratorRegister,
      RegisterResponse registerResponse) {
    Watcher watcher = null;
    try {
      configuratorRegister.setIp(channel.getRemoteAddress().getAddress().getHostAddress());
      configuratorRegister.setPort(channel.getRemoteAddress().getPort());

      if (StringUtils.isBlank(configuratorRegister.getInstanceId())) {
        configuratorRegister.setInstanceId(DEFAULT_INSTANCE_ID);
      }

      watcher = SubscriberConverter.convert(configuratorRegister);
      watcher.setProcessId(
          channel.getRemoteAddress().getHostName() + ":" + channel.getRemoteAddress().getPort());

      handle(watcher, channel, configuratorRegister, registerResponse);
    } catch (Throwable e) {
      handleError(configuratorRegister, watcher, registerResponse, e);
    }
  }

  protected void handle(
      Watcher watcher,
      Channel channel,
      ConfiguratorRegister register,
      RegisterResponse registerResponse) {
    watcher.setSourceAddress(
        new URL(channel.getRemoteAddress(), BoltUtil.getBoltCustomSerializer(channel)));
    watcher.setTargetAddress(new URL(channel.getLocalAddress()));

    final String eventType = register.getEventType();
    if (EventTypeConstants.REGISTER.equals(eventType)) {
      sessionRegistry.register(watcher, channel);
    } else if (EventTypeConstants.UNREGISTER.equals(eventType)) {
      sessionRegistry.unRegister(watcher);
    } else {
      RegisterLogs.REGISTER_LOGGER.warn("unsupported watch.eventType:{}", eventType);
    }
    registerResponse.setVersion(register.getVersion());
    registerResponse.setRegistId(register.getRegistId());
    registerResponse.setSuccess(true);
    registerResponse.setMessage("ConfiguratorRegister register success!");
    log(true, register, watcher);
  }

  private void log(boolean success, ConfiguratorRegister register, Watcher watcher) {
    // [Y|N],[R|U|N],app,zone,dataInfoId,registerId,clientVersion,clientIp,clientPort
    Metrics.Access.watCount(success);
    WATCH_LOGGER.info(
        "{},{},{},{},{},{},{},{},{},{},{},attrs={}",
        success ? 'Y' : 'N',
        EventTypeConstants.getEventTypeFlag(register.getEventType()),
        register.getAppName(),
        register.getZone(),
        register.getDataId(),
        register.getGroup(),
        register.getInstanceId(),
        register.getRegistId(),
        watcher == null ? "" : watcher.getClientVersion(),
        register.getIp(),
        register.getPort(),
        watcher == null ? "0" : watcher.attributesSize());
  }

  protected void handleError(
      ConfiguratorRegister register,
      Watcher watcher,
      RegisterResponse registerResponse,
      Throwable e) {
    log(false, register, watcher);
    RegisterLogs.logError(register, "Watcher", registerResponse, e);
  }
}
