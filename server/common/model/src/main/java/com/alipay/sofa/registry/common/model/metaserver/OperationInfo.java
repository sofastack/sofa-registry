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

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.google.common.base.Objects;

public class OperationInfo {
  private NodeType nodeType;

  private String cell;

  private String address;

  private long operateTs;

  public OperationInfo() {}

  public OperationInfo(NodeType nodeType, String cell, String address, long operateTs) {
    this.nodeType = nodeType;
    this.cell = cell;
    this.address = address;
    this.operateTs = operateTs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationInfo that = (OperationInfo) o;
    return nodeType == that.nodeType
        && Objects.equal(cell, that.cell)
        && Objects.equal(address, that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(nodeType, cell, address);
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
   * Getter method for property <tt>cell</tt>.
   *
   * @return property value of cell
   */
  public String getCell() {
    return cell;
  }

  /**
   * Getter method for property <tt>address</tt>.
   *
   * @return property value of address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Getter method for property <tt>operateTs</tt>.
   *
   * @return property value of operateTs
   */
  public long getOperateTs() {
    return operateTs;
  }

  /**
   * Setter method for property <tt>nodeType</tt>.
   *
   * @param nodeType value to be assigned to property nodeType
   */
  public void setNodeType(NodeType nodeType) {
    this.nodeType = nodeType;
  }

  /**
   * Setter method for property <tt>cell</tt>.
   *
   * @param cell value to be assigned to property cell
   */
  public void setCell(String cell) {
    this.cell = cell;
  }

  /**
   * Setter method for property <tt>address</tt>.
   *
   * @param address value to be assigned to property address
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Setter method for property <tt>operateTs</tt>.
   *
   * @param operateTs value to be assigned to property operateTs
   */
  public void setOperateTs(long operateTs) {
    this.operateTs = operateTs;
  }

  @Override
  public String toString() {
    return "OperationInfo{"
        + "nodeType="
        + nodeType
        + ", cell='"
        + cell
        + '\''
        + ", address='"
        + address
        + '\''
        + ", operateTs="
        + operateTs
        + '}';
  }
}
