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

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.GetDataRequest;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.util.ParaCheckUtil;

import static com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.HandlerMetrics.GetData.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * processor to get specific data
 *
 * @author qian.lqlq
 * @version $Id: GetDataProcessor.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class GetDataHandler extends AbstractDataHandler<GetDataRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger("GET");
    @Autowired
    private DatumCache          datumCache;

    @Autowired
    private ThreadPoolExecutor  getDataProcessorExecutor;

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

        final String dataInfoId = request.getDataInfoId();

        final SlotAccess slotAccessBefore = checkAccess(dataInfoId, request.getSlotTableEpoch(),
            request.getSlotLeaderEpoch());
        if (!slotAccessBefore.isAccept()) {
            GET_DATUM_N_COUNTER.inc();
            return SlotAccessGenericResponse.failedResponse(slotAccessBefore);
        }
        Map<String, Datum> datumMap = datumCache.getDatumGroupByDataCenter(request.getDataCenter(),
            dataInfoId);
        // important. double check the slot access. avoid the case:
        // 1. the slot is leader, the first check pass
        // 2. slot moved and data cleaned
        // 3. get datum, but null after cleaned, dangerous!!
        // 3.1. session get datum by change.version, ignored null datum, would not push
        // 3.2. session get datum by subscriber.register, accept null datum(the pub may not exists) and push empty
        // so, need a double check slot access, make sure the slot's leader not change in the getting
        final SlotAccess slotAccessAfter = checkAccess(dataInfoId, request.getSlotTableEpoch(),
            request.getSlotLeaderEpoch());
        if (slotAccessAfter.getSlotLeaderEpoch() != slotAccessBefore.getSlotLeaderEpoch()) {
            // the slot's leader has change
            GET_DATUM_N_COUNTER.inc();
            return SlotAccessGenericResponse.failedResponse(slotAccessAfter,
                "slotLeaderEpoch has change, prev=" + slotAccessBefore);
        }

        final String localDataCenter = dataServerConfig.getLocalDataCenter();
        final Datum localDatum = datumMap.get(localDataCenter);
        GET_DATUM_Y_COUNTER.inc();
        if (localDatum != null) {
            LOGGER.info("getD,{},{},{},{}", dataInfoId, localDataCenter,
                localDatum.publisherSize(), localDatum.getVersion());
            GET_PUBLISHER_COUNTER.inc(localDatum.publisherSize());
        } else {
            LOGGER.info("getDNil,{},{}", dataInfoId, localDataCenter);
        }
        return SlotAccessGenericResponse.successResponse(slotAccessAfter, datumMap);
    }

    @Override
    protected void logRequest(Channel channel, GetDataRequest request) {
        // not log
    }

    @Override
    public Class interest() {
        return GetDataRequest.class;
    }
}
