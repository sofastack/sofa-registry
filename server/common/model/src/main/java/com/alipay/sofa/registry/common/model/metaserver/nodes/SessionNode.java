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

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.store.URL;

/**
 * @author shangyu.wh
 * @version $Id: SessionNodeRegister.java, v 0.1 2018-01-11 16:32 shangyu.wh Exp $
 */
public class SessionNode extends AbstractNode {

  private final ProcessId processId;

  /**
   * constructor
   *
   * @param nodeUrl nodeUrl
   * @param regionId regionId
   * @param processId processId
   */
  public SessionNode(URL nodeUrl, String regionId, ProcessId processId) {
    super(null, nodeUrl, regionId);
    this.processId = processId;
  }

  @Override
  public NodeType getNodeType() {
    return NodeType.SESSION;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SessionNode)) {
      return false;
    }

    SessionNode that = (SessionNode) o;
    return equal(that);
  }

  public ProcessId getProcessId() {
    return processId;
  }

  /**
   * Hash code int.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return super.hash();
  }
}
