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
    private static final Logger LOGGER     = LoggerFactory
                                               .getLogger(DefaultSubscriberHandlerStrategy.class);
    private static final Logger SUB_LOGGER = LoggerFactory.getLogger("SUB-RECEIVE");

    @Autowired
    private Registry            sessionRegistry;

    @Override
    public void handleSubscriberRegister(Channel channel, SubscriberRegister subscriberRegister,
                                         RegisterResponse registerResponse) {
        Subscriber subscriber = null;
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

            subscriber = SubscriberConverter.convert(subscriberRegister);
            subscriber.setProcessId(ip + ":" + port);

            handle(subscriber, channel, subscriberRegister, registerResponse);
        } catch (Throwable e) {
            handleError(subscriberRegister, subscriber, registerResponse, e);
        }
    }

    protected void handle(Subscriber subscriber, Channel channel,
                          SubscriberRegister subscriberRegister, RegisterResponse registerResponse) {
        subscriber.setSourceAddress(new URL(channel.getRemoteAddress(), BoltChannelUtil
            .getBoltCustomSerializer(channel)));
        subscriber.setTargetAddress(new URL(channel.getLocalAddress()));

        final String eventType = subscriberRegister.getEventType();
        if (EventTypeConstants.REGISTER.equals(eventType)) {
            sessionRegistry.register(subscriber);
        } else if (EventTypeConstants.UNREGISTER.equals(eventType)) {
            sessionRegistry.unRegister(subscriber);
        } else {
            LOGGER.warn("unsupported subscriber.eventType:{}", eventType);
        }
        registerResponse.setVersion(subscriberRegister.getVersion());
        registerResponse.setRegistId(subscriberRegister.getRegistId());
        registerResponse.setSuccess(true);
        registerResponse.setMessage("Subscriber register success!");
        log(true, subscriberRegister, subscriber);
    }

    private void log(boolean success, SubscriberRegister subscriberRegister, Subscriber subscriber) {
        //[Y|N],[R|U|N],app,zone,dataInfoId,registerId,scope,assembleType,elementType,clientVersion,clientIp,clientPort
        SUB_LOGGER.info("{},{},{},{},{},{},{},{},{},{},{},{},{},{}", success ? 'Y' : 'N',
            getEventTypeFlag(subscriberRegister.getEventType()), subscriberRegister.getAppName(),
            subscriberRegister.getZone(), subscriberRegister.getDataId(), subscriberRegister
                .getGroup(), subscriberRegister.getInstanceId(), subscriberRegister.getRegistId(),
            subscriberRegister.getScope(), subscriber == null ? "" : subscriber.getAssembleType(),
            subscriber == null ? "" : subscriber.getElementType(), subscriber == null ? ""
                : subscriber.getClientVersion(), subscriberRegister.getIp(), subscriberRegister
                .getPort());
    }

    private char getEventTypeFlag(String eventType) {
        if (EventTypeConstants.REGISTER.equals(eventType)) {
            return 'R';
        } else if (EventTypeConstants.UNREGISTER.equals(eventType)) {
            return 'U';
        }
        return 'N';
    }

    protected void handleError(SubscriberRegister subscriberRegister, Subscriber subscriber,
                               RegisterResponse registerResponse, Throwable e) {
        log(false, subscriberRegister, subscriber);
        LOGGER.error("Publisher register error!Type {}", subscriberRegister.getEventType(), e);
        registerResponse.setSuccess(false);
        registerResponse.setMessage("Publisher register failed!Type:"
                                    + subscriberRegister.getEventType());
    }
}
