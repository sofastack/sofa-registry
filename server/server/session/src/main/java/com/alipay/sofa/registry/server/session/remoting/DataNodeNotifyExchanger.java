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
package com.alipay.sofa.registry.server.session.remoting;

import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * use for dataNode notify
 *
 * @author yuzhi.lyz
 */
public class DataNodeNotifyExchanger extends ClientSideExchanger {
  @Autowired private SessionServerConfig sessionServerConfig;

  @Resource(name = "dataNotifyClientHandlers")
  private Collection<ChannelHandler> dataNotifyClientHandlers;

  public DataNodeNotifyExchanger() {
    super(Exchange.DATA_SERVER_NOTIFY_TYPE);
  }

  /** @see DataNodeNotifyExchanger#request(Request) */
  @Override
  public Response request(Request request) throws RequestException {
    // dataNode notify unsupported request to data server
    throw new UnsupportedOperationException();
  }

  @Override
  public Client connectServer() {
    Set<String> ips = serverIps;
    int count = tryConnectAllServer(ips);
    if (count != ips.size()) {
      throw new RuntimeException("failed to connect all dataServers: " + ips);
    }
    return getClient();
  }

  @Override
  protected Collection<ChannelHandler> getClientHandlers() {
    return dataNotifyClientHandlers;
  }

  @Override
  public int getRpcTimeoutMillis() {
    return sessionServerConfig.getDataNodeExchangeTimeoutMillis();
  }

  @Override
  public int getServerPort() {
    return sessionServerConfig.getDataServerNotifyPort();
  }

  @Override
  public int getConnNum() {
    return sessionServerConfig.getDataNotifyClientConnNum();
  }
}
