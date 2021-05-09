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
package com.alipay.sofa.registry.server.shared.remoting;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.SlotTableChangeEvent;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Feb 24, 2021
 */
public class SlotTableChangeEventHandler extends AbstractClientHandler<SlotTableChangeEvent> {

  @Autowired MetaServerService metaServerService;

  @Autowired ThreadPoolExecutor metaNodeExecutor;

  @Override
  protected Node.NodeType getConnectNodeType() {
    return Node.NodeType.META;
  }

  @Override
  public void checkParam(SlotTableChangeEvent request) {
    ParaCheckUtil.checkNotNull(request, "SlotTableChangeEvent");
    super.checkParam(request);
    ParaCheckUtil.checkIsPositive(
        request.getSlotTableEpoch(), "SlotTableChangeEvent.slotTableEpoch");
  }

  @Override
  public Object doHandle(Channel channel, SlotTableChangeEvent request) {
    boolean result = metaServerService.handleSlotTableChange(request);
    if (result) {
      return CommonResponse.buildSuccessResponse("successfully triggered slot-table retrieval");
    } else {
      return CommonResponse.buildFailedResponse(
          "won't update slot-table, check [AbstractMetaServerService] log");
    }
  }

  @Override
  public Object buildFailedResponse(String msg) {
    return CommonResponse.buildFailedResponse(msg);
  }

  @Override
  public Class interest() {
    return SlotTableChangeEvent.class;
  }

  @Override
  public Executor getExecutor() {
    return metaNodeExecutor;
  }
}
