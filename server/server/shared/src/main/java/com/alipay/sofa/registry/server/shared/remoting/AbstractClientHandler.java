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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

/**
 * @author shangyu.wh
 * @version $Id: ClientHandler.java, v 0.1 2017-11-28 18:06 shangyu.wh Exp $
 */
public abstract class AbstractClientHandler<T> extends AbstractChannelHandler<T> {

  private static final Logger LOGGER_EXCHANGE = LoggerFactory.getLogger("CLI-EXCHANGE");
  private static final Logger LOGGER_CONNECT = LoggerFactory.getLogger("CLI-CONNECT");

  public AbstractClientHandler() {
    super(LOGGER_CONNECT, LOGGER_EXCHANGE);
  }
}
