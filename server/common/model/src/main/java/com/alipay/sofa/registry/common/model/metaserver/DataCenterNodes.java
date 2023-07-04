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
package com.alipay.sofa.registry.common.model.metaserver;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import java.io.Serializable;
import java.util.Map;

/**
 * @author shangyu.wh
 * @version $Id: DataCenterNodes.java, v 0.1 2018-02-12 11:06 shangyu.wh Exp $
 */
public class DataCenterNodes<T extends Node> implements Serializable {

  private final NodeType nodeType;

  private final long version;

  private final String dataCenterId;

  private Map<String /*ipAddress*/, T> nodes;

  /**
   * constructor
   *
   * @param nodeType nodeType
   * @param version version
   * @param dataCenterId dataCenterId
   */
  public DataCenterNodes(NodeType nodeType, long version, String dataCenterId) {
    this.nodeType = nodeType;
    this.version = version;
    this.dataCenterId = dataCenterId;
  }

  /**
   * Getter method for property <tt>version</tt>.
   *
   * @return property value of version
   */
  public long getVersion() {
    return version;
  }

  /**
   * Getter method for property <tt>dataCenterId</tt>.
   *
   * @return property value of dataCenterId
   */
  public String getDataCenterId() {
    return dataCenterId;
  }

  /**
   * Getter method for property <tt>nodes</tt>.
   *
   * @return property value of nodes
   */
  public Map<String, T> getNodes() {
    return nodes;
  }

  /**
   * Setter method for property <tt>nodes</tt>.
   *
   * @param nodes value to be assigned to property nodes
   */
  public void setNodes(Map<String, T> nodes) {
    this.nodes = nodes;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("DataCenterNodes{");
    sb.append("nodeType=").append(nodeType);
    sb.append(", version=").append(version);
    sb.append(", dataCenterId='").append(dataCenterId).append('\'');
    sb.append(", nodes=").append(nodes);
    sb.append('}');
    return sb.toString();
  }
}
