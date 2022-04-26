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
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.constants.EventTypeConstants;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.converter.PublisherConverter;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.strategy.PublisherHandlerStrategy;
import com.alipay.sofa.registry.server.shared.remoting.RemotingHelper;
import com.alipay.sofa.registry.server.shared.util.DatumUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.async.Hack;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultPublisherHandlerStrategy implements PublisherHandlerStrategy {
  private static final Logger PUB_LOGGER =
      Hack.hackLoggerDisruptor(LoggerFactory.getLogger("PUB-RECEIVE"));
  @Autowired protected Registry sessionRegistry;

  @Override
  public void handlePublisherRegister(
      Channel channel, PublisherRegister publisherRegister, RegisterResponse registerResponse) {
    Publisher publisher = null;
    boolean fromPb = RemotingHelper.isMarkProtobuf(channel);
    try {
      String ip = channel.getRemoteAddress().getAddress().getHostAddress();
      int port = channel.getRemoteAddress().getPort();
      publisherRegister.setIp(ip);
      publisherRegister.setPort(port);

      if (StringUtils.isBlank(publisherRegister.getZone())) {
        publisherRegister.setZone(ValueConstants.DEFAULT_ZONE);
      }

      if (StringUtils.isBlank(publisherRegister.getInstanceId())) {
        publisherRegister.setInstanceId(DEFAULT_INSTANCE_ID);
      }

      publisher = PublisherConverter.convert(publisherRegister);
      publisher.setProcessId(ip + ":" + port);

      handle(publisher, channel, publisherRegister, registerResponse, fromPb);
    } catch (Throwable e) {
      handleError(publisherRegister, publisher, registerResponse, fromPb, e);
    }
  }

  protected void handle(
      Publisher publisher,
      Channel channel,
      PublisherRegister publisherRegister,
      RegisterResponse registerResponse,
      boolean pb) {
    publisher.setSourceAddress(new URL(channel.getRemoteAddress()));
    publisher.setTargetAddress(new URL(channel.getLocalAddress()));
    final String eventType = publisherRegister.getEventType();
    if (EventTypeConstants.REGISTER.equals(eventType)) {
      sessionRegistry.register(publisher, channel);
    } else if (EventTypeConstants.UNREGISTER.equals(eventType)) {
      sessionRegistry.unRegister(publisher);
    } else {
      RegisterLogs.REGISTER_LOGGER.warn("unsupported publisher.eventType:{}", eventType);
    }
    registerResponse.setSuccess(true);
    registerResponse.setVersion(publisher.getVersion());
    registerResponse.setRegistId(publisherRegister.getRegistId());
    registerResponse.setMessage("Publisher register success!");
    log(true, publisherRegister, publisher, pb);
  }

  private void log(
      boolean success, PublisherRegister publisherRegister, Publisher publisher, boolean pb) {
    // [Y|N],[R|U|N],app,zone,dataInfoId,registerId,version,registerTimestamp,clientVersion,clientIp,clientPort,dataSize
    long size = DatumUtils.DataBoxListSize(publisherRegister.getDataList());
    Metrics.Access.pubCount(success);
    Metrics.Access.pubSize("registry", publisherRegister.getGroup(), size);
    PUB_LOGGER.info(
        "{},{},{},{},{},G={},I={},{},{},{},{},{},{},size={},pb={},attrs={}",
        success ? 'Y' : 'N',
        EventTypeConstants.getEventTypeFlag(publisherRegister.getEventType()),
        publisherRegister.getAppName(),
        publisherRegister.getZone(),
        publisher == null ? publisherRegister.getDataId() : publisher.getDataId(),
        publisherRegister.getGroup(),
        publisherRegister.getInstanceId(),
        publisherRegister.getRegistId(),
        publisherRegister.getVersion(),
        publisher == null ? "" : publisher.getRegisterTimestamp(),
        publisher == null ? "" : publisher.getClientVersion(),
        publisherRegister.getIp(),
        publisherRegister.getPort(),
        size,
        pb ? 'Y' : 'N',
        publisher == null ? "0" : publisher.attributesSize());
  }

  protected void handleError(
      PublisherRegister publisherRegister,
      Publisher publisher,
      RegisterResponse registerResponse,
      boolean pb,
      Throwable e) {
    log(false, publisherRegister, publisher, pb);
    RegisterLogs.logError(publisherRegister, "Publisher", registerResponse, e);
  }
}
