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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.MDC;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-14 13:57 yuzhi.lyz Exp $
 */
public abstract class AbstractChannelHandler<T> implements ChannelHandler<T> {
  private final Logger connectLog;
  protected final Logger exchangeLog;

  protected AbstractChannelHandler(Logger connectLog, Logger exchangeLog) {
    this.connectLog = connectLog;
    this.exchangeLog = exchangeLog;
  }

  @Override
  public void connected(Channel channel) {
    connectLog.info("{} node connected, channel {}", getConnectNodeType(), channel);
  }

  @Override
  public void disconnected(Channel channel) {
    connectLog.info("{} node disconnected, channel {}", getConnectNodeType(), channel);
  }

  protected abstract Node.NodeType getConnectNodeType();

  @Override
  public void caught(Channel channel, T message, Throwable exception) {
    exchangeLog.safeError(
        "{} caughtException, channel {}, msg={}",
        getConnectNodeType(),
        channel,
        message,
        (Throwable) exception);
  }

  @Override
  public void received(Channel channel, T message) {
    // only support as async
    throw new UnsupportedOperationException();
  }

  @Override
  public Object reply(Channel channel, T request) {
    final String address = RemotingHelper.getRemoteHostAddress(channel);
    try {
      MDC.startTraceRequest(address);
      logRequest(channel, request);
      checkParam(request);
      return doHandle(channel, request);
    } catch (Throwable e) {
      exchangeLog.safeError("[{}] handle request failed", getClassName(), e);
      return buildFailedResponse(e.getMessage());
    } finally {
      MDC.finishTraceRequest();
    }
  }

  /**
   * check params if valid
   *
   * @param request
   */
  public void checkParam(T request) {}

  /**
   * execute
   *
   * @param request
   * @return
   */
  public abstract Object doHandle(Channel channel, T request);

  /**
   * build failed response
   *
   * @param msg
   * @return
   */
  public Object buildFailedResponse(String msg) {
    throw new RuntimeException(msg);
  }

  /**
   * print request
   *
   * @param request
   */
  protected void logRequest(Channel channel, T request) {
    if (exchangeLog.isInfoEnabled()) {
      StringBuilder sb = new StringBuilder(256);
      sb.append("[").append(getClassName()).append("] ");
      sb.append("Remote:")
          .append(RemotingHelper.getChannelRemoteAddress(channel))
          .append(" Request:")
          .append(request);
      exchangeLog.info(sb.toString());
    }
  }

  /**
   * get simple name of this class
   *
   * @return
   */
  private String getClassName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public HandlerType getType() {
    return HandlerType.PROCESSER;
  }
}
