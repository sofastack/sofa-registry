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

import com.alipay.sofa.registry.common.model.store.BaseInfo.ClientVersion;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.converter.ScopeEnumConverter;
import com.alipay.sofa.registry.core.model.ConfiguratorRegister;
import com.alipay.sofa.registry.core.model.SubscriberRegister;

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
  public static Subscriber convert(SubscriberRegister subscriberRegister) {

    Converter<SubscriberRegister, Subscriber> converter =
        source -> {
          Subscriber subscriber = new Subscriber();

          subscriber.setAppName(source.getAppName());
          subscriber.setCell(source.getZone());
          subscriber.setClientId(source.getClientId());
          subscriber.setDataId(source.getDataId());
          subscriber.setGroup(source.getGroup());
          subscriber.setInstanceId(source.getInstanceId());
          subscriber.setRegisterId(source.getRegistId());
          subscriber.setProcessId(source.getProcessId());
          subscriber.setVersion(source.getVersion());
          subscriber.setRegisterTimestamp(System.currentTimeMillis());
          subscriber.setClientRegisterTimestamp(source.getTimestamp());
          subscriber.setScope(ScopeEnumConverter.convertToScope(source.getScope()));
          subscriber.setSourceAddress(new URL(source.getIp(), source.getPort()));
          subscriber.setAttributes(source.getAttributes());
          subscriber.setClientVersion(ClientVersion.StoreData);
          subscriber.internAcceptEncoding(source.getAcceptEncoding());
          subscriber.setAcceptMulti(source.acceptMulti());

          DataInfo dataInfo =
              new DataInfo(source.getInstanceId(), source.getDataId(), source.getGroup());

          subscriber.setDataInfoId(dataInfo.getDataInfoId());

          return subscriber;
        };
    return converter.convert(subscriberRegister);
  }
  /**
   * Convert watcher.
   *
   * @param configuratorRegister configuratorRegister
   * @return Watcher
   */
  public static Watcher convert(ConfiguratorRegister configuratorRegister) {
    Converter<ConfiguratorRegister, Watcher> converter =
        source -> {
          Watcher watcher = new Watcher();

          watcher.setAppName(source.getAppName());
          watcher.setCell(source.getZone());
          watcher.setClientId(source.getClientId());
          watcher.setDataId(source.getDataId());
          watcher.setGroup(source.getGroup());
          watcher.setInstanceId(source.getInstanceId());
          watcher.setRegisterId(source.getRegistId());
          watcher.setProcessId(source.getProcessId());
          watcher.setVersion(source.getVersion());
          watcher.setAttributes(source.getAttributes());
          if (source.getTimestamp() != null) {
            watcher.setClientRegisterTimestamp(source.getTimestamp());
          }
          watcher.setRegisterTimestamp(source.getTimestamp());
          watcher.setSourceAddress(new URL(source.getIp(), source.getPort()));

          watcher.setClientVersion(ClientVersion.StoreData);

          DataInfo dataInfo =
              new DataInfo(source.getInstanceId(), source.getDataId(), source.getGroup());

          watcher.setDataInfoId(dataInfo.getDataInfoId());

          return watcher;
        };
    return converter.convert(configuratorRegister);
  }
}
