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
package com.alipay.sofa.registry.server.session.registry;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.remoting.Channel;
import java.util.List;

/**
 * @author shangyu.wh
 * @version $Id: SessionRegistry.java, v 0.1 2017-11-27 19:49 shangyu.wh Exp $
 */
public interface Registry {

  /**
   * register new publisher or subscriber data
   *
   * @param data data
   * @param channel channel
   */
  void register(StoreData<String> data, Channel channel);

  /**
   * clean all the connectIds: 1. clean pubs/sub/wat
   *
   * @param connectIds connectIds
   */
  void clean(List<ConnectId> connectIds);

  /**
   * client off the connectIds: 1. clean pubs, keep sub/wat; 2. check sub
   *
   * @param connectIds connectIds
   */
  void clientOff(List<ConnectId> connectIds);

  /**
   * disable the connectIds: 1. clean pub/sub/wat; 2. check sub
   *
   * @param connectIds connectIds
   */
  void blacklist(List<ConnectId> connectIds);

  /**
   * message mode com.alipay.sofa.registry.client.provider for client node to unregister single
   * subscriber or publisher data
   *
   * @param data data
   */
  void unRegister(StoreData<String> data);

  /** for scheduler clean no connect client */
  void cleanClientConnect();
}
