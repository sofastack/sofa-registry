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
package com.alipay.sofa.registry.jraft.bootstrap;

import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.CliClientService;
import com.alipay.sofa.jraft.rpc.impl.AbstractBoltClientService;
import com.alipay.sofa.jraft.rpc.impl.cli.BoltCliClientService;
import com.alipay.sofa.registry.jraft.command.ProcessRequest;
import com.alipay.sofa.registry.jraft.command.ProcessResponse;
import com.alipay.sofa.registry.jraft.handler.ConfigurationCommittedHandler;
import com.alipay.sofa.registry.jraft.handler.NotifyLeaderChangeHandler;
import com.alipay.sofa.registry.jraft.handler.RaftClientConnectionHandler;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.bolt.ConnectionEventAdapter;
import com.alipay.sofa.registry.remoting.bolt.SyncUserProcessorAdapter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author shangyu.wh
 * @version $Id: RaftClient.java, v 0.1 2018-05-16 11:40 shangyu.wh Exp $
 */
public class RaftClient {

    private static final Logger  LOGGER  = LoggerFactory.getLogger(RaftClient.class);

    private BoltCliClientService cliClientService;
    private RpcClient            rpcClient;
    private CliOptions           cliOptions;
    private String               groupId;
    private Configuration        conf;

    private AtomicBoolean        started = new AtomicBoolean(false);

    /**
     * @param groupId
     * @param confStr  Example: 127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083
     */
    public RaftClient(String groupId, String confStr) {

        this.groupId = groupId;
        conf = new Configuration();
        if (!conf.parse(confStr)) {
            throw new IllegalArgumentException("Fail to parse conf:" + confStr);
        }
        cliOptions = new CliOptions();
        cliClientService = new BoltCliClientService();
    }

    /**
     * @param groupId
     * @param confStr
     * @param cliClientService
     */
    public RaftClient(String groupId, String confStr, AbstractBoltClientService cliClientService) {

        this.groupId = groupId;
        conf = new Configuration();
        if (!conf.parse(confStr)) {
            throw new IllegalArgumentException("Fail to parse conf:" + confStr);
        }
        cliOptions = new CliOptions();
        this.cliClientService = (BoltCliClientService) cliClientService;
    }

    /**
     * raft client start
     */
    public void start() {
        if (started.compareAndSet(false, true)) {

            RouteTable.getInstance().updateConfiguration(groupId, conf);

            cliClientService.init(cliOptions);

            rpcClient = cliClientService.getRpcClient();

            RaftClientConnectionHandler raftClientConnectionHandler = new RaftClientConnectionHandler(
                this);

            rpcClient.addConnectionEventProcessor(ConnectionEventType.CONNECT,
                new ConnectionEventAdapter(ConnectionEventType.CONNECT,
                    raftClientConnectionHandler, null));
            rpcClient.addConnectionEventProcessor(ConnectionEventType.CLOSE,
                new ConnectionEventAdapter(ConnectionEventType.CLOSE, raftClientConnectionHandler,
                    null));
            rpcClient.addConnectionEventProcessor(ConnectionEventType.EXCEPTION,
                new ConnectionEventAdapter(ConnectionEventType.EXCEPTION,
                    raftClientConnectionHandler, null));

            //reset leader notify
            NotifyLeaderChangeHandler notifyLeaderChangeHandler = new NotifyLeaderChangeHandler(
                groupId, cliClientService);
            rpcClient
                .registerUserProcessor(new SyncUserProcessorAdapter(notifyLeaderChangeHandler));

            ConfigurationCommittedHandler configurationCommittedHandler = new ConfigurationCommittedHandler(
                groupId, cliClientService);
            rpcClient.registerUserProcessor(new SyncUserProcessorAdapter(
                configurationCommittedHandler));
        }
    }

    /**
     * stop cliClientService
     */
    public void shutdown() {
        if (cliClientService != null) {
            cliClientService.shutdown();
        }
    }

    /**
     * repick leader
     * @return
     */
    public PeerId refreshLeader() {
        return refreshLeader(cliClientService, groupId, cliOptions.getRpcDefaultTimeout());
    }

    public static PeerId refreshLeader(CliClientService cliClientService, String groupId,
                                       int timeout) {
        try {
            Status status = RouteTable.getInstance().refreshLeader(cliClientService, groupId,
                timeout);
            if (!status.isOk()) {
                throw new IllegalStateException(String.format("Refresh leader failed,error=%s",
                    status.getErrorMsg()));
            }
            PeerId leader = RouteTable.getInstance().selectLeader(groupId);
            LOGGER.info("Leader is {}", leader);

            //avoid refresh leader config ip list must be current list,list maybe change by manage
            status = RouteTable.getInstance().refreshConfiguration(cliClientService, groupId,
                timeout);
            if (!status.isOk()) {
                throw new IllegalStateException(String.format(
                    "Refresh configuration failed, error=%s", status.getErrorMsg()));
            }

            return leader;
        } catch (Exception e) {
            LOGGER.error("Refresh leader failed", e);
            throw new IllegalStateException("Refresh leader failed", e);
        }
    }

    public static void refreshConfiguration(CliClientService clientService, String groupId,
                                            Configuration configuration, int timeout) {
        try {
            Configuration oldConf = RouteTable.getInstance().getConfiguration(groupId);
            Configuration adding = new Configuration();
            Configuration removing = new Configuration();
            configuration.diff(oldConf, adding, removing);
            if (adding.size() + removing.size() > 0) {
                Status status = RouteTable.getInstance().refreshConfiguration(clientService,
                    groupId, timeout);
                if (!status.isOk()) {
                    throw new IllegalStateException(String.format(
                        "Refresh configuration failed, error=%s", status.getErrorMsg()));
                }
            }

        } catch (Exception e) {
            LOGGER.error("Update configuration failed", e);
            throw new IllegalStateException("Update configuration failed", e);
        }
    }

    /**
     * get leader
     * @return
     */
    public PeerId getLeader() {
        PeerId leader = RouteTable.getInstance().selectLeader(groupId);
        if (leader == null) {
            leader = refreshLeader();
        }
        return leader;
    }

    /**
     * raft client send request
     * @param request
     * @return
     */
    public Object sendRequest(ProcessRequest request) {
        try {
            if (!started.get()) {
                LOGGER.error("Client must be started before send request!");
                throw new IllegalStateException("Client must be started before send request!");
            }

            PeerId peer = getLeader();
            LOGGER.info("Raft client send message {} to url {}", request, peer.getEndpoint()
                .toString());
            Object response = this.rpcClient.invokeSync(peer.getEndpoint().toString(), request,
                cliOptions.getRpcDefaultTimeout());
            if (response == null) {
                LOGGER.error("Send process request has no response return!");
                throw new RuntimeException("Send process request has no response return!");
            }
            ProcessResponse cmd = (ProcessResponse) response;
            if (cmd.getSuccess()) {
                return cmd.getEntity();
            } else {
                String redirect = cmd.getRedirect();
                if (redirect != null && !redirect.isEmpty()) {
                    return redirectRequest(request, redirect);
                } else {
                    throw new IllegalStateException("Server error:" + cmd.getEntity());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Send process request error!", e);
            throw new RuntimeException("Send process request error!" + e.getMessage(), e);
        }
    }

    private Object redirectRequest(ProcessRequest request, String redirect) {
        try {
            PeerId redirectLead = new PeerId();
            if (!redirectLead.parse(redirect)) {
                throw new IllegalArgumentException("Fail to parse serverId:" + redirect);
            }

            //wait for onLeaderStart
            TimeUnit.MILLISECONDS.sleep(1000);

            LOGGER.info("Redirect request send to return peer {},request {}", redirect, request);
            Object response = this.rpcClient.invokeSync(redirectLead.getEndpoint().toString(),
                request, cliOptions.getRpcDefaultTimeout());
            ProcessResponse cmd = (ProcessResponse) response;
            if (cmd.getSuccess()) {
                RouteTable.getInstance().updateLeader(groupId, redirectLead);
                return cmd.getEntity();
            } else {
                refreshLeader();
                throw new IllegalStateException("Redirect request server error:" + cmd.getEntity());
            }
        } catch (Exception e) {
            LOGGER.error("Redirect process request error!", e);
            throw new RuntimeException("Redirect process request error!" + e.getMessage(), e);
        }
    }

    /**
     * Getter method for property <tt>groupId</tt>.
     *
     * @return property value of groupId
     */
    public String getGroupId() {
        return groupId;
    }
}