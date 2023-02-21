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
package com.alipay.sofa.registry.server.session.node.service;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.shared.constant.MetaLeaderLearnModeEnum;
import com.alipay.sofa.registry.server.shared.meta.AbstractMetaLeaderExchanger;
import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Mar 15, 2021
 */
public class SessionMetaServerManager extends AbstractMetaLeaderExchanger {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMetaLeaderExchanger.class);

  @Autowired private SessionServerConfig sessionServerConfig;

  @Resource(name = "metaClientHandlers")
  private Collection<ChannelHandler> metaClientHandlers;

  public SessionMetaServerManager() {
    super(Exchange.META_SERVER_TYPE, LOGGER);
  }

  @Override
  protected MetaLeaderLearnModeEnum getMode() {
    if (defaultCommonConfig.isJdbc()) {
      return MetaLeaderLearnModeEnum.JDBC;
    } else {
      return MetaLeaderLearnModeEnum.LOADBALANCER;
    }
  }

  @Override
  protected Collection<String> getMetaServerDomains(String dataCenter) {
    return sessionServerConfig.getMetaServerAddresses();
  }

  @Override
  public int getRpcTimeoutMillis() {
    return sessionServerConfig.getMetaNodeExchangeTimeoutMillis();
  }

  @Override
  public int getServerPort() {
    return sessionServerConfig.getMetaServerPort();
  }

  @Override
  protected Collection<ChannelHandler> getClientHandlers() {
    return metaClientHandlers;
  }

  @VisibleForTesting
  void setSessionServerConfig(SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
  }
}
