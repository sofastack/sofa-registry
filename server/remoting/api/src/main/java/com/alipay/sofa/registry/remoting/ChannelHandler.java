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

import java.util.concurrent.Executor;

/**
 * @author shangyu.wh
 * @version $Id: ChannelHandler.java, v 0.1 2017-11-20 20:45 shangyu.wh Exp $
 */
public interface ChannelHandler<T> {

  /** The enum Handler type. */
  enum HandlerType {
    LISTENER,
    PROCESSER
  }

  /** The enum Invoke type. */
  enum InvokeType {
    SYNC,
    ASYNC
  }

  /**
   * on channel connected.
   *
   * @param channel
   * @throws RemotingException
   */
  void connected(Channel channel) throws RemotingException;

  /**
   * on channel disconnected.
   *
   * @param channel channel.
   * @throws RemotingException
   */
  void disconnected(Channel channel) throws RemotingException;

  /**
   * on message received.
   *
   * @param channel channel.
   * @param message message.
   * @throws RemotingException
   */
  void received(Channel channel, T message) throws RemotingException;

  /**
   * on message reply.
   *
   * @param channel
   * @param message
   * @return
   * @throws RemotingException
   */
  Object reply(Channel channel, T message) throws RemotingException;

  /**
   * on exception caught.
   *
   * @param channel channel.
   * @param message message.
   * @param exception exception.
   * @throws RemotingException
   */
  void caught(Channel channel, T message, Throwable exception) throws RemotingException;

  /**
   * check handlerType
   *
   * @return
   */
  HandlerType getType();

  /**
   * return processor request class name
   *
   * @return
   */
  Class interest();

  /**
   * Select Sync process by reply or Async process by received
   *
   * @return
   */
  default InvokeType getInvokeType() {
    return InvokeType.SYNC;
  }

  /**
   * specify executor for processor handler
   *
   * @return
   */
  default Executor getExecutor() {
    return null;
  }
}
