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

import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.shared.remoting.ClientExchanger;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-28 15:36 yuzhi.lyz Exp $
 */
public abstract class AbstractMetaNodeExchanger extends ClientExchanger {
    private static final Logger        LOGGER = LoggerFactory
                                                  .getLogger(AbstractMetaNodeExchanger.class);

    @Resource(name = "metaClientHandlers")
    private Collection<ChannelHandler> metaClientHandlers;

    @Autowired
    private AbstractRaftClientManager  raftClientManager;

    protected AbstractMetaNodeExchanger() {
        super(Exchange.META_SERVER_TYPE);
    }

    public void startRaftClient() {
        this.serverIps = Sets.newHashSet(raftClientManager.getConfigMetaIp());
        raftClientManager.startRaftClient();
        connectServer();
    }

    @Override
    public Response request(Request request) throws RequestException {
        LOGGER.info("MetaNode Exchanger request={},url={},callbackHandler={}",
            request.getRequestBody(), request.getRequestUrl(), request.getCallBackHandler());
        try {
            return super.request(request);
        } catch (Throwable e) {
            //retry
            URL url = new URL(raftClientManager.refreshLeader().getIp(), getServerPort());
            LOGGER.warn(
                "MetaNode Exchanger request send error!It will be retry once!Request url:{}", url);
            return super.request(request);
        }
    }

    public PeerId getLeader() {
        return raftClientManager.getLeader();
    }

    public String getLocalDataCenter() {
        return raftClientManager.getLocalDataCenter();
    }

    @Override
    protected Collection<ChannelHandler> getClientHandlers() {
        return metaClientHandlers;
    }
}
