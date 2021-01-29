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

import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.dataserver.BatchRequest;
import com.alipay.sofa.registry.common.model.dataserver.ClientOffRequest;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.UnPublisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class BatchPutDataHandler extends AbstractDataHandler<BatchRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger("PUT");
    @Autowired
    private ThreadPoolExecutor  publishProcessorExecutor;

    @Override
    public void checkParam(BatchRequest request) throws RuntimeException {
        checkSessionProcessId(request.getSessionProcessId());
        for (Object req : request.getRequest()) {
            if (req instanceof Publisher) {
                checkPublisher((Publisher) req);
            } else if (req instanceof ClientOffRequest) {
                ParaCheckUtil.checkNotNull(((ClientOffRequest) req).getConnectId(),
                    "ClientOffRequest.connectIds");
                ParaCheckUtil.checkNotNull(((ClientOffRequest) req).getPublisherMap(),
                    "ClientOffRequest.publisherMap");
            } else {
                throw new IllegalArgumentException("unsupported item in batch:" + req);
            }
        }
    }

    @Override
    public Object doHandle(Channel channel, BatchRequest request) {
        processSessionProcessId(channel, request.getSessionProcessId());
        final SlotAccess slotAccess = checkAccess(request.getSlotId(), request.getSlotTableEpoch());
        if (slotAccess.isMoved()) {
            // only reject the when moved
            return SlotAccessGenericResponse.failedResponse(slotAccess);
        }
        final String slotIdStr = String.valueOf(request.getSlotId());
        final Set<String> changeDataInfoIds = new HashSet<>(64);
        try {
            for (Object req : request.getRequest()) {
                // contains publisher and unPublisher
                if (req instanceof Publisher) {
                    Publisher publisher = (Publisher) req;
                    changeDataInfoIds.addAll(doHandle(publisher));
                    if (publisher instanceof UnPublisher) {
                        LOGGER.info("unpub,{},{},{},{},{}", slotIdStr, publisher.getDataInfoId(),
                            publisher.getRegisterId(), publisher.getVersion(),
                            publisher.getRegisterTimestamp());
                    } else {
                        LOGGER.info("pub,{},{},{},{}", slotIdStr, publisher.getDataInfoId(),
                            publisher.getRegisterId(), publisher.getVersion(),
                            publisher.getRegisterTimestamp());
                    }
                } else if (req instanceof ClientOffRequest) {
                    ClientOffRequest clientOff = (ClientOffRequest) req;
                    changeDataInfoIds.addAll(doHandle(clientOff));
                    for (Map.Entry<String, Map<String, RegisterVersion>> e : clientOff
                        .getPublisherMap().entrySet()) {
                        final String dataInfoId = e.getKey();
                        for (Map.Entry<String, RegisterVersion> ver : e.getValue().entrySet()) {
                            RegisterVersion version = ver.getValue();
                            LOGGER.info("off,{},{},{},{}", slotIdStr, dataInfoId, ver.getKey(),
                                version.getVersion(), version.getRegisterTimestamp());
                        }
                    }
                } else {
                    throw new IllegalArgumentException("unsupported item in batch:" + req);
                }
            }
        } finally {
            // if has exception, try to notify the req which was handled
            if (!changeDataInfoIds.isEmpty()) {
                dataChangeEventCenter.onChange(changeDataInfoIds,
                    dataServerConfig.getLocalDataCenter());
            }
        }

        return SlotAccessGenericResponse.successResponse(slotAccess, null);
    }

    private List<String> doHandle(Publisher publisher) {
        publisher = Publisher.internPublisher(publisher);
        if (publisher.getPublishType() == PublishType.TEMPORARY) {
            // create datum for the temp publisher, we need the datum.version for check ver
            localDatumStorage.createEmptyDatumIfAbsent(publisher.getDataInfoId(),
                dataServerConfig.getLocalDataCenter());
            // temporary only notify session, not store
            dataChangeEventCenter.onTempPubChange(publisher, dataServerConfig.getLocalDataCenter());
        } else {
            DatumVersion version = localDatumStorage.putPublisher(publisher);
            if (version != null) {
                return Collections.singletonList(publisher.getDataInfoId());
            }
        }
        return Collections.emptyList();
    }

    public List<String> doHandle(ClientOffRequest request) {
        Map<String, Map<String, RegisterVersion>> publisherMap = request.getPublisherMap();
        List<String> dataInfoIds = new ArrayList<>(publisherMap.size());
        for (Map.Entry<String, Map<String, RegisterVersion>> e : publisherMap.entrySet()) {
            DatumVersion version = localDatumStorage.remove(e.getKey(),
                request.getSessionProcessId(), e.getValue());
            if (version != null) {
                dataInfoIds.add(e.getKey());
            }
        }
        return dataInfoIds;
    }

    @Override
    public Class interest() {
        return BatchRequest.class;
    }

    @Override
    public Executor getExecutor() {
        return publishProcessorExecutor;
    }

}
