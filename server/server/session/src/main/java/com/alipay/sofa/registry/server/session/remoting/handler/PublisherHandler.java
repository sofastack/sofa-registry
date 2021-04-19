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

import com.alipay.sofa.registry.core.model.PublisherRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.strategy.PublisherHandlerStrategy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO
 *
 * @author shangyu.wh
 * @version $Id: SessionHandler.java, v 0.1 2017-11-29 11:32 shangyu.wh Exp $
 */
public class PublisherHandler extends AbstractClientDataRequestHandler<PublisherRegister> {

  @Autowired PublisherHandlerStrategy publisherHandlerStrategy;

  @Override
  public Object doHandle(Channel channel, PublisherRegister publisherRegister) {
    RegisterResponse result = new RegisterResponse();
    publisherHandlerStrategy.handlePublisherRegister(channel, publisherRegister, result);
    return result;
  }

  @Override
  public Class interest() {
    return PublisherRegister.class;
  }
}
