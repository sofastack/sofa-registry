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
package com.alipay.sofa.registry.server.meta.remoting.handler;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: ServerHandler.java, v 0.1 2017-11-28 18:06 shangyu.wh Exp $
 */
public abstract class BaseMetaServerHandler<T> extends AbstractServerHandler<T> {

  @Autowired private ThreadPoolExecutor defaultRequestExecutor;

  @Override
  public Executor getExecutor() {
    return defaultRequestExecutor;
  }

  @Override
  protected Node.NodeType getConnectNodeType() {
    return null;
  }
}
