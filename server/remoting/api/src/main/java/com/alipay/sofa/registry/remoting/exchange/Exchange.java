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
package com.alipay.sofa.registry.remoting.exchange;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.Server;

/**
 * @author shangyu.wh
 * @version $Id: Exchanger.java, v 0.1 2017-11-20 21:25 shangyu.wh Exp $
 */
public interface Exchange<T> {
  String SESSION_SERVER_TYPE = "sessionServer";
  String SESSION_SERVER_CONSOLE_TYPE = "sessionServerConsole";
  String DATA_SERVER_TYPE = "dataServer";
  String DATA_SERVER_NOTIFY_TYPE = "dataServerNotify";
  String META_SERVER_TYPE = "metaServer";
  String REMOTE_CLUSTER_META = "remoteMetaServer";
  String REMOTE_DATA_SERVER_TYPE = "remoteDataServer";

  /**
   * connect same type server,one server ip one connection such as different server on data
   * server,serverOne and serverTwo,different type server must match different channelHandlers, so
   * we must connect by serverType,and get Client instance by serverType
   *
   * @param serverType
   * @param serverUrl
   * @param channelHandlers
   * @return
   */
  Client connect(String serverType, URL serverUrl, T... channelHandlers);

  /**
   * connect same type server,one server ip one connection such as different server on data
   * server,serverOne and serverTwo,different type server must match different channelHandlers, so
   * we must connect by serverType,and get Client instance by serverType
   *
   * @param serverType
   * @param connNum connection number per serverUrl
   * @param serverUrl
   * @param channelHandlers
   * @return
   */
  Client connect(String serverType, int connNum, URL serverUrl, T... channelHandlers);

  /**
   * bind server by server port in url parameter,one port must by same server type
   *
   * @param url
   * @param channelHandlers
   * @return
   */
  Server open(URL url, T... channelHandlers);

  Server open(URL url, int lowWaterMark, int highWaterMark, T... channelHandlers);

  /**
   * * get Client instance by serverType,very client instance has different channels match different
   * server ip
   *
   * @param serverType
   * @return
   */
  Client getClient(String serverType);

  /**
   * get server instance by port
   *
   * @param port
   * @return
   */
  Server getServer(Integer port);
}
