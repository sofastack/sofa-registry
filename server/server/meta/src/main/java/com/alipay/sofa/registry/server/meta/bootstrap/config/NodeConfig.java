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
package com.alipay.sofa.registry.server.meta.bootstrap.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author shangyu.wh
 * @version $Id: NodeConfig.java, v 0.1 2018-01-23 15:00 shangyu.wh Exp $
 */
public interface NodeConfig {

  /**
   * get other metaServer node
   *
   * @return Map
   */
  Map<String, Collection<String>> getMetaNode();

  /**
   * get other metaServer node ip
   *
   * @return Map
   */
  Map<String, Collection<String>> getMetaNodeIP();

  /**
   * local data Center id
   *
   * @return String
   */
  String getLocalDataCenter();

  /**
   * get dataCenter by meta node ipAddress
   *
   * @param metaIpAddress metaIpAddress
   * @return String
   */
  String getMetaDataCenter(String metaIpAddress);

  /**
   * get datacenter meta servers
   *
   * @param dataCenter dataCenter
   * @return Set
   */
  Set<String> getDataCenterMetaServers(String dataCenter);
}
