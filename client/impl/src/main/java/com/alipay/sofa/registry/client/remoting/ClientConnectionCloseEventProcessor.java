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
package com.alipay.sofa.registry.client.remoting;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import com.alipay.sofa.registry.client.log.LoggerFactory;
import org.slf4j.Logger;

/**
 * The type Client connection close event processor.
 *
 * @author zhuoyu.sjw
 * @version $Id : ClientConnectionCloseEventProcessor.java, v 0.1 2018-02-26 20:28 zhuoyu.sjw Exp $$
 */
public class ClientConnectionCloseEventProcessor implements ConnectionEventProcessor {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ClientConnectionCloseEventProcessor.class);

  /**
   * On event.
   *
   * @param remoteAddr the remote addr
   * @param conn the conn
   */
  @Override
  public void onEvent(String remoteAddr, Connection conn) {
    if (null != conn) {
      LOGGER.info(
          "[connection] Client disconnected, remote address: {}, localAddress: {}",
          remoteAddr,
          conn.getLocalAddress());
    }
  }
}
