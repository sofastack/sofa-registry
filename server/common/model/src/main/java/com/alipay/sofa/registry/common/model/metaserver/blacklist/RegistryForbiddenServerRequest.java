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
package com.alipay.sofa.registry.common.model.metaserver.blacklist;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataOperation;
import com.alipay.sofa.registry.common.model.metaserver.NodeServerOperateInfo;
import java.io.Serializable;

/**
 * @author chen.zhu
 *     <p>Mar 18, 2021
 */
public class RegistryForbiddenServerRequest implements Serializable {

  private final DataOperation operation;

  private final NodeType nodeType;

  private final String ip;

  private final String cell;

  private NodeServerOperateInfo operateInfo;

  /**
   * @param operation operation
   * @param nodeType nodeType
   * @param ip ip
   */
  public RegistryForbiddenServerRequest(
      DataOperation operation, NodeType nodeType, String ip, String cell) {
    this.operation = operation;
    this.nodeType = nodeType;
    this.ip = ip;
    this.cell = cell;
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
   * Gets get operation.
   *
   * @return the get operation
   */
  public DataOperation getOperation() {
    return operation;
  }

  /**
   * Gets get ip.
   *
   * @return the get ip
   */
  public String getIp() {
    return ip;
  }

  /**
   * Getter method for property <tt>cell</tt>.
   *
   * @return property value of cell
   */
  public String getCell() {
    return cell;
  }

  /**
   * Getter method for property <tt>operateInfo</tt>.
   *
   * @return property value of operateInfo
   */
  public NodeServerOperateInfo getOperateInfo() {
    return operateInfo;
  }

  /**
   * Setter method for property <tt>operateInfo</tt>.
   *
   * @param operateInfo value to be assigned to property operateInfo
   */
  public void setOperateInfo(NodeServerOperateInfo operateInfo) {
    this.operateInfo = operateInfo;
  }

  @Override
  public String toString() {
    return "RegistryForbiddenServerRequest{"
        + "operation="
        + operation
        + ", nodeType="
        + nodeType
        + ", ip='"
        + ip
        + '\''
        + ", cell='"
        + cell
        + '\''
        + ", operateInfo="
        + operateInfo
        + '}';
  }
}
