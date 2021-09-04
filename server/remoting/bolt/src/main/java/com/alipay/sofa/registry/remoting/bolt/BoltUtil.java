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

import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.Url;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelConnectException;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.ChannelOverflowException;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.util.StringFormatter;
import java.util.List;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public final class BoltUtil {
  private BoltUtil() {}

  public static Byte getBoltCustomSerializer(Channel channel) {
    if (channel instanceof BoltChannel) {
      BoltChannel boltChannel = (BoltChannel) channel;

      // set client custom codec for request command if not null
      Object clientCustomCodec = boltChannel.getConnAttribute(InvokeContext.BOLT_CUSTOM_SERIALIZER);
      if (null != clientCustomCodec) {
        try {
          return (Byte) clientCustomCodec;
        } catch (ClassCastException e) {
          throw new IllegalArgumentException(
              "Illegal custom codec ["
                  + clientCustomCodec
                  + "], the type of value should be [byte], but now is ["
                  + clientCustomCodec.getClass().getName()
                  + "].");
        }
      }
    }
    return null;
  }

  public static RuntimeException handleException(
      String role, Object target, Throwable e, String op) {
    if (e instanceof RemotingException) {
      final String format =
          StringFormatter.format("{} {} RemotingException! target url:{}", role, op, target);
      final String msg = e.getMessage();
      // see RpcClientRemoting.connectionManager.check
      if (msg != null) {
        if (msg.contains("write overflow")) {
          return new ChannelOverflowException(format, e);
        }
        if (msg.contains("Connection is null when do check")
            || msg.contains("Check connection failed for address")) {
          return new ChannelConnectException(format, e);
        }
      }
      return new RuntimeException(format, e);
    }
    if (e instanceof InterruptedException) {
      String msg =
          StringFormatter.format("{} {} InterruptedException! target url:{}", role, op, target);
      return new RuntimeException(msg, e);
    }
    String msg = StringFormatter.format("{} {} Exception! target url:{}", role, op, target);
    return new RuntimeException(msg, e);
  }

  public static void checkChannelConnected(Channel channel) {
    if (channel == null) {
      throw new RequestException("channel is null");
    }
    if (!channel.isConnected()) {
      throw new RequestChannelClosedException("channel is not connect:" + channel);
    }
  }

  public static Url createTargetUrl(Channel channel) {
    return new Url(
        channel.getRemoteAddress().getAddress().getHostAddress(),
        channel.getRemoteAddress().getPort());
  }

  public static ChannelHandler getListenerHandlers(List<ChannelHandler> channelHandlers) {
    ChannelHandler connectionEventHandler = null;
    for (ChannelHandler channelHandler : channelHandlers) {
      if (ChannelHandler.HandlerType.LISTENER.equals(channelHandler.getType())) {
        if (connectionEventHandler != null) {
          throw new IllegalArgumentException(
              StringFormatter.format(
                  "only support one listener handler, {} conflict {}",
                  connectionEventHandler,
                  channelHandler));
        }
        connectionEventHandler = channelHandler;
      }
    }
    return connectionEventHandler;
  }
}
