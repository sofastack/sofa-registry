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

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.dataserver.PublishDataRequest;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;

/**
 * processor to publish data
 *
 * @author qian.lqlq
 * @version $Id: ForwardPublishDataProcessor.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class PublishDataHandler extends AbstractDataHandler<PublishDataRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishDataHandler.class);

    @Autowired
    private ThreadPoolExecutor  publishProcessorExecutor;

    @Override
    public void checkParam(PublishDataRequest request) throws RuntimeException {
        checkSessionProcessId(request.getSessionProcessId());
        Publisher publisher = request.getPublisher();
        checkPublisher(publisher);
    }

    @Override
    public Object doHandle(Channel channel, PublishDataRequest request) {
        processSessionProcessId(channel, request.getSessionProcessId());

        Publisher publisher = Publisher.internPublisher(request.getPublisher());

        final SlotAccess slotAccess = checkAccess(publisher.getDataInfoId(),
            request.getSlotTableEpoch());
        if (!slotAccess.isAccept()) {
            return SlotAccessGenericResponse.failedResponse(slotAccess);
        }
        if (publisher.getPublishType() == PublishType.TEMPORARY) {
            // temporary only notify session, not store
            dataChangeEventCenter.onTempPubChange(publisher, dataServerConfig.getLocalDataCenter());
        } else {
            DatumVersion version = localDatumStorage.putPublisher(publisher,
                request.getSessionProcessId());
            if (version != null) {
                dataChangeEventCenter.onChange(publisher.getDataInfoId(),
                    dataServerConfig.getLocalDataCenter());
            }
        }
        return SlotAccessGenericResponse.successResponse(slotAccess, null);
    }

    @Override
    public Class interest() {
        return PublishDataRequest.class;
    }

    @Override
    public Executor getExecutor() {
        return publishProcessorExecutor;
    }
}
