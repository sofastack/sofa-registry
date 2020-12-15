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
package com.alipay.sofa.registry.server.data.remoting.sessionserver.handler;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.GetDataVersionRequest;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * processor to get versions of specific dataInfoIds
 *
 * @author qian.lqlq
 * @version $Id: GetDataVersionsProcessor.java, v 0.1 2017-12-06 19:56 qian.lqlq Exp $
 */
public class GetDataVersionsHandler extends AbstractServerHandler<GetDataVersionRequest> {

    @Autowired
    private DatumCache                       datumCache;

    @Autowired
    private ThreadPoolExecutor               getDataProcessorExecutor;

    @Autowired
    private SessionLeaseManager              sessionLeaseManager;

    @Autowired
    protected SessionServerConnectionFactory sessionServerConnectionFactory;

    @Override
    public Executor getExecutor() {
        return getDataProcessorExecutor;
    }

    @Override
    public void checkParam(GetDataVersionRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotEmpty(request.getDataInfoIds(), "GetDataVersionRequest.dataInfoIds");
        ParaCheckUtil.checkNotNull(request.getSessionProcessId(), "request.sessionProcessId");
    }

    @Override
    public Object doHandle(Channel channel, GetDataVersionRequest request) {
        sessionLeaseManager.renewSession(request.getSessionProcessId());
        sessionServerConnectionFactory.registerSession(request.getSessionProcessId(), channel);

        Map<String/*datacenter*/, Map<String/*dataInfoId*/, Long/*version*/>> map = new HashMap<>();
        List<String> dataInfoIds = request.getDataInfoIds();
        for (String dataInfoId : dataInfoIds) {
            Map<String, Long> datumMap = datumCache.getVersions(dataInfoId);
            Set<Entry<String, Long>> entrySet = datumMap.entrySet();
            for (Entry<String, Long> entry : entrySet) {
                String dataCenter = entry.getKey();
                Long version = entry.getValue();
                Map<String, Long> dataInfoIdToVersionMap = map.computeIfAbsent(dataCenter, k -> Maps.newHashMap());
                dataInfoIdToVersionMap.put(dataInfoId, version);
            }
        }
        return new GenericResponse<Map<String, Map<String, Long>>>().fillSucceed(map);
    }

    @Override
    public GenericResponse<Map<String, Map<String, Long>>> buildFailedResponse(String msg) {
        return new GenericResponse<Map<String, Map<String, Long>>>().fillFailed(msg);
    }

    @Override
    public Class interest() {
        return GetDataVersionRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.SESSION;
    }
}