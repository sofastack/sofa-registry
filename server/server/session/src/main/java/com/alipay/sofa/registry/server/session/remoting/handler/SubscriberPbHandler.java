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
import com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb;
import com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.core.model.SubscriberRegister;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.RemotingException;
import com.alipay.sofa.registry.server.session.converter.pb.RegisterResponseConvertor;
import com.alipay.sofa.registry.server.session.converter.pb.SubscriberRegisterConvertor;
import com.alipay.sofa.registry.server.shared.remoting.RemotingHelper;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhuoyu.sjw
 * @version $Id: SubscriberPbHandler.java, v 0.1 2018-04-02 16:03 zhuoyu.sjw Exp $$
 */
public class SubscriberPbHandler extends AbstractClientDataRequestHandler<SubscriberRegisterPb> {

  @Autowired SubscriberHandler subscriberHandler;

  @Override
  protected Node.NodeType getConnectNodeType() {
    return subscriberHandler.getConnectNodeType();
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
  public Object doHandle(Channel channel, SubscriberRegisterPb message) {
    RemotingHelper.markProtobuf(channel);
    RemotingHelper.setPbSerializer(channel);

    RegisterResponsePb.Builder builder = RegisterResponsePb.newBuilder();
    SubscriberRegister register = SubscriberRegisterConvertor.convert2Java(message);
    Object response = subscriberHandler.doHandle(channel, register);
    if (!(response instanceof RegisterResponse)) {
      return builder.setSuccess(false).setMessage("Unknown response type").build();
    }

    return RegisterResponseConvertor.convert2Pb((RegisterResponse) response);
  }

  /**
   * Interest class.
   *
   * @return the class
   */
  @Override
  public Class interest() {
    return SubscriberRegisterPb.class;
  }

  @Override
  public Executor getExecutor() {
    return subscriberHandler.getExecutor();
  }
}
