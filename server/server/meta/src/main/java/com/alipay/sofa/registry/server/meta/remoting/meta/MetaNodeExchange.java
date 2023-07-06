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
package com.alipay.sofa.registry.server.meta.remoting.meta;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.remoting.exchange.message.Response.ResultStatus;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: MetaNodeExchange.java, v 0.1 2021年03月29日 16:08 xiaojian.xj Exp $
 */
public class MetaNodeExchange extends ClientSideExchanger {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetaNodeExchange.class);

  @Autowired private MetaServerConfig metaServerConfig;

  @Autowired private MetaLeaderService metaLeaderService;

  protected volatile String metaLeader;

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  public MetaNodeExchange() {
    super(Exchange.META_SERVER_TYPE);
  }

  @Override
  public int getRpcTimeoutMillis() {
    return metaServerConfig.getMetaNodeExchangeTimeoutMillis();
  }

  @Override
  public int getServerPort() {
    return metaServerConfig.getMetaServerPort();
  }

  @Override
  protected Collection<ChannelHandler> getClientHandlers() {
    return Collections.emptyList();
  }

  @Override
  public int getConnNum() {
    return 3;
  }

  public Response sendRequest(Object requestBody) throws RequestException {
    final String newLeader = metaLeaderService.getLeader();
    if (StringUtils.isBlank(newLeader)) {
      LOGGER.error("[sendRequest] meta leader is empty.");
      return () -> ResultStatus.FAILED;
    }

    if (!StringUtils.equals(metaLeader, newLeader) || boltExchange.getClient(serverType) == null) {
      setLeaderAndConnect(newLeader);
    }

    Request request =
        new Request() {
          @Override
          public Object getRequestBody() {
            return requestBody;
          }

          @Override
          public URL getRequestUrl() {
            return new URL(newLeader, getServerPort());
          }
        };
    return request(request);
  }

  private void setLeaderAndConnect(String newLeader) {
    String removed = metaLeader;
    try {
      lock.writeLock().lock();
      metaLeader = newLeader;
    } finally {
      lock.writeLock().unlock();
    }

    try {
      LOGGER.info(
          "[setLeaderAndConnect][reset leader when heartbeat] connect meta leader: {}",
          newLeader,
          removed);
      connect(new URL(newLeader, metaServerConfig.getMetaServerPort()));

    } catch (Throwable th) {
      LOGGER.error("[setLeaderAndConnect]", th);
    }
  }

  /**
   * Getter method for property <tt>metaLeader</tt>.
   *
   * @return property value of metaLeader
   */
  public String getMetaLeader() {
    return metaLeader;
  }
}
