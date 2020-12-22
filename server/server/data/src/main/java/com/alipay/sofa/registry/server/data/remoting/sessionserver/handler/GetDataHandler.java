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

import com.alipay.sofa.registry.common.model.dataserver.GetDataRequest;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * processor to get specific data
 *
 * @author qian.lqlq
 * @version $Id: GetDataProcessor.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class GetDataHandler extends AbstractDataHandler<GetDataRequest> {

    @Autowired
    private DatumCache         datumCache;

    @Autowired
    private ThreadPoolExecutor getDataProcessorExecutor;

    @Override
    public Executor getExecutor() {
        return getDataProcessorExecutor;
    }

    @Override
    public void checkParam(GetDataRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "GetDataRequest.dataInfoId");
        checkSessionProcessId(request.getSessionProcessId());
    }

    @Override
    public Object doHandle(Channel channel, GetDataRequest request) {
        processSessionProcessId(channel, request.getSessionProcessId());

        String dataInfoId = request.getDataInfoId();
        final SlotAccess slotAccess = checkAccess(dataInfoId, request.getSlotTableEpoch());
        if (!slotAccess.isAccept()) {
            return SlotAccessGenericResponse.failedResponse(slotAccess);
        }

        return SlotAccessGenericResponse.successResponse(slotAccess,
            datumCache.getDatumGroupByDataCenter(request.getDataCenter(), dataInfoId));
    }

    @Override
    public Class interest() {
        return GetDataRequest.class;
    }
}
