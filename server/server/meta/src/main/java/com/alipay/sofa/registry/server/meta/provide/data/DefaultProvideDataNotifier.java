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
package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.server.meta.remoting.data.DefaultDataServerService;
import com.alipay.sofa.registry.server.meta.remoting.session.DefaultSessionServerService;
import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chen.zhu
 *     <p>Dec 03, 2020
 *     <p>Provide Data is designed for two scenerio, as below: 1. Dynamic Configs inside
 *     Sofa-Registry itself 2. Service Gaven (or say 'Watcher') are subscring messages through
 *     Session-Server
 *     <p>All above user cases stages a Config Center role by Sofa-Registry And all these infos are
 *     madantorily persistenced to disk So, by leveraging meta server's JRaft feature, infos are
 *     reliable and stable to be stored on MetaServer
 */
@Component
public class DefaultProvideDataNotifier implements ProvideDataNotifier {

  @Autowired private DefaultDataServerService defaultDataServerService;

  @Autowired private DefaultSessionServerService defaultSessionServerService;

  @Override
  public void notifyProvideDataChange(ProvideDataChangeEvent event) {
    Set<Node.NodeType> notifyTypes = event.getNodeTypes();
    if (notifyTypes.contains(Node.NodeType.DATA)) {
      defaultDataServerService.notifyProvideDataChange(event);
    }
    if (notifyTypes.contains(Node.NodeType.SESSION)) {
      defaultSessionServerService.notifyProvideDataChange(event);
    }
  }

  @VisibleForTesting
  DefaultProvideDataNotifier setDataServerProvideDataNotifier(
      DefaultDataServerService defaultDataServerService) {
    this.defaultDataServerService = defaultDataServerService;
    return this;
  }

  @VisibleForTesting
  DefaultProvideDataNotifier setSessionServerProvideDataNotifier(
      DefaultSessionServerService defaultSessionServerService) {
    this.defaultSessionServerService = defaultSessionServerService;
    return this;
  }
}
