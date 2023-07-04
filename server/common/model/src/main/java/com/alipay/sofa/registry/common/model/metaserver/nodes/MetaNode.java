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

/**
 * @author shangyu.wh
 * @version $Id: MetaNode.java, v 0.1 2018-03-02 16:42 shangyu.wh Exp $
 */
public class MetaNode extends AbstractNode {

  /**
   * constructor
   *
   * @param nodeUrl nodeUrl
   * @param dataCenter dataCenter
   */
  public MetaNode(URL nodeUrl, String dataCenter) {
    super(dataCenter, nodeUrl, null);
  }

  @Override
  public NodeType getNodeType() {
    return NodeType.META;
  }

  /**
   * Getter method for property <tt>dataCenter</tt>.
   *
   * @return property value of dataCenter
   */
  public String getDataCenter() {
    return dataCenter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MetaNode that = (MetaNode) o;
    return super.equal(that);
  }

  @Override
  public int hashCode() {
    return super.hash();
  }
}
