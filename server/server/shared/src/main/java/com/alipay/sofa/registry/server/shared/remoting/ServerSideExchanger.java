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
package com.alipay.sofa.registry.server.shared.remoting;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.util.CollectionUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-18 11:40 yuzhi.lyz Exp $
 */
public abstract class ServerSideExchanger implements NodeExchanger {

  @Autowired protected Exchange boltExchange;

  @Override
  public Response request(Request request) throws RequestException {
    final URL url = request.getRequestUrl();
    if (url == null) {
      throw new RequestException("null url", request);
    }
    return request(url, request);
  }

  public Response request(URL url, Request request) throws RequestException {
    final Server server = boltExchange.getServer(getServerPort());
    if (server == null) {
      throw new RequestException("no server for " + url + "," + getServerPort(), request);
    }
    final int timeout = request.getTimeout() != null ? request.getTimeout() : getRpcTimeoutMillis();
    Channel channel = null;
    if (url == null) {
      // now use in dsr console sync case
      channel = chooseChannel(server);
    } else {
      channel = server.getChannel(url);
    }

    if (channel == null || !channel.isConnected()) {
      throw new RequestChannelClosedException(
          getServerPort() + ", channel may be closed, " + url, request);
    }
    if (request.getCallBackHandler() != null) {
      server.sendCallback(channel, request.getRequestBody(), request.getCallBackHandler(), timeout);
      return () -> Response.ResultStatus.SUCCESSFUL;
    } else {
      final Object result = server.sendSync(channel, request.getRequestBody(), timeout);
      return () -> result;
    }
  }

  private Channel chooseChannel(Server server) {
    List<Channel> channels = server.getChannels();
    return CollectionUtils.getRandom(channels);
  }

  @Override
  public Client connectServer() {
    throw new UnsupportedOperationException();
  }

  public abstract int getRpcTimeoutMillis();

  public abstract int getServerPort();
}
