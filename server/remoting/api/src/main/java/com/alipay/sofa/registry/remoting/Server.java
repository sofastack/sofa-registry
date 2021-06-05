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
package com.alipay.sofa.registry.remoting;

import com.alipay.sofa.registry.common.model.store.URL;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author shangyu.wh
 * @version $Id: Server.java, v 0.1 2017-11-20 21:05 shangyu.wh Exp $
 */
public interface Server extends Endpoint {

  /**
   * get Server status
   *
   * @return
   */
  boolean isOpen();

  /**
   * get channels.
   *
   * @return channels
   */
  List<Channel> getChannels();

  /** select one available channel for every ip */
  Map<String, Channel> selectAvailableChannelsForHostAddress();
  /**
   * get channel.
   *
   * @param remoteAddress
   * @return channel
   */
  /** select all available channels for every ip */
  Map<String, List<Channel>> selectAllAvailableChannelsForHostAddress();

  Channel getChannel(InetSocketAddress remoteAddress);

  /**
   * get channel by url
   *
   * @param url
   * @return channel
   */
  Channel getChannel(URL url);

  /**
   * close the channel.
   *
   * @param channel
   */
  void close(Channel channel);

  int getChannelCount();

  /**
   * send with callback handler
   *
   * @param channel the channel
   * @param message the message
   * @param callbackHandler the callback handler
   * @param timeoutMillis the timeout millis
   */
  void sendCallback(
      final Channel channel,
      final Object message,
      CallbackHandler callbackHandler,
      final int timeoutMillis);

  /**
   * Sync send
   *
   * @param channel the channel
   * @param message the message
   * @param timeoutMillis the timeout millis
   * @return object
   */
  Object sendSync(final Channel channel, final Object message, final int timeoutMillis);
}
