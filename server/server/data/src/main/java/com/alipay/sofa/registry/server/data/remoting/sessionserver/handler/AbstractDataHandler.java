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

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Set;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-04 14:59 yuzhi.lyz Exp $
 */
public abstract class AbstractDataHandler<T> extends AbstractServerHandler<T> {
    protected static final Logger            LOGGER             = LoggerFactory
                                                                    .getLogger("DATA-ACCESS");
    protected static final Logger            LOGGER_SLOT_ACCESS = LoggerFactory
                                                                    .getLogger("SLOT-ACCESS");

    private final Set<StoreData.DataType>    DATA_TTPES         = Collections
                                                                    .unmodifiableSet(Sets
                                                                        .newHashSet(
                                                                            StoreData.DataType.PUBLISHER,
                                                                            StoreData.DataType.UN_PUBLISHER));

    @Autowired
    protected DataChangeEventCenter          dataChangeEventCenter;

    @Autowired
    protected DataServerConfig               dataServerConfig;

    @Autowired
    protected SlotManager                    slotManager;

    @Autowired
    protected DatumStorage                   localDatumStorage;

    @Autowired
    protected SessionLeaseManager            sessionLeaseManager;

    @Autowired
    protected SessionServerConnectionFactory sessionServerConnectionFactory;

    protected void checkPublisher(Publisher publisher) {
        ParaCheckUtil.checkNotNull(publisher, "publisher");
        ParaCheckUtil.checkNotBlank(publisher.getDataId(), "publisher.dataId");
        ParaCheckUtil.checkNotBlank(publisher.getInstanceId(), "publisher.instanceId");
        ParaCheckUtil.checkNotBlank(publisher.getGroup(), "publisher.group");
        ParaCheckUtil.checkNotBlank(publisher.getDataInfoId(), "publisher.dataInfoId");
        ParaCheckUtil.checkNotNull(publisher.getVersion(), "publisher.version");
        ParaCheckUtil.checkNotBlank(publisher.getRegisterId(), "publisher.registerId");
        if (publisher.getPublishType() != PublishType.TEMPORARY) {
            ParaCheckUtil.checkNotNull(publisher.getSourceAddress(), "publisher.sourceAddress");
        }
        ParaCheckUtil.checkContains(DATA_TTPES, publisher.getDataType(), "publisher.dataType");
    }

    protected void checkSessionProcessId(ProcessId sessionProcessId) {
        ParaCheckUtil.checkNotNull(sessionProcessId, "request.sessionProcessId");
    }

    protected SlotAccess checkAccess(String dataInfoId, long slotTableEpoch) {
        final SlotAccess slotAccess = slotManager.checkSlotAccess(dataInfoId, slotTableEpoch);
        if (slotAccess.isMoved()) {
            LOGGER_SLOT_ACCESS
                .warn("[moved] Slot has moved, {} access: {}", dataInfoId, slotAccess);
        }

        if (slotAccess.isMigrating()) {
            LOGGER_SLOT_ACCESS.warn("[migrating] Slot is migrating, {} access: {}", dataInfoId,
                slotAccess);
        }
        return slotAccess;
    }

    protected void processSessionProcessId(Channel channel, ProcessId sessionProcessId) {
        sessionServerConnectionFactory.registerSession(sessionProcessId, channel);
        sessionLeaseManager.renewSession(sessionProcessId);
    }

    @Override
    public CommonResponse buildFailedResponse(String msg) {
        return SlotAccessGenericResponse.failedResponse(msg);
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.SESSION;
    }

}
