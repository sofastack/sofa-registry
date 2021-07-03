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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import java.util.Objects;

public abstract class AbstractNode implements Node {
  protected final URL nodeUrl;
  protected final String regionId;
  protected final String dataCenter;
  protected final String nodeName;

  protected AbstractNode(String dataCenter, URL nodeUrl, String regionId) {
    ParaCheckUtil.checkNotNull(nodeUrl, "nodeURl");
    this.dataCenter = dataCenter;
    this.nodeUrl = nodeUrl;
    this.regionId = regionId;
    this.nodeName = getIp();
  }

  public String getIp() {
    return nodeUrl.getIpAddress();
  }

  @Override
  public URL getNodeUrl() {
    return nodeUrl;
  }

  public String getRegionId() {
    return regionId;
  }

  public String getNodeName() {
    return nodeName;
  }

  public String getDataCenter() {
    return dataCenter;
  }

  public String getAddressString() {
    return nodeUrl.buildAddressString();
  }

  protected boolean equal(AbstractNode node) {
    return Objects.equals(nodeName, node.nodeName)
        && Objects.equals(regionId, node.regionId)
        && Objects.equals(dataCenter, node.dataCenter)
        && Objects.equals(getAddressString(), node.getAddressString());
  }

  protected int hash() {
    return Objects.hash(nodeName, regionId, dataCenter, getAddressString());
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "{}-Node{{},region={},dc={}}", getNodeType(), nodeName, regionId, dataCenter);
  }
}
