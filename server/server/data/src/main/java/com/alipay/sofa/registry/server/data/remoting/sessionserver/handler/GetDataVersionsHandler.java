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

import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.dataserver.GetDataVersionRequest;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.util.ParaCheckUtil;

import static com.alipay.sofa.registry.server.data.remoting.sessionserver.handler.HandlerMetrics.GetVersion.*;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * processor to get versions of specific dataInfoIds
 *
 * @author qian.lqlq
 * @version $Id: GetDataVersionsProcessor.java, v 0.1 2017-12-06 19:56 qian.lqlq Exp $
 */
public class GetDataVersionsHandler extends AbstractDataHandler<GetDataVersionRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger("GET");
    @Autowired
    private DatumCache          datumCache;

    @Autowired
    private DatumStorage        localDatumStorage;

    @Autowired
    private ThreadPoolExecutor  getDataProcessorExecutor;

    @Override
    public Executor getExecutor() {
        return getDataProcessorExecutor;
    }

    @Override
    public void checkParam(GetDataVersionRequest request) throws RuntimeException {
        ParaCheckUtil.checkNonNegative(request.getSlotId(), "GetDataVersionRequest.slotId");
        ParaCheckUtil.checkNotBlank(request.getDataCenter(), "GetDataVersionRequest.dataCenter");
        checkSessionProcessId(request.getSessionProcessId());
    }

    @Override
    public Object doHandle(Channel channel, GetDataVersionRequest request) {
        processSessionProcessId(channel, request.getSessionProcessId());
        final int slotId = request.getSlotId();
        final String dataCenter = request.getDataCenter();
        final SlotAccess slotAccessBefore = checkAccess(slotId, request.getSlotTableEpoch(),
            request.getSlotLeaderEpoch());
        if (!slotAccessBefore.isAccept()) {
            return SlotAccessGenericResponse.failedResponse(slotAccessBefore);
        }
        Map<String/*dataInfoId*/, DatumVersion> getVersions = datumCache.getVersions(dataCenter,
            slotId);
        // double check slot access, @see GetDataHandler
        final SlotAccess slotAccessAfter = checkAccess(slotId, request.getSlotTableEpoch(),
            request.getSlotLeaderEpoch());
        if (slotAccessAfter.getSlotLeaderEpoch() != slotAccessBefore.getSlotLeaderEpoch()) {
            return SlotAccessGenericResponse.failedResponse(slotAccessAfter,
                "slotLeaderEpoch has change, prev=" + slotAccessBefore);
        }
        final boolean localDataCenter = dataServerConfig.isLocalDataCenter(dataCenter);
        Map<String, DatumVersion> ret = Maps.newHashMapWithExpectedSize(request.getInterests()
            .size());
        for (Map.Entry<String, DatumVersion> e : request.getInterests().entrySet()) {
            final String dataInfoId = e.getKey();
            final DatumVersion interestVer = e.getValue();
            final DatumVersion currentVer = getVersions.get(dataInfoId);
            // contains the datum which is interested
            if (currentVer != null) {
                ret.put(dataInfoId, currentVer);
                if (interestVer.getValue() > currentVer.getValue()) {
                    // the session.push version is bigger than datum.version. this may happens:
                    // 1. slot-1 own by data-A, balance slot-1, migrating from data-A to data-B.
                    //    this need a very short time window to broadcast the information. e.g. a heartbeat interval time
                    // 2.1. part of session/data has not updated the slotTable. [session-A, data-A]
                    // 2.2. another part of the session/data has updated the slotTable. [session-B, data-B]
                    // 3. data-B finish migrating and session-B put publisher-B to data-B gen a new datum.version=V1
                    // 4. session-A put publisher-A to data-A, gen a new datum.version=V2(V1<V2), and push V2 to subscriber-A
                    // 5. session-A update the slotTable, connect to data-B, but the subscriber-A(push.version)=V2 is
                    //    bigger than current datum.version=V1, the publisher-B would not push to subscriber-A.
                    // so, we need to compare the push.version and datum.version
                    DatumVersion updateVer = datumCache.updateVersion(dataCenter, dataInfoId);
                    LOGGER.info("updateV,{},{},{},interestVer={},currentVer={},updateVer={}",
                        slotId, dataInfoId, dataCenter, interestVer, currentVer, updateVer);
                }
            } else {
                if (localDataCenter) {
                    // no datum in data node, this maybe happens an empty datum occurs migrating
                    // there is subscriber subs the dataId. we create a empty datum to trace the version
                    final DatumVersion v = localDatumStorage.createEmptyDatumIfAbsent(dataInfoId,
                        dataCenter);
                    if (v != null) {
                        getVersions.put(dataInfoId, v);
                    }
                    LOGGER.info("createV,{},{},{},interestVer={},createV={}", slotId, dataInfoId,
                        dataCenter, interestVer, v);
                }
            }
        }
        LOGGER.info("getV,{},{},interests={},gets={},rets={}", slotId, dataCenter, request
            .getInterests().size(), getVersions.size(), ret.size());
        GET_VERSION_COUNTER.inc();
        return SlotAccessGenericResponse.successResponse(slotAccessAfter, ret);
    }

    @Override
    public Class interest() {
        return GetDataVersionRequest.class;
    }
}