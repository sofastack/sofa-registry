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
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.client.pb.GetRevisionsRequest;
import com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.strategy.AppRevisionHandlerStrategy;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: GetRevisionPbHandler.java, v 0.1 2021年02月04日 21:55 xiaojian.xj Exp $
 */
public class GetRevisionPbHandler extends AbstractServerHandler<GetRevisionsRequest> {

  @Autowired private AppRevisionHandlerStrategy appRevisionHandlerStrategy;

  @Override
  protected NodeType getConnectNodeType() {
    return Node.NodeType.CLIENT;
  }

  @Override
  public Object doHandle(Channel channel, GetRevisionsRequest request) {
    List<String> revisions = request.getRevisionsList().subList(0, request.getRevisionsCount());
    GetRevisionsResponse response = appRevisionHandlerStrategy.queryRevision(revisions);
    return response;
  }

  @Override
  public Class interest() {
    return GetRevisionsRequest.class;
  }
}
