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

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.ProcessIdCache;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumStorage;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.lease.SessionLeaseManager;
import com.alipay.sofa.registry.server.data.slot.SlotAccessor;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-04 14:59 yuzhi.lyz Exp $
 */
public abstract class AbstractDataHandler<T> extends AbstractServerHandler<T> {
  private static final Logger LOGGER_SLOT_ACCESS = LoggerFactory.getLogger("SLOT-ACCESS");

  private final Set<StoreData.DataType> DATA_TYPES =
      Collections.unmodifiableSet(
          Sets.newHashSet(StoreData.DataType.PUBLISHER, StoreData.DataType.UN_PUBLISHER));

  @Autowired protected DataChangeEventCenter dataChangeEventCenter;

  @Autowired protected DataServerConfig dataServerConfig;

  @Autowired protected SlotAccessor slotAccessor;

  @Resource protected DatumStorage datumStorageDelegate;

  @Autowired protected SessionLeaseManager sessionLeaseManager;

  protected void checkPublisher(Publisher publisher) {
    ParaCheckUtil.checkNotNull(publisher, "publisher");
    ParaCheckUtil.checkNotBlank(publisher.getDataId(), "publisher.dataId");
    ParaCheckUtil.checkNotBlank(publisher.getInstanceId(), "publisher.instanceId");
    ParaCheckUtil.checkNotBlank(publisher.getGroup(), "publisher.group");
    ParaCheckUtil.checkNotBlank(publisher.getDataInfoId(), "publisher.dataInfoId");
    ParaCheckUtil.checkNotNull(publisher.getVersion(), "publisher.version");
    ParaCheckUtil.checkNotBlank(publisher.getRegisterId(), "publisher.registerId");
    if (publisher.getPublishType() != PublishType.TEMPORARY
        && publisher.getDataType() == StoreData.DataType.PUBLISHER) {
      ParaCheckUtil.checkNotNull(publisher.getSourceAddress(), "publisher.sourceAddress");
    }
    ParaCheckUtil.checkContains(DATA_TYPES, publisher.getDataType(), "publisher.dataType");
  }

  protected void checkSessionProcessId(ProcessId sessionProcessId) {
    ParaCheckUtil.checkNotNull(sessionProcessId, "request.sessionProcessId");
  }

  protected SlotAccess checkAccess(
      String dataCenter, String dataInfoId, long slotTableEpoch, long slotLeaderEpoch) {
    final int slotId = slotAccessor.slotOf(dataInfoId);
    return checkAccess(dataCenter, slotId, slotTableEpoch, slotLeaderEpoch);
  }

  protected SlotAccess checkAccess(
      String dataCenter, int slotId, long slotTableEpoch, long slotLeaderEpoch) {
    final SlotAccess slotAccess =
        slotAccessor.checkSlotAccess(dataCenter, slotId, slotTableEpoch, slotLeaderEpoch);
    if (slotAccess.isMoved()) {
      LOGGER_SLOT_ACCESS.warn(
          "[moved]{}, dataCenter={}, leaderEpoch={}, tableEpoch={}",
          slotAccess,
          dataCenter,
          slotLeaderEpoch,
          slotTableEpoch);
    }

    if (slotAccess.isMigrating()) {
      LOGGER_SLOT_ACCESS.warn(
          "[migrating]{}, dataCenter={}, leaderEpoch={}, tableEpoch={}",
          slotAccess,
          dataCenter,
          slotLeaderEpoch,
          slotTableEpoch);
    }
    if (slotAccess.isMisMatch()) {
      LOGGER_SLOT_ACCESS.warn(
          "[mismatch]{}, dataCenter={}, leaderEpoch={}, tableEpoch={}",
          slotAccess,
          dataCenter,
          slotLeaderEpoch,
          slotTableEpoch);
    }
    return slotAccess;
  }

  protected void processSessionProcessId(Channel channel, ProcessId sessionProcessId) {
    // the channel is null when caller is xxx-Resource
    if (channel != null) {
      // bind the processId with the conn
      sessionProcessId = ProcessIdCache.cache(sessionProcessId);
      final Connection conn = ((BoltChannel) channel).getConnection();
      conn.setAttributeIfAbsent(ValueConstants.ATTR_RPC_CHANNEL_PROCESS_ID, sessionProcessId);
    }
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

  @Override
  protected void logRequest(Channel channel, T request) {
    // too much to log
  }
}
