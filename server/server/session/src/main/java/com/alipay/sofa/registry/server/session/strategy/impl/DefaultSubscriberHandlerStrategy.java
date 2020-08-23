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
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.constants.EventTypeConstants;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.core.model.SubscriberRegister;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltChannelUtil;
import com.alipay.sofa.registry.server.session.converter.SubscriberConverter;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.strategy.SubscriberHandlerStrategy;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultSubscriberHandlerStrategy implements SubscriberHandlerStrategy {
    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(DefaultSubscriberHandlerStrategy.class);

    @Autowired
    private Registry            sessionRegistry;

    @Override
    public void handleSubscriberRegister(Channel channel, SubscriberRegister subscriberRegister,
                                         RegisterResponse registerResponse) {
        try {
            String ip = channel.getRemoteAddress().getAddress().getHostAddress();
            int port = channel.getRemoteAddress().getPort();
            subscriberRegister.setIp(ip);
            subscriberRegister.setPort(port);

            if (StringUtils.isBlank(subscriberRegister.getZone())) {
                subscriberRegister.setZone(ValueConstants.DEFAULT_ZONE);
            }

            if (StringUtils.isBlank(subscriberRegister.getInstanceId())) {
                subscriberRegister.setInstanceId(DEFAULT_INSTANCE_ID);
            }

            Subscriber subscriber = SubscriberConverter.convert(subscriberRegister);
            subscriber.setProcessId(ip + ":" + port);
            subscriber.setSourceAddress(new URL(channel.getRemoteAddress(), BoltChannelUtil
                .getBoltCustomSerializer(channel)));
            subscriber.setTargetAddress(new URL(channel.getLocalAddress()));

            if (EventTypeConstants.REGISTER.equals(subscriberRegister.getEventType())) {
                sessionRegistry.register(subscriber);
            } else if (EventTypeConstants.UNREGISTER.equals(subscriberRegister.getEventType())) {
                sessionRegistry.unRegister(subscriber);
            }
            registerResponse.setVersion(subscriberRegister.getVersion());
            registerResponse.setRegistId(subscriberRegister.getRegistId());
            LOGGER.info("Subscriber register success! Type:{} Info:{}",
                subscriberRegister.getEventType(), subscriber);
            registerResponse.setSuccess(true);
            registerResponse.setMessage("Subscriber register success!");
        } catch (Exception e) {
            LOGGER.error("Subscriber register error!Type{}", subscriberRegister.getEventType(), e);
            registerResponse.setSuccess(false);
            registerResponse.setMessage("Subscriber register failed!");
        }
    }
}
