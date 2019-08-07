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

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.core.constants.EventTypeConstants;
import com.alipay.sofa.registry.core.model.ConfiguratorRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltChannelUtil;
import com.alipay.sofa.registry.server.session.converter.SubscriberConverter;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.alipay.sofa.registry.server.session.strategy.WatcherHandlerStrategy;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class DefaultWatcherHandlerStrategy implements WatcherHandlerStrategy {
    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(DefaultWatcherHandlerStrategy.class);

    @Autowired
    private Registry            sessionRegistry;

    @Override
    public void handleConfiguratorRegister(Channel channel,
                                           ConfiguratorRegister configuratorRegister,
                                           RegisterResponse registerResponse) {
        try {
            configuratorRegister.setIp(channel.getRemoteAddress().getAddress().getHostAddress());
            configuratorRegister.setPort(channel.getRemoteAddress().getPort());

            if (StringUtils.isBlank(configuratorRegister.getInstanceId())) {
                configuratorRegister.setInstanceId(DEFAULT_INSTANCE_ID);
            }

            Watcher watcher = SubscriberConverter.convert(configuratorRegister);
            watcher.setProcessId(channel.getRemoteAddress().getHostName() + ":"
                                 + channel.getRemoteAddress().getPort());
            watcher.setSourceAddress(new URL(channel.getRemoteAddress(), BoltChannelUtil
                .getBoltCustomSerializer(channel)));

            if (EventTypeConstants.REGISTER.equals(configuratorRegister.getEventType())) {
                sessionRegistry.register(watcher);
            } else if (EventTypeConstants.UNREGISTER.equals(configuratorRegister.getEventType())) {
                sessionRegistry.unRegister(watcher);
            }
            registerResponse.setVersion(configuratorRegister.getVersion());
            registerResponse.setRegistId(configuratorRegister.getRegistId());
            LOGGER.info("ConfiguratorRegister register success! {}", watcher);
            registerResponse.setSuccess(true);
            registerResponse.setMessage("ConfiguratorRegister register success!");
        } catch (Exception e) {
            LOGGER.error("ConfiguratorRegister register error!", e);
            registerResponse.setSuccess(false);
            registerResponse.setMessage("ConfiguratorRegister register failed!");
        }
    }
}
