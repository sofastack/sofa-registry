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
package com.alipay.sofa.registry.server.data.remoting.sessionserver.forward;

import com.alipay.remoting.exception.RemotingException;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.node.DataNodeStatus;
import com.alipay.sofa.registry.server.data.node.DataServerNode;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerNodeFactory;
import com.alipay.sofa.registry.server.data.util.LocalServerStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * The type Forward service.
 * @author zhuoyu.sjw
 * @version $Id : ForwardServiceImpl.java, v 0.1 2018-06-16 17:44 zhuoyu.sjw Exp $$
 */
public class ForwardServiceImpl implements ForwardService {

    /** LOGGER */
    private static final Logger LOGGER = LoggerFactory.getLogger(ForwardServiceImpl.class);

    @Autowired
    private DataServerConfig    dataServerBootstrapConfig;

    @Autowired
    private DataNodeStatus      dataNodeStatus;

    @Autowired
    private DataNodeExchanger   dataNodeExchanger;

    /**
     * @see ForwardService#needForward(String)
     */
    @Override
    public boolean needForward(String dataInfoId) {
        return dataNodeStatus.getStatus() != LocalServerStatusEnum.WORKING;
    }

    /**
     * @see ForwardService#forwardRequest(String, Object)
     */
    @Override
    public Object forwardRequest(String dataInfoId, Object request) throws RemotingException {
        try {
            // 1. get store nodes
            List<DataServerNode> dataServerNodes = DataServerNodeFactory.computeDataServerNodes(
                dataServerBootstrapConfig.getLocalDataCenter(), dataInfoId,
                dataServerBootstrapConfig.getStoreNodes());

            // 2. find next node
            if (null == dataServerNodes || dataServerNodes.size() <= 0) {
                throw new RuntimeException("No available server to forward");
            }

            boolean next = false;
            String localIp = NetUtil.getLocalAddress().getHostAddress();
            DataServerNode nextNode = null;
            for (DataServerNode dataServerNode : dataServerNodes) {
                if (next) {
                    nextNode = dataServerNode;
                    break;
                }
                if (null != localIp && localIp.equals(dataServerNode.getIp())) {
                    next = true;
                }
            }

            if (null == nextNode || null == nextNode.getConnection()) {
                throw new RuntimeException("No available connection to forward");
            }

            LOGGER.info("[forward] target: {}, dataInfoId: {}, request: {}, allNodes: {}",
                nextNode.getIp(), dataInfoId, request, dataServerNodes);

            // 3. invoke and return result
            final DataServerNode finalNextNode = nextNode;
            return dataNodeExchanger.request(new Request() {
                @Override
                public Object getRequestBody() {
                    return request;
                }

                @Override
                public URL getRequestUrl() {
                    return new URL(finalNextNode.getConnection().getRemoteIP(), finalNextNode
                        .getConnection().getRemotePort());
                }
            }).getResult();
        } catch (Exception e) {
            throw new RemotingException("ForwardServiceImpl forwardRequest error", e);
        }
    }
}
