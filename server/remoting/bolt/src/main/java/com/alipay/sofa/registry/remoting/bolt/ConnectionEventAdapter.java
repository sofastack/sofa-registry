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
package com.alipay.sofa.registry.remoting.bolt;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.util.StringFormatter;

/**
 * The type Connection event adapter.
 *
 * @author shangyu.wh
 * @version $Id : ConnectionEventAdapter.java, v 0.1 2017-11-22 21:01 shangyu.wh Exp $
 */
public class ConnectionEventAdapter implements ConnectionEventProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionEventAdapter.class);

  private final ConnectionEventType connectionEventType;

  private final ChannelHandler connectionEventHandler;

  /**
   * Instantiates a new Connection event adapter.
   *
   * @param connectionEventType the connection event type
   * @param connectionEventHandler the connection event handler
   */
  public ConnectionEventAdapter(
      ConnectionEventType connectionEventType, ChannelHandler connectionEventHandler) {
    this.connectionEventType = connectionEventType;
    // the connect event handler maybe is null
    this.connectionEventHandler = connectionEventHandler;
  }

  /** @see ConnectionEventProcessor#onEvent(String, Connection) */
  @Override
  public void onEvent(String remoteAddr, Connection conn) {
    try {
      switch (connectionEventType) {
        case CONNECT:
          if (connectionEventHandler != null) {
            connectionEventHandler.connected(new BoltChannel(conn));
          }
          LOGGER.info("[connect]local={},remote={}", conn.getLocalPort(), remoteAddr);
          break;

        case CLOSE:
          if (connectionEventHandler != null) {
            connectionEventHandler.disconnected(new BoltChannel(conn));
          }
          LOGGER.info("[close]local={},remote={}", conn.getLocalPort(), remoteAddr);
          break;

        case EXCEPTION:
          if (connectionEventHandler != null) {
            connectionEventHandler.caught(new BoltChannel(conn), null, null);
          }
          LOGGER.error("[exception]local={},remote={}", conn.getLocalPort(), remoteAddr);
          break;
        case CONNECT_FAILED:
          LOGGER.error("[connectFailed]local={},remote={}", conn.getLocalPort(), remoteAddr);
          break;
        default:
          break;
      }
    } catch (Throwable e) {
      String err =
          StringFormatter.format(
              "failed to process connection, type={}, local={}, remote={}, conn={}",
              connectionEventType,
              conn.getLocalPort(),
              remoteAddr,
              conn);
      LOGGER.error(err, e);
      throw new RuntimeException(err, e);
    }
  }
}
