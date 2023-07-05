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
package com.alipay.sofa.registry.common.model.metaserver.rpc;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import java.io.Serializable;

/**
 * @author shangyu.wh
 * @version $Id: GetChangeListRequest.java, v 0.1 2018-02-12 11:02 shangyu.wh Exp $
 */
public class NodeClusterViewRequest implements Serializable {

  private final NodeType nodeType;

  /** get list data dataCenter */
  private final String dataCenterId;

  /**
   * constructor
   *
   * @param nodeType nodeType
   * @param dataCenterId dataCenterId
   */
  public NodeClusterViewRequest(NodeType nodeType, String dataCenterId) {
    this.nodeType = nodeType;
    this.dataCenterId = dataCenterId;
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
   * Getter method for property <tt>nodeType</tt>.
   *
   * @return property value of nodeType
   */
  public NodeType getNodeType() {
    return nodeType;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("GetChangeListRequest{");
    sb.append("nodeType=").append(nodeType);
    sb.append(", dataCenterId='").append(dataCenterId).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
