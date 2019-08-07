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
package com.alipay.sofa.registry.server.data.remoting.dataserver.handler;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.SyncData;
import com.alipay.sofa.registry.common.model.dataserver.SyncDataRequest;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.datasync.SyncDataService;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author shangyu.wh
 * @version $Id: SyncDataProcessor.java, v 0.1 2018-03-08 20:02 shangyu.wh Exp $
 */
public class SyncDataHandler extends AbstractServerHandler<SyncDataRequest> {

    @Autowired
    private SyncDataService syncDataService;

    @Override
    public void checkParam(SyncDataRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "request.dataInfoId");
        ParaCheckUtil.checkNotBlank(request.getDataCenter(), "request.dataCenter");
        ParaCheckUtil.checkNotBlank(String.valueOf(request.getVersion()), "request.currentVersion");
    }

    @Override
    public Object doHandle(Channel channel, SyncDataRequest request) {
        SyncData syncData = syncDataService.getSyncDataChange(request);
        return new GenericResponse<SyncData>().fillSucceed(syncData);
    }

    @Override
    public GenericResponse<SyncData> buildFailedResponse(String msg) {
        return new GenericResponse<SyncData>().fillFailed(msg);
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return SyncDataRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }
}