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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.cleaner.AppRevisionSlice;
import com.alipay.sofa.registry.common.model.metaserver.cleaner.AppRevisionSliceRequest;
import com.alipay.sofa.registry.common.model.slot.func.Crc32cSlotFunction;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.shared.remoting.AbstractClientHandler;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.glassfish.jersey.internal.guava.Sets;
import org.springframework.beans.factory.annotation.Autowired;

public class AppRevisionSliceHandler extends AbstractClientHandler<AppRevisionSliceRequest> {

  @Autowired ThreadPoolExecutor metaNodeExecutor;

  @Autowired protected AppRevisionRepository appRevisionJdbcRepository;

  @Override
  protected Node.NodeType getConnectNodeType() {
    return Node.NodeType.META;
  }

  @Override
  public Object doHandle(Channel channel, AppRevisionSliceRequest request) {
    Set<String> revisions = Sets.newHashSet();
    Crc32cSlotFunction function = new Crc32cSlotFunction(request.getSlotNum());
    for (String revision : appRevisionJdbcRepository.availableRevisions()) {
      if (function.slotOf(revision) == request.getSlotId()) {
        revisions.add(revision);
      }
    }
    return new AppRevisionSlice(revisions);
  }

  @Override
  public Class interest() {
    return AppRevisionSliceRequest.class;
  }

  @Override
  public Executor getExecutor() {
    return metaNodeExecutor;
  }
}
