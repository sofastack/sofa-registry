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
package com.alipay.sofa.registry.server.meta.remoting.connection;

import com.alipay.sofa.registry.common.model.Node.NodeType;

/**
 * Handle data node's connect request
 *
 * @author shangyu.wh
 * @version $Id: DataConnectionManager.java, v 0.1 2018-01-24 16:04 shangyu.wh Exp $
 */
//DataServerConnectionFactory 就是用来对`com.alipay.remoting.Connection`进行连接管理。
//
//其核心变量是以ip:port作为key，Connection作为value的一个Map。
public class DataConnectionManager extends AbstractNodeConnectManager {

  @Override
  public NodeType getConnectNodeType() {
    return NodeType.DATA;
  }

  @Override
  protected int getServerPort() {
    return metaServerConfig.getDataServerPort();
  }
}
