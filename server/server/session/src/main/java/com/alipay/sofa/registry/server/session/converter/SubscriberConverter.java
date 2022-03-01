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
package com.alipay.sofa.registry.server.session.converter;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.store.BaseInfo.ClientVersion;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.converter.ScopeEnumConverter;
import com.alipay.sofa.registry.core.model.ConfiguratorRegister;
import com.alipay.sofa.registry.core.model.SubscriberRegister;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.util.ParaCheckUtil;

/**
 * The type Subscriber converter.
 *
 * @author shangyu.wh
 * @version $Id : SubscriberConverter.java, v 0.1 2017-12-05 11:00 shangyu.wh Exp $
 */
public final class SubscriberConverter {
  private SubscriberConverter() {}
  /**
   * Convert subscriber.
   *
   * @param subscriberRegister the subscriber register
   * @return the subscriber
   */
  public static Subscriber convert(SubscriberRegister subscriberRegister, Channel channel) {

    Converter<Tuple<SubscriberRegister, Channel>, Subscriber> converter =
        src -> {
          Subscriber subscriber = new Subscriber();
          SubscriberRegister register = src.getFirst();
          Channel chan = src.getSecond();
          ParaCheckUtil.checkNotNull(chan, "channel");

          subscriber.setAppName(register.getAppName());
          subscriber.setCell(register.getZone());
          subscriber.setClientId(register.getClientId());
          subscriber.setDataId(register.getDataId());
          subscriber.setGroup(register.getGroup());
          subscriber.setInstanceId(register.getInstanceId());
          subscriber.setRegisterId(register.getRegistId());
          subscriber.setProcessId(register.getProcessId());
          subscriber.setVersion(register.getVersion());
          subscriber.setRegisterTimestamp(System.currentTimeMillis());
          subscriber.setClientRegisterTimestamp(register.getTimestamp());
          subscriber.setScope(ScopeEnumConverter.convertToScope(register.getScope()));
          subscriber.setSourceAddress(
              new URL(chan.getRemoteAddress().getHostString(), chan.getRemoteAddress().getPort()));
          subscriber.setTargetAddress(
              new URL(chan.getLocalAddress().getHostString(), chan.getLocalAddress().getPort()));
          subscriber.setIp(register.getIp());
          subscriber.setAttributes(register.getAttributes());
          subscriber.setClientVersion(ClientVersion.StoreData);
          subscriber.internAcceptEncoding(register.getAcceptEncoding());

          DataInfo dataInfo =
              new DataInfo(register.getInstanceId(), register.getDataId(), register.getGroup());

          subscriber.setDataInfoId(dataInfo.getDataInfoId());

          return subscriber;
        };
    return converter.convert(new Tuple<>(subscriberRegister, channel));
  }
  /**
   * Convert watcher.
   *
   * @param configuratorRegister
   * @return
   */
  public static Watcher convert(ConfiguratorRegister configuratorRegister, Channel channel) {
    Converter<Tuple<ConfiguratorRegister, Channel>, Watcher> converter =
        src -> {
          Watcher watcher = new Watcher();
          ConfiguratorRegister register = src.getFirst();
          Channel chan = src.getSecond();
          ParaCheckUtil.checkNotNull(chan, "channel");

          watcher.setAppName(register.getAppName());
          watcher.setCell(register.getZone());
          watcher.setClientId(register.getClientId());
          watcher.setDataId(register.getDataId());
          watcher.setGroup(register.getGroup());
          watcher.setInstanceId(register.getInstanceId());
          watcher.setRegisterId(register.getRegistId());
          watcher.setProcessId(register.getProcessId());
          watcher.setVersion(register.getVersion());
          watcher.setAttributes(register.getAttributes());
          if (register.getTimestamp() != null) {
            watcher.setClientRegisterTimestamp(register.getTimestamp());
          }
          watcher.setRegisterTimestamp(register.getTimestamp());
          watcher.setSourceAddress(
              new URL(chan.getRemoteAddress().getHostString(), chan.getRemoteAddress().getPort()));
          watcher.setTargetAddress(
              new URL(chan.getLocalAddress().getHostString(), chan.getLocalAddress().getPort()));
          watcher.setIp(register.getIp());

          watcher.setClientVersion(ClientVersion.StoreData);

          DataInfo dataInfo =
              new DataInfo(register.getInstanceId(), register.getDataId(), register.getGroup());

          watcher.setDataInfoId(dataInfo.getDataInfoId());

          return watcher;
        };
    return converter.convert(new Tuple<>(configuratorRegister, channel));
  }
}
