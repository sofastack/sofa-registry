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
package com.alipay.sofa.registry.server.session.strategy.impl;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.constants.EventTypeConstants;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.converter.PublisherConverter;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.remoting.handler.PublisherHandler;
import com.alipay.sofa.registry.server.session.strategy.PublisherHandlerStrategy;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultPublisherHandlerStrategy implements PublisherHandlerStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(PublisherHandler.class);

    @Autowired
    private Registry            sessionRegistry;

    @Override
    public void handlePublisherRegister(Channel channel, PublisherRegister publisherRegister,
                                        RegisterResponse registerResponse) {
        try {
            String ip = channel.getRemoteAddress().getAddress().getHostAddress();
            int port = channel.getRemoteAddress().getPort();
            publisherRegister.setIp(ip);
            publisherRegister.setPort(port);

            if (StringUtils.isBlank(publisherRegister.getZone())) {
                publisherRegister.setZone(ValueConstants.DEFAULT_ZONE);
            }

            if (StringUtils.isBlank(publisherRegister.getInstanceId())) {
                publisherRegister.setInstanceId(DEFAULT_INSTANCE_ID);
            }

            Publisher publisher = PublisherConverter.convert(publisherRegister);
            publisher.setProcessId(ip + ":" + port);
            publisher.setSourceAddress(new URL(channel.getRemoteAddress()));
            publisher.setTargetAddress(new URL(channel.getLocalAddress()));
            if (EventTypeConstants.REGISTER.equals(publisherRegister.getEventType())) {
                sessionRegistry.register(publisher);
            } else if (EventTypeConstants.UNREGISTER.equals(publisherRegister.getEventType())) {
                sessionRegistry.unRegister(publisher);
            }
            registerResponse.setSuccess(true);
            registerResponse.setVersion(publisher.getVersion());
            registerResponse.setRegistId(publisherRegister.getRegistId());
            registerResponse.setMessage("Publisher register success!");
            LOGGER.info("Publisher register success!Type:{} Info:{}",
                publisherRegister.getEventType(), publisher);
        } catch (Exception e) {
            LOGGER.error("Publisher register error!Type {}", publisherRegister.getEventType(), e);
            registerResponse.setSuccess(false);
            registerResponse.setMessage("Publisher register failed!Type:"
                                        + publisherRegister.getEventType());
        }
    }
}
