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
package com.alipay.sofa.registry.server.data.multi.cluster.exchanger;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.MultiClusterDataServerConfig;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import java.util.Collection;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : RemoteDataNodeExchanger.java, v 0.1 2022年05月10日 20:30 xiaojian.xj Exp $
 */
public class RemoteDataNodeExchanger extends ClientSideExchanger {

  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteDataNodeExchanger.class);

  @Autowired private MultiClusterDataServerConfig multiClusterDataServerConfig;

  @Resource(name = "remoteDataClientHandlers")
  private Collection<ChannelHandler> remoteDataClientHandlers;

  /**
   * Setter method for property <tt>remoteDataClientHandlers</tt>.
   *
   * @param remoteDataClientHandlers value to be assigned to property remoteDataClientHandlers
   */
  public void setRemoteDataClientHandlers(Collection<ChannelHandler> remoteDataClientHandlers) {
    LOGGER.info(
        "[RemoteDataNodeExchanger]setRemoteDataClientHandlers:{}", remoteDataClientHandlers);
    this.remoteDataClientHandlers = remoteDataClientHandlers;
  }

  public RemoteDataNodeExchanger() {
    super(Exchange.REMOTE_DATA_SERVER_TYPE);
  }

  @Override
  public int getRpcTimeoutMillis() {
    return multiClusterDataServerConfig.getSyncRemoteSlotLeaderTimeoutMillis();
  }

  @Override
  public int getServerPort() {
    return multiClusterDataServerConfig.getSyncRemoteSlotLeaderPort();
  }

  @Override
  public int getConnNum() {
    return multiClusterDataServerConfig.getSyncRemoteSlotLeaderConnNum();
  }

  @Override
  protected Collection<ChannelHandler> getClientHandlers() {
    LOGGER.info("[RemoteDataNodeExchanger]remoteDataClientHandlers:{}", remoteDataClientHandlers);
    return remoteDataClientHandlers;
  }
}
