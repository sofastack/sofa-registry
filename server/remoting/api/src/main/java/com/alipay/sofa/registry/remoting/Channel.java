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
