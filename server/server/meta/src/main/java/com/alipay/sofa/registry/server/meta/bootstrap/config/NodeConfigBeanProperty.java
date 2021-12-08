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
import org.springframework.beans.factory.annotation.Value;

/**
 * @author shangyu.wh
 * @version $Id: NodeConfigBeanProperty.java, v 0.1 2018-05-03 16:21 shangyu.wh Exp $
 */
public class NodeConfigBeanProperty extends AbstractNodeConfigBean {

  @Value(
      "#{PropertySplitter.mapOfKeyList('${nodes.localDataCenter:DefaultDataCenter}', '${nodes.metaNode:DefaultDataCenter:localhost}')}")
  private Map<String /*dataCenterId*/, Collection<String>> metaNode;

  @Value("${nodes.localDataCenter:DefaultDataCenter}")
  private String localDataCenter;

  @Override
  public Map<String, Collection<String>> getMetaNode() {
    return metaNode;
  }

  /**
   * Getter method for property <tt>localDataCenter</tt>.
   *
   * @return property value of localDataCenter
   */
  @Override
  public String getLocalDataCenter() {
    return localDataCenter;
  }
}
