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
package com.alipay.sofa.registry.common.model.metaserver.nodes;

import com.alipay.sofa.registry.common.model.store.URL;
import java.util.Objects;

/**
 * @author shangyu.wh
 * @version $Id: DataNode.java, v 0.1 2018-01-18 18:06 shangyu.wh Exp $
 */
public class DataNode extends AbstractNode {

  private long registrationTimestamp;

  /**
   * constructor
   *
   * @param nodeUrl nodeUrl
   * @param dataCenter dataCenter
   */
  public DataNode(URL nodeUrl, String dataCenter) {
    super(dataCenter, nodeUrl, null);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DataNode)) {
      return false;
    }

    DataNode that = (DataNode) o;
    return equal(that) && registrationTimestamp == that.registrationTimestamp;
  }

  /**
   * Hash code int.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(hash(), registrationTimestamp);
  }

  @Override
  public NodeType getNodeType() {
    return NodeType.DATA;
  }

  /**
   * Getter method for property <tt>registrationTimestamp</tt>.
   *
   * @return property value of registrationTimestamp
   */
  public long getRegistrationTimestamp() {
    return registrationTimestamp;
  }

  /**
   * Setter method for property <tt>registrationTimestamp</tt>.
   *
   * @param registrationTimestamp value to be assigned to property registrationTimestamp
   */
  public void setRegistrationTimestamp(long registrationTimestamp) {
    this.registrationTimestamp = registrationTimestamp;
  }
}
