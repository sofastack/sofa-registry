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
package com.alipay.sofa.registry.server.meta.remoting.handler;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.GetSlotTableRequest;
import com.alipay.sofa.registry.common.model.metaserver.GetSlotTableResult;
import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-11 11:23 yuzhi.lyz Exp $
 */

// for cross-dc sync use only
public final class GetSlotTableRequestHandler extends BaseMetaServerHandler<GetSlotTableRequest> {

  private static final Logger LOGGER = LoggerFactory.getLogger("META-SLOT");

  @Autowired private CurrentDcMetaServer currentDcMetaServer;

  @Override
  public Object doHandle(Channel channel, GetSlotTableRequest getNodesRequest) {
    final long epochOfNode = getNodesRequest.getEpochOfNode();
    final SlotTable currentTable = currentDcMetaServer.getSlotTable();
    if (epochOfNode == currentTable.getEpoch()) {
      // not change, return null
      return new GenericResponse().fillSucceed(null);
    }
    if (epochOfNode > currentTable.getEpoch()) {
      // should not happen
      LOGGER.error(
          "[SLOT-EPOCH] meta's epoch={} should not less than node's epoch={}, {}",
          currentTable.getEpoch(),
          epochOfNode,
          channel.getRemoteAddress().getAddress().getHostAddress());
      return new GenericResponse().fillFailed("illegal epoch of node");
    }
    if (getNodesRequest.getTargetDataNode() == null) {
      return new GenericResponse<SlotTable>().fillSucceed(currentTable);
    }
    List<DataNodeSlot> dataNodeSlots =
        currentTable.transfer(
            getNodesRequest.getTargetDataNode(), getNodesRequest.isIgnoredFollowers());
    return new GenericResponse()
        .fillSucceed(new GetSlotTableResult(currentTable.getEpoch(), dataNodeSlots));
  }

  @Override
  public Class interest() {
    return GetSlotTableRequest.class;
  }
}
