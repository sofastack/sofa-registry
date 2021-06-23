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
import com.alipay.sofa.registry.common.model.client.pb.ResultPb;
import com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb;
import com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb;
import com.alipay.sofa.registry.core.model.SyncConfigResponse;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.RemotingException;
import com.alipay.sofa.registry.server.session.converter.pb.SyncConfigRequestConvertor;
import com.alipay.sofa.registry.server.session.converter.pb.SyncConfigResponseConvertor;
import com.alipay.sofa.registry.server.shared.remoting.RemotingHelper;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhuoyu.sjw
 * @version $Id: SyncConfigPbHandler.java, v 0.1 2018-04-02 17:13 zhuoyu.sjw Exp $$
 */
public class SyncConfigPbHandler extends AbstractClientDataRequestHandler<SyncConfigRequestPb> {

  @Autowired SyncConfigHandler syncConfigHandler;

  @Override
  protected Node.NodeType getConnectNodeType() {
    return syncConfigHandler.getConnectNodeType();
  }

  /**
   * Reply object.
   *
   * @param channel the channel
   * @param message the message
   * @return the object
   * @throws RemotingException the remoting exception
   */
  @Override
  public Object doHandle(Channel channel, SyncConfigRequestPb message) {
    RemotingHelper.markProtobuf(channel);
    Object response =
        syncConfigHandler.doHandle(channel, SyncConfigRequestConvertor.convert2Java(message));
    if (!(response instanceof SyncConfigResponse)) {
      return fail();
    }

    return SyncConfigResponseConvertor.convert2Pb((SyncConfigResponse) response);
  }

  static SyncConfigResponsePb fail() {
    SyncConfigResponsePb.Builder builder = SyncConfigResponsePb.newBuilder();
    return builder
        .setResult(
            ResultPb.newBuilder()
                .setSuccess(false)
                .setMessage("Unknown sync config response type")
                .build())
        .build();
  }

  /**
   * Interest class.
   *
   * @return the class
   */
  @Override
  public Class interest() {
    return SyncConfigRequestPb.class;
  }

  @Override
  public Executor getExecutor() {
    return syncConfigHandler.getExecutor();
  }
}
