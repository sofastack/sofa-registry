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
 * @version $Id: NodeChangeRequest.java, v 0.1 2018-01-11 16:55 shangyu.wh Exp $
 */
public class NodeChangeResult<T extends Node> implements Serializable {

  private final NodeType nodeType;

  private Map<String /*dataCenter id*/, Map<String /*ipAddress*/, T>> nodes;

  private Long version;

  private Map<String /*dataCenter*/, Long /*version*/> dataCenterListVersions;

  /** local dataCenter id */
  private String localDataCenter;

  /**
   * constructor
   *
   * @param nodeType nodeType
   */
  public NodeChangeResult(NodeType nodeType) {
    this.nodeType = nodeType;
  }

  /**
   * Getter method for property <tt>nodeType</tt>.
   *
   * @return property value of nodeType
   */
  public NodeType getNodeType() {
    return nodeType;
  }

  /**
   * Getter method for property <tt>nodes</tt>.
   *
   * @return property value of nodes
   */
  public Map<String, Map<String, T>> getNodes() {
    return nodes;
  }

  /**
   * Setter method for property <tt>nodes</tt>.
   *
   * @param nodes value to be assigned to property nodes
   */
  public void setNodes(Map<String, Map<String, T>> nodes) {
    this.nodes = nodes;
  }

  /**
   * Getter method for property <tt>localDataCenter</tt>.
   *
   * @return property value of localDataCenter
   */
  public String getLocalDataCenter() {
    return localDataCenter;
  }

  /**
   * Setter method for property <tt>localDataCenter</tt>.
   *
   * @param localDataCenter value to be assigned to property localDataCenter
   */
  public void setLocalDataCenter(String localDataCenter) {
    this.localDataCenter = localDataCenter;
  }

  /**
   * Getter method for property <tt>version</tt>.
   *
   * @return property value of version
   */
  public Long getVersion() {
    return version;
  }

  /**
   * Setter method for property <tt>version</tt>.
   *
   * @param version value to be assigned to property version
   */
  public void setVersion(Long version) {
    this.version = version;
  }

  /**
   * Getter method for property <tt>dataCenterListVersions</tt>.
   *
   * @return property value of dataCenterListVersions
   */
  public Map<String, Long> getDataCenterListVersions() {
    return dataCenterListVersions;
  }

  /**
   * Setter method for property <tt>dataCenterListVersions</tt>.
   *
   * @param dataCenterListVersions value to be assigned to property dataCenterListVersions
   */
  public void setDataCenterListVersions(Map<String, Long> dataCenterListVersions) {
    this.dataCenterListVersions = dataCenterListVersions;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("NodeChangeResult{");
    sb.append("nodeType=").append(nodeType);
    sb.append(", nodes=").append(nodes);
    sb.append(", version=").append(version);
    sb.append(", dataCenterListVersions=").append(dataCenterListVersions);
    sb.append(", localDataCenter='").append(localDataCenter).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
