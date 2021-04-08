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

  private final BoltServer boltServer;

  /**
   * Instantiates a new Connection event adapter.
   *
   * @param connectionEventType the connection event type
   * @param connectionEventHandler the connection event handler
   * @param boltServer the bolt server
   */
  public ConnectionEventAdapter(
      ConnectionEventType connectionEventType,
      ChannelHandler connectionEventHandler,
      BoltServer boltServer) {
    this.connectionEventType = connectionEventType;
    // the connect event handler maybe is null
    this.connectionEventHandler = connectionEventHandler;
    // boltClient, the blotServer is null
    this.boltServer = boltServer;
  }

  /** @see ConnectionEventProcessor#onEvent(String, Connection) */
  @Override
  public void onEvent(String remoteAddr, Connection conn) {
    try {
      if (connectionEventHandler != null) {
        switch (connectionEventType) {
          case CONNECT:
            BoltChannel boltChannel = new BoltChannel(conn);
            if (boltServer != null) {
              boltServer.addChannel(boltChannel);
            }
            connectionEventHandler.connected(boltChannel);
            break;

          case CLOSE:
            BoltChannel boltChannelClose = new BoltChannel(conn);
            if (boltServer != null) {
              boltServer.removeChannel(boltChannelClose);
            }
            connectionEventHandler.disconnected(boltChannelClose);
            break;

          case EXCEPTION:
            BoltChannel boltChannelException = new BoltChannel(conn);
            connectionEventHandler.caught(boltChannelException, null, null);
            break;
          default:
            break;
        }
      }
    } catch (Exception e) {
      LOGGER.error("Connection process " + connectionEventType + " error!", e);
      throw new RuntimeException("Connection process " + connectionEventType + " error!", e);
    }
  }
}
