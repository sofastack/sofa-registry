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
package com.alipay.sofa.registry.server.meta.remoting;

import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.shared.remoting.ServerSideExchanger;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: DataNodeExchanger.java, v 0.1 2018-01-23 19:18 shangyu.wh Exp $
 */
public class DataNodeExchanger extends ServerSideExchanger {

  @Autowired private MetaServerConfig metaServerConfig;

  @Override
  public int getRpcTimeoutMillis() {
    return metaServerConfig.getDataNodeExchangeTimeoutMillis();
  }

  @Override
  public int getServerPort() {
    return metaServerConfig.getDataServerPort();
  }

  @VisibleForTesting
  public DataNodeExchanger setMetaServerConfig(MetaServerConfig metaServerConfig) {
    this.metaServerConfig = metaServerConfig;
    return this;
  }

  @VisibleForTesting
  public DataNodeExchanger setBoltExchange(Exchange boltExchange) {
    this.boltExchange = boltExchange;
    return this;
  }
}
