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

import java.net.InetSocketAddress;
import javax.ws.rs.client.WebTarget;

/**
 * @author shangyu.wh
 * @version $Id: Channel.java, v 0.1 2017-11-20 20:39 shangyu.wh Exp $
 */
//其基本功能主要是属性相关功能。
  //Channel用于在字节缓冲区和位于通道另一侧的实体（通常是一个文件或套接字）之间有效地传输数据
public interface Channel {

  /**
   * get remote address.
   *
   * @return remote address.
   */
  InetSocketAddress getRemoteAddress();

  /**
   * get local address.
   *
   * @return local address.
   */
  InetSocketAddress getLocalAddress();

  /**
   * is connected.
   *
   * @return connected
   */
  boolean isConnected();

  /**
   * get attribute in context.
   *
   * @param key key.
   * @return value.
   */
  Object getAttribute(String key);

  /**
   * set attribute in context.
   *
   * @param key key.
   * @param value value.
   */
  void setAttribute(String key, Object value);

  /**
   * get attribute in connection.
   *
   * @param key key.
   * @return value.
   */
  Object getConnAttribute(String key);

  /**
   * set attribute in connection.
   *
   * @param key key.
   * @param value value.
   */
  void setConnAttribute(String key, Object value);

  /**
   * for rest api
   *
   * @return
   */
  WebTarget getWebTarget();

  void close();
}
