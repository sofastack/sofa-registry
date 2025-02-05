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
package com.alipay.sofa.registry.server.session.node.service;

import com.alipay.sofa.registry.common.model.ClientOffPublishers;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.MultiSubDatum;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.remoting.exchange.ExchangeCallback;
import java.util.Map;
import java.util.Set;

/**
 * @author shangyu.wh
 * @version $Id: NodeService.java, v 0.1 2017-11-28 11:09 shangyu.wh Exp $
 */
public interface DataNodeService {

  /**
   * new publisher data transform to data server
   *
   * @param publisher publisher
   */
  void register(Publisher publisher);

  /**
   * remove publisher data from data server
   *
   * @param publisher publisher
   */
  void unregister(Publisher publisher);

  /**
   * session server support api to stop some client node,all register data on data server will be
   * removed data on session server will be remove too
   *
   * @param clientOffPublishers clientOffPublishers
   */
  void clientOff(ClientOffPublishers clientOffPublishers);

  void fetchDataVersion(
      String dataCenter,
      int slotId,
      Map<String, DatumVersion> interests,
      ExchangeCallback<Map<String /*datainfoid*/, DatumVersion>> callback);
  /**
   * fetch one dataCenter publisher data from data server
   *
   * @param dataInfoId dataInfoId
   * @param dataCenters dataCenters
   * @return MultiSubDatum
   */
  MultiSubDatum fetch(String dataInfoId, Set<String> dataCenters);
}
