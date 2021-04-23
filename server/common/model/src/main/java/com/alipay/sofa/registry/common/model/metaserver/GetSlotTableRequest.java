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

import java.io.Serializable;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-11 11:16 yuzhi.lyz Exp $
 */
public class GetSlotTableRequest implements Serializable {
  private static final long serialVersionUID = 2478663577413212315L;
  private final long epochOfNode;
  // session node not care the followers
  private final boolean ignoredFollowers;
  // data node only care self, if not set, get all
  private final String targetDataNode;

  public GetSlotTableRequest(long epochOfNode, String targetDataNode, boolean ignoredFollowers) {
    this.epochOfNode = epochOfNode;
    this.targetDataNode = targetDataNode;
    this.ignoredFollowers = ignoredFollowers;
  }

  /**
   * Getter method for property <tt>epochOfNode</tt>.
   *
   * @return property value of epochOfNode
   */
  public long getEpochOfNode() {
    return epochOfNode;
  }

  /**
   * Getter method for property <tt>targetDataNode</tt>.
   *
   * @return property value of targetDataNode
   */
  public String getTargetDataNode() {
    return targetDataNode;
  }

  public boolean isIgnoredFollowers() {
    return ignoredFollowers;
  }

  @Override
  public String toString() {
    return "GetSlotTableRequest{"
        + "epochOfNode="
        + epochOfNode
        + ", ignoredFollowers="
        + ignoredFollowers
        + ", targetDataNode='"
        + targetDataNode
        + '\''
        + '}';
  }
}
