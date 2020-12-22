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
import com.alipay.sofa.registry.common.model.dataserver.UnPublishDataRequest;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.cache.UnPublisher;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * processor to unPublish specific data
 *
 * @author qian.lqlq
 * @version $Id: UnPublishDataProcessor.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class UnPublishDataHandler extends AbstractDataHandler<UnPublishDataRequest> {

    @Autowired
    private ThreadPoolExecutor publishProcessorExecutor;

    @Override
    public Executor getExecutor() {
        return publishProcessorExecutor;
    }

    @Override
    public void checkParam(UnPublishDataRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "UnPublishDataRequest.dataInfoId");
        ParaCheckUtil.checkNotBlank(request.getRegisterId(), "UnPublishDataRequest.registerId");
        checkSessionProcessId(request.getSessionProcessId());
    }

    @Override
    public Object doHandle(Channel channel, UnPublishDataRequest request) {
        processSessionProcessId(channel, request.getSessionProcessId());

        UnPublisher publisher = new UnPublisher(request.getDataInfoId(),
            request.getSessionProcessId(), request.getRegisterId(), request.getRegisterTimestamp(),
            request.getVersion());

        Publisher.internPublisher(publisher);

        final SlotAccess slotAccess = checkAccess(publisher.getDataInfoId(),
            request.getSlotTableEpoch());
        if (slotAccess.isMoved()) {
            return SlotAccessGenericResponse.failedResponse(slotAccess);
        }
        DatumVersion version = localDatumStorage.putPublisher(publisher,
            request.getSessionProcessId());
        if (version != null) {
            dataChangeEventCenter.onChange(publisher.getDataInfoId(),
                dataServerConfig.getLocalDataCenter());
        }
        return SlotAccessGenericResponse.successResponse(slotAccess, null);
    }

    @Override
    public Class interest() {
        return UnPublishDataRequest.class;
    }

}
