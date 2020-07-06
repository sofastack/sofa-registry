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
package com.alipay.sofa.registry.jraft.handler;

import com.alipay.remoting.rpc.RpcClient;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.rpc.CliClientService;
import com.alipay.sofa.registry.jraft.bootstrap.RaftClient;
import com.alipay.sofa.registry.jraft.command.ConfigurationCommitted;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.RemotingException;
import com.alipay.sofa.registry.remoting.bolt.SyncUserProcessorAdapter;

/**
 * @author xiangxu
 * @version : NotifyConfigurationCommittedHandler.java, v 0.1 2020年07月02日 7:53 下午 xiangxu Exp $
 */
public class ConfigurationCommittedHandler implements ChannelHandler {
    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(ConfigurationCommittedHandler.class);
    private String              groupId;
    private CliClientService    clientService;

    public ConfigurationCommittedHandler(String groupId, CliClientService clientService) {
        this.groupId = groupId;
        this.clientService = clientService;
    }

    @Override
    public void connected(Channel channel) throws RemotingException {

    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {

    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {

    }

    @Override
    public Object reply(Channel channel, Object message) throws RemotingException {
        ConfigurationCommitted configurationCommitted = (ConfigurationCommitted) message;
        LOGGER.info(
            "Receive ConfigurationCommitted request:{}, remote address: {}, localAddress:{}",
            configurationCommitted.peers, channel.getRemoteAddress(), channel.getLocalAddress());
        if (clientService != null) {
            RaftClient.refreshConfiguration(clientService, groupId,
                JRaftUtils.getConfiguration(configurationCommitted.peers), 5000);
        }
        return null;
    }

    @Override
    public void caught(Channel channel, Object message, Throwable exception)
                                                                            throws RemotingException {
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return ConfigurationCommitted.class;
    }

    public static void registerMockHandler(RpcClient rpcClient) {
        rpcClient.registerUserProcessor(new SyncUserProcessorAdapter(
            new ConfigurationCommittedHandler(null, null) {
                @Override
                public Object reply(Channel channel, Object message) throws RemotingException {
                    return null;
                }
            }));
    }
}