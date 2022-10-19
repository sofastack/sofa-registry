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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.constants.EventTypeConstants;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.core.model.SubscriberRegister;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltUtil;
import com.alipay.sofa.registry.server.session.converter.SubscriberConverter;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.strategy.SubscriberHandlerStrategy;
import com.alipay.sofa.registry.server.shared.remoting.RemotingHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.async.Hack;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultSubscriberHandlerStrategy implements SubscriberHandlerStrategy {
  private static final Logger SUB_LOGGER =
      Hack.hackLoggerDisruptor(LoggerFactory.getLogger("SUB-RECEIVE"));

  @Autowired protected Registry sessionRegistry;

  @Override
  public void handleSubscriberRegister(
      Channel channel, SubscriberRegister subscriberRegister, RegisterResponse registerResponse) {
    Subscriber subscriber = null;
    boolean fromPb = RemotingHelper.isMarkProtobuf(channel);
    try {
      String ip = channel.getRemoteAddress().getAddress().getHostAddress();
      int port = channel.getRemoteAddress().getPort();
      subscriberRegister.setIp(ip);
      subscriberRegister.setPort(port);

      if (StringUtils.isBlank(subscriberRegister.getZone())) {
        subscriberRegister.setZone(ValueConstants.DEFAULT_ZONE);
      }

      if (StringUtils.isBlank(subscriberRegister.getInstanceId())) {
        subscriberRegister.setInstanceId(DEFAULT_INSTANCE_ID);
      }

      subscriber = SubscriberConverter.convert(subscriberRegister);
      subscriber.setProcessId(ip + ":" + port);

      handle(subscriber, channel, subscriberRegister, registerResponse, fromPb);
    } catch (Throwable e) {
      handleError(subscriberRegister, subscriber, registerResponse, fromPb, e);
    }
  }

  protected void handle(
      Subscriber subscriber,
      Channel channel,
      SubscriberRegister subscriberRegister,
      RegisterResponse registerResponse,
      boolean pb) {
    subscriber.setSourceAddress(
        new URL(channel.getRemoteAddress(), BoltUtil.getBoltCustomSerializer(channel)));
    subscriber.setTargetAddress(new URL(channel.getLocalAddress()));

    final String eventType = subscriberRegister.getEventType();
    if (EventTypeConstants.REGISTER.equals(eventType)) {
      sessionRegistry.register(subscriber, channel);
    } else if (EventTypeConstants.UNREGISTER.equals(eventType)) {
      sessionRegistry.unRegister(subscriber);
    } else {
      RegisterLogs.REGISTER_LOGGER.warn("unsupported subscriber.eventType:{}", eventType);
    }
    registerResponse.setVersion(subscriberRegister.getVersion());
    registerResponse.setRegistId(subscriberRegister.getRegistId());
    registerResponse.setSuccess(true);
    registerResponse.setMessage("Subscriber register success!");
    log(true, subscriberRegister, subscriber, pb);
  }

  private void log(
      boolean success, SubscriberRegister subscriberRegister, Subscriber subscriber, boolean pb) {
    // [Y|N],[R|U|N],app,zone,dataInfoId,registerId,scope,elementType,clientVersion,clientIp,clientPort
    Metrics.Access.subCount(success);
    SUB_LOGGER.info(
        "{},{},{},{},{},G={},I={},{},{},{},{},{},{},{},{},pb={},attrs={},multi={}",
        success ? 'Y' : 'N',
        EventTypeConstants.getEventTypeFlag(subscriberRegister.getEventType()),
        subscriberRegister.getAppName(),
        subscriberRegister.getZone(),
        subscriberRegister.getDataId(),
        subscriberRegister.getGroup(),
        subscriberRegister.getInstanceId(),
        subscriberRegister.getRegistId(),
        subscriberRegister.getScope(),
        subscriber == null ? "" : subscriber.getElementType(),
        subscriber == null ? "" : subscriber.getClientVersion(),
        subscriber == null ? "" : subscriber.getRegisterTimestamp(),
        subscriber == null ? "" : subscriber.getVersion(),
        subscriberRegister.getIp(),
        subscriberRegister.getPort(),
        pb ? 'Y' : 'N',
        subscriber == null ? "0" : subscriber.attributesSize(),
        subscriberRegister.acceptMulti() ? 'Y' : 'N');
  }

  protected void handleError(
      SubscriberRegister subscriberRegister,
      Subscriber subscriber,
      RegisterResponse registerResponse,
      boolean pb,
      Throwable e) {
    log(false, subscriberRegister, subscriber, pb);
    RegisterLogs.logError(subscriberRegister, "Subscriber", registerResponse, e);
  }
}
