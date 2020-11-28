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
package com.alipay.sofa.registry.server.shared.meta;

import com.alipay.remoting.Connection;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.FetchProvideDataRequest;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.RenewNodesRequest;
import com.alipay.sofa.registry.common.model.metaserver.inter.communicate.BaseHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.inter.communicate.DataHeartBeatResponse;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-28 15:21 yuzhi.lyz Exp $
 */
public abstract class AbstractMetaServerService implements MetaServerService {
    private static final Logger         LOGGER = LoggerFactory
                                                   .getLogger(AbstractMetaServerService.class);

    @Autowired
    protected AbstractMetaNodeExchanger metaNodeExchanger;

    @Override
    public void renewNode() {
        final String leaderIp = getLeader().getIp();
        try {
            RenewNodesRequest<DataNode> renewNodesRequest = new RenewNodesRequest<>(new DataNode(
                new URL(ServerEnv.IP), metaNodeExchanger.getLocalDataCenter()));
            GenericResponse<DataHeartBeatResponse> resp = (GenericResponse<DataHeartBeatResponse>) metaNodeExchanger
                .request(new Request() {
                    @Override
                    public Object getRequestBody() {
                        return renewNodesRequest;
                    }

                    @Override
                    public URL getRequestUrl() {
                        return new URL(leaderIp, metaNodeExchanger.getMetaServerPort());
                    }
                }).getResult();
            if (resp != null && resp.isSuccess()) {
                handleRenewResult(resp.getData());
            } else {
                LOGGER.error("[RenewNodeTask] renew data node to metaServer error : {}, {}",
                    leaderIp, resp);
                throw new RuntimeException("[RenewNodeTask] renew data node to metaServer error : "
                                           + leaderIp);
            }
        } catch (Throwable e) {
            LOGGER.error("renew node error from {}", leaderIp, e);
            throw new RuntimeException("renew node error! " + e.getMessage(), e);
        }
    }

    @Override
    public ProvideData fetchData(String dataInfoId) {
        final String leaderIp = getLeader().getIp();
        try {
            Request<FetchProvideDataRequest> request = new Request<FetchProvideDataRequest>() {
                @Override
                public FetchProvideDataRequest getRequestBody() {
                    return new FetchProvideDataRequest(dataInfoId);
                }

                @Override
                public URL getRequestUrl() {
                    return new URL(leaderIp, metaNodeExchanger.getMetaServerPort());
                }
            };

            Response response = metaNodeExchanger.request(request);

            Object result = response.getResult();
            if (result instanceof ProvideData) {
                return (ProvideData) result;
            } else {
                LOGGER.error("fetch null provider data from {}", leaderIp);
                throw new RuntimeException("MetaNodeService fetch null provider data!");
            }
        } catch (Exception e) {
            LOGGER.error("fetch provider data error from {}", leaderIp, e);
            throw new RuntimeException("fetch provider data error! " + e.getMessage(), e);
        }

    }

    public Map<String, List<Connection>> getConnections() {
        return metaNodeExchanger.getConnections();
    }

    public void updateMetaIps(Collection<String> ips) {
        this.metaNodeExchanger.updateMetaIps(ips);
    }

    public void startRaftClient() {
        metaNodeExchanger.startRaftClient();
    }

    public PeerId getLeader() {
        return metaNodeExchanger.getLeader();
    }

    protected abstract void handleRenewResult(BaseHeartBeatResponse result);
}
