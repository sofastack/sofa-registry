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

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import java.util.concurrent.Executor;

/**
 * @author shangyu.wh
 * @version $Id: UserProcessorAdapter.java, v 0.1 2017-11-24 17:10 shangyu.wh Exp $
 */
//bolt的rpc，处理协议的handler只需要继承SyncUserProcessor即可
//  SyncUserProcessorAdapter将实现委托给ChannelHandler
//  为什么这样做，其实还是为了扩展SyncUserProcessor。以及更好的设计代码。
public class SyncUserProcessorAdapter extends SyncUserProcessor {

  private final ChannelHandler userProcessorHandler;

  /**
   * constructor
   *
   * @param userProcessorHandler
   */
  public SyncUserProcessorAdapter(ChannelHandler userProcessorHandler) {
    this.userProcessorHandler = userProcessorHandler;
  }

  @Override
  public Object handleRequest(BizContext bizCtx, Object request) throws Exception {
    BoltChannel boltChannel = new BoltChannel(bizCtx.getConnection());
    return userProcessorHandler.reply(boltChannel, request);
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
