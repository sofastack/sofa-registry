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
package com.alipay.sofa.registry.server.session.scheduler.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.alipay.sofa.registry.common.model.dataserver.SessionServerRegisterRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.SessionProcessIdGenerator;
import com.alipay.sofa.registry.task.listener.TaskEvent;

/**
 *
 * @author shangyu.wh
 * @author kezhu.wukz
 * @version $Id: SessionRegisterDataTask.java, v 0.1 2018-04-16 16:07 shangyu.wh Exp $
 */
public class SessionRegisterDataTask extends AbstractSessionTask {

    private static final Logger          LOGGER = LoggerFactory.getLogger(
                                                    SessionRegisterDataTask.class, "[Task]");

    private final Exchange               boltExchange;
    private final SessionServerConfig    sessionServerConfig;

    private SessionServerRegisterRequest sessionServerRegisterRequest;
    private BoltChannel                  channel;

    public SessionRegisterDataTask(Exchange boltExchange, SessionServerConfig sessionServerConfig) {
        this.boltExchange = boltExchange;
        this.sessionServerConfig = sessionServerConfig;
    }

    @Override
    public boolean checkRetryTimes() {
        return checkRetryTimes(sessionServerConfig.getSessionRegisterDataServerTaskRetryTimes());
    }

    @Override
    public void setTaskEvent(TaskEvent taskEvent) {

        //taskId create from event
        if (taskEvent.getTaskId() != null) {
            setTaskId(taskEvent.getTaskId());
        }

        Object obj = taskEvent.getEventObj();

        if (obj instanceof BoltChannel) {
            this.channel = (BoltChannel) obj;

        } else {
            throw new IllegalArgumentException("Input task event object error!");
        }
        Server sessionServer = boltExchange.getServer(sessionServerConfig.getServerPort());

        if (sessionServer != null) {

            Collection<Channel> chs = sessionServer.getChannels();
            Set<String> connectIds = new HashSet<>();
            chs.forEach(channel -> connectIds.add(NetUtil.toAddressString(channel.getRemoteAddress())));

            sessionServerRegisterRequest = new SessionServerRegisterRequest(
                    SessionProcessIdGenerator.getSessionProcessId(), connectIds);
        } else {
            LOGGER.error("get null session server,please check server started before register!port {}",
                    sessionServerConfig.getServerPort());
            sessionServerRegisterRequest = new SessionServerRegisterRequest(
                    SessionProcessIdGenerator.getSessionProcessId(), new HashSet<>());
        }
    }

    @Override
    public void execute() {
        if (!channel.isConnected()) {
            return;
        }

        Client sessionClient = boltExchange.getClient(Exchange.DATA_SERVER_TYPE);
        try {
            sessionClient.sendSync(channel, sessionServerRegisterRequest,
                sessionServerConfig.getDataNodeExchangeTimeOut());
        } catch (Exception e) {
            if (isLastRetry()) {
                LOGGER
                    .error(
                        "Register to DataServer({}/{}:{}) error for multiple times, so close this channel (let bolt reconnect it)",
                        channel.getLocalAddress(), channel.getRemoteAddress().getHostString(),
                        channel.getRemoteAddress().getPort());
                channel.close();
            }
            throw e;
        }

    }

    protected boolean isLastRetry() {
        return getExecCount() >= sessionServerConfig.getSessionRegisterDataServerTaskRetryTimes();
    }

    @Override
    public String toString() {
        return "SESSION_REGISTER_DATA_TASK{" + "taskId='" + taskId + '\''
               + ", sessionServerRegisterRequest=" + sessionServerRegisterRequest.getProcessId()
               + ", clientList=" + sessionServerRegisterRequest.getConnectIds().size()
               + ", channel=" + channel.getLocalAddress() + "/"
               + channel.getRemoteAddress().getHostString() + ':'
               + channel.getRemoteAddress().getPort() + '}';
    }
}