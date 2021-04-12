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

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import java.util.concurrent.Executor;

/**
 * @author shangyu.wh
 * @version $Id: AsyncUserProcessorAdapter.java, v 0.1 2018-01-18 21:24 shangyu.wh Exp $
 */
public class AsyncUserProcessorAdapter extends AsyncUserProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncUserProcessorAdapter.class);

  private final ChannelHandler userProcessorHandler;

  /**
   * constructor
   *
   * @param userProcessorHandler
   */
  public AsyncUserProcessorAdapter(ChannelHandler userProcessorHandler) {
    this.userProcessorHandler = userProcessorHandler;
  }

  @Override
  public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, Object request) {
    try {
      BoltChannel boltChannel = new BoltChannel(bizCtx.getConnection());
      boltChannel.setAsyncContext(asyncCtx);
      userProcessorHandler.received(boltChannel, request);
    } catch (Throwable e) {
      LOGGER.error("Handle request error!", e);
      throw new RuntimeException("Handle request error!", e);
    }
  }

  @Override
  public String interest() {
    if (userProcessorHandler.interest() != null) {
      return userProcessorHandler.interest().getName();
    }

    return null;
  }

  @Override
  public Executor getExecutor() {
    return userProcessorHandler.getExecutor();
  }
}
