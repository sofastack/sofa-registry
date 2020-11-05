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

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.server.data.cache.SlotManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.GetDataRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;

/**
 * processor to get specific data
 *
 * @author qian.lqlq
 * @version $Id: GetDataProcessor.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class GetDataHandler extends AbstractServerHandler<GetDataRequest> {

    /** LOGGER */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetDataHandler.class);

    @Autowired
    private DatumCache          datumCache;

    @Autowired
    private ThreadPoolExecutor  getDataProcessorExecutor;

    @Autowired
    private SlotManager slotManager;

    @Override
    public Executor getExecutor() {
        return getDataProcessorExecutor;
    }

    @Override
    public void checkParam(GetDataRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "GetDataRequest.dataInfoId");
    }

    @Override
    public Object doHandle(Channel channel, GetDataRequest request) {
        String dataInfoId = request.getDataInfoId();
        final SlotAccess slotAccess = slotManager.checkSlotAccess(dataInfoId, request.getSlotEpoch());
        if (slotAccess.isMoved()) {
            LOGGER.warn("[moved] Slot has moved, access: {}, request: {}", slotAccess, request);
            return SlotAccessGenericResponse.buildFailedResponse(slotAccess);
        }

        if (slotAccess.isMigrating()) {
            LOGGER.warn("[migrating] Slot is migrating, access: {}, request: {}", slotAccess, request);
            return SlotAccessGenericResponse.buildFailedResponse(slotAccess);
        }

        return new GenericResponse<Map<String, Datum>>().fillSucceed(datumCache
            .getDatumGroupByDataCenter(request.getDataCenter(), dataInfoId));
    }

    @Override
    public GenericResponse<Map<String, Datum>> buildFailedResponse(String msg) {
        return new GenericResponse<Map<String, Datum>>().fillFailed(msg);
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return GetDataRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }

}
