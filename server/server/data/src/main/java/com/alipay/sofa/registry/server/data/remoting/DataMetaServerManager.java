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
package com.alipay.sofa.registry.server.data.remoting;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
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
public class DataMetaServerManager extends AbstractMetaLeaderExchanger {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMetaLeaderExchanger.class);

  @Autowired private DataServerConfig dataServerConfig;

  @Resource(name = "metaClientHandlers")
  private Collection<ChannelHandler> metaClientHandlers;

  public DataMetaServerManager() {
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
    return dataServerConfig.getMetaServerAddresses();
  }

  @Override
  public int getRpcTimeoutMillis() {
    return dataServerConfig.getRpcTimeoutMillis();
  }

  @Override
  public int getServerPort() {
    return dataServerConfig.getMetaServerPort();
  }

  @Override
  protected Collection<ChannelHandler> getClientHandlers() {
    return metaClientHandlers;
  }

  @VisibleForTesting
  void setDataServerConfig(DataServerConfig dataServerConfig) {
    this.dataServerConfig = dataServerConfig;
  }
}
