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
package com.alipay.sofa.registry.server.session.remoting.handler;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.shared.providedata.SystemPropertyProcessorManager;
import com.alipay.sofa.registry.server.shared.remoting.AbstractClientHandler;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version $Id: DataChangeRequestHandler.java, v 0.1 2017-12-12 15:09 shangyu.wh Exp $
 */
public class NotifyProvideDataChangeHandler extends AbstractClientHandler<ProvideDataChangeEvent> {

  @Autowired ThreadPoolExecutor metaNodeExecutor;

  @Autowired SystemPropertyProcessorManager systemPropertyProcessorManager;

  @Override
  protected NodeType getConnectNodeType() {
    return NodeType.META;
  }

  @Override
  public Object doHandle(Channel channel, ProvideDataChangeEvent provideDataChangeEvent) {
    final String notifyDataInfoId = provideDataChangeEvent.getDataInfoId();

    // system data do fetch
    systemPropertyProcessorManager.doFetch(notifyDataInfoId);
    return null;
  }

  @Override
  public Class interest() {
    return ProvideDataChangeEvent.class;
  }

  @Override
  public Executor getExecutor() {
    return metaNodeExecutor;
  }
}
