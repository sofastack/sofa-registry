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

import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.rpc.CliClientService;
import com.alipay.sofa.registry.jraft.bootstrap.RaftClient;
import com.alipay.sofa.registry.jraft.command.NotifyLeaderChange;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;

/**
 *
 * @author shangyu.wh
 * @version $Id: NotifyLeaderChangeHandler.java, v 0.1 2018-06-23 14:37 shangyu.wh Exp $
 */
public class NotifyLeaderChangeHandler implements ChannelHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyLeaderChangeHandler.class);

    private String              groupId;

    private CliClientService    clientService;

    /**
     * constructor
     * @param groupId
     */
    public NotifyLeaderChangeHandler(String groupId, CliClientService clientService) {
        this.groupId = groupId;
        this.clientService = clientService;
    }

    @Override
    public void connected(Channel channel) {

    }

    @Override
    public void disconnected(Channel channel) {

    }

    @Override
    public void received(Channel channel, Object message) {

    }

    @Override
    public Object reply(Channel channel, Object message) {

        NotifyLeaderChange notifyLeaderChange = (NotifyLeaderChange) message;
        LOGGER.info("Receive NotifyLeaderChange request:{},remote address:{}, localAddress:",
            notifyLeaderChange, channel.getRemoteAddress(), channel.getLocalAddress());

        //reset leader
        if (clientService != null) {
            PeerId peerId = RaftClient.refreshLeader(clientService, groupId, 5000);
            if (peerId != null) {
                LOGGER.info("Reset leader for raft group {},leader()", groupId, peerId);
            }
        }

        return null;
    }

    @Override
    public void caught(Channel channel, Object message, Throwable exception) {

    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return NotifyLeaderChange.class;
    }
}