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
package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.remoting.RaftExchanger;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author shangyu.wh
 * @version $Id: MetaStoreResource.java, v 0.1 2018-07-30 18:55 shangyu.wh Exp $
 */
@Path("manage")
public class MetaStoreResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaStoreResource.class);

    @Autowired
    private CurrentDcMetaServer currentDcMetaServer;

    @Autowired
    private NodeConfig          nodeConfig;

    @Autowired
    private RaftExchanger       raftExchanger;

    /**
     * set node list in raft
     */
    @POST
    @Path("changePeer")
    @Produces(MediaType.APPLICATION_JSON)
    public Result changePeer(@FormParam("ipAddressList") String ipAddressList) {
        Result result = new Result();
        result.setSuccess(false);

        List<String> ipAddressList0 = null;
        if (StringUtils.isNotBlank(ipAddressList)) {
            String[] ipAddressArr = StringUtils.split(StringUtils.trim(ipAddressList), ',');
            if (ipAddressArr != null) {
                ipAddressList0 = Lists.newArrayList(ipAddressArr);
            }
        }
        if (ipAddressList0 == null || ipAddressList0.size() <= 0) {
            String msg = String.format("Empty ipAddressList from input: %s", ipAddressList);
            LOGGER.error(msg);
            result.setMessage(msg);
            return result;
        }

        //domain -> ip
        ipAddressList0 = ipAddressList0.stream().map(NetUtil::getIPAddressFromDomain).collect(Collectors.toList());

        try {
            raftExchanger.changePeer(ipAddressList0);

            //sleep for a while after changePeer: if leader changed, 'metaServerRegistry.setNodes(metaNodes);' will throw error:
            // java.lang.IllegalStateException: Server error:is not leader
            //  at com.alipay.sofa.registry.jraft.bootstrap.RaftClient.sendRequest(RaftClient.java:192)
            Thread.sleep(3000L);

            List<MetaNode> metaNodes = Lists.newArrayList();
            for (String ipAddress : ipAddressList0) {
                MetaNode metaNode = new MetaNode(new URL(ipAddress, 0), nodeConfig.getLocalDataCenter());
                metaNodes.add(metaNode);
            }

            currentDcMetaServer.updateClusterMembers(metaNodes, DatumVersionUtil.nextId());
            result.setSuccess(true);

            LOGGER.info("Change peer ipAddressList {} to store!", ipAddressList0);
        } catch (Exception e) {
            String msg = String.format("Error when changePeer： %s", e.getMessage());
            result.setMessage(msg);
            LOGGER.error(msg, e);
        }
        return result;
    }

    /**
     * force set node list in raft (no need leader alive), maybe lost data
     */
    @POST
    @Path("resetPeer")
    @Produces(MediaType.APPLICATION_JSON)
    public Result resetPeer(@FormParam("ipAddressList") String ipAddressList) {
        Result result = new Result();
        result.setSuccess(false);

        List<String> ipAddressList0 = null;
        if (StringUtils.isNotBlank(ipAddressList)) {
            String[] ipAddressArr = StringUtils.split(StringUtils.trim(ipAddressList), ',');
            if (ipAddressArr != null) {
                ipAddressList0 = Lists.newArrayList(ipAddressArr);
            }
        }
        if (ipAddressList0 == null || ipAddressList0.size() <= 0) {
            String msg = String.format("Empty ipAddressList from input: %s", ipAddressList);
            LOGGER.error(msg);
            result.setMessage(msg);
            return result;
        }

        //domain -> ip
        ipAddressList0 = ipAddressList0.stream().map(NetUtil::getIPAddressFromDomain).collect(Collectors.toList());

        try {
            raftExchanger.resetPeer(ipAddressList0);

            List<MetaNode> metaNodes = Lists.newArrayList();
            for (String ipAddress : ipAddressList0) {
                MetaNode metaNode = new MetaNode(new URL(ipAddress, 0), nodeConfig.getLocalDataCenter());
                metaNodes.add(metaNode);
            }
            currentDcMetaServer.updateClusterMembers(metaNodes, DatumVersionUtil.nextId());

            result.setSuccess(true);

            LOGGER.info("Reset peer ipAddressList {} to store!", ipAddressList0);
        } catch (Exception e) {
            String msg = String.format("Error when resetPeer： %s", e.getMessage());
            result.setMessage(msg);
            LOGGER.error(msg, e);
        }
        return result;
    }

    @POST
    @Path("removePeer")
    @Produces(MediaType.APPLICATION_JSON)
    public Result remove(Map<String/*ipAddress*/, String/*dataCenter*/> map) {

        Result result = new Result();
        if (map != null && !map.isEmpty()) {

            Map<String, String> throwables = new HashMap<>();
            map.forEach((ipAddress, dataCenter) -> {
                try {
                    if (!nodeConfig.getLocalDataCenter().equals(dataCenter)) {
                        LOGGER.error("Error input dataCenter {} ,current dataCenter {}!", dataCenter,
                                nodeConfig.getLocalDataCenter());
                        throwables.put(ipAddress, "Error input dataCenter");
                        return;
                    }

                    //domain -> ip
                    ipAddress = NetUtil.getIPAddressFromDomain(ipAddress);

                    raftExchanger.removePeer(ipAddress);
                    currentDcMetaServer.cancel(new MetaNode(new URL(ipAddress), dataCenter));
                    LOGGER.info("Remove peer ipAddress {} to store!", ipAddress);
                } catch (Exception e) {
                    LOGGER.error("Error remove peer ipAddress {} to store!", ipAddress, e);
                    throwables.put(ipAddress, e.getMessage());
                }
            });

            if (throwables.isEmpty()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
                result.setMessage(String.format("Nod remove all peer node,there are %s exception!", throwables.size()));
            }
        } else {
            result.setSuccess(false);
            result.setMessage("Input node map is empty!");
        }
        return result;
    }

    @GET
    @Path("queryPeer")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getMetaList() {
        List<PeerId> peerIds = raftExchanger.getPeers();
        return peerIds.stream().map(PeerId::getIp).collect(Collectors.toList());
    }
}