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
import com.alipay.sofa.registry.common.model.dataserver.GetMultiDataRequest;
import com.alipay.sofa.registry.common.model.slot.MultiSlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.MultiSubDatum;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author xiaojian.xj
 * @version : MultiGetDataHandler.java, v 0.1 2022年06月20日 16:32 xiaojian.xj Exp $
 */
public class GetMultiDataHandler extends BaseGetDataHandler<GetMultiDataRequest> {

  @Override
  public void checkParam(GetMultiDataRequest request) {
    ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "GetMultiDataRequest.dataInfoId");
    ParaCheckUtil.checkNotEmpty(
        request.getSlotTableEpochs(), "GetMultiDataRequest.slotTableEpochs");
    ParaCheckUtil.checkNotEmpty(
        request.getSlotLeaderEpochs(), "GetMultiDataRequest.slotLeaderEpochs");

    for (Entry<String, Long> entry : request.getSlotTableEpochs().entrySet()) {
      String dataCenter = entry.getKey();
      ParaCheckUtil.checkNotNull(entry.getValue(), dataCenter + ".slotTableEpoch");
      ParaCheckUtil.checkNotNull(
          request.getSlotLeaderEpochs().get(dataCenter), dataCenter + ".slotLeaderEpoch");
    }

    checkSessionProcessId(request.getSessionProcessId());
  }

  /**
   * return processor request class name
   *
   * @return Class
   */
  @Override
  public Class interest() {
    return GetMultiDataRequest.class;
  }

  /**
   * execute
   *
   * @param channel channel
   * @param request request
   * @return MultiSlotAccessGenericResponse
   */
  @Override
  public MultiSlotAccessGenericResponse<MultiSubDatum> doHandle(
      Channel channel, GetMultiDataRequest request) {
    processSessionProcessId(channel, request.getSessionProcessId());

    int dataCenterSize = request.getSlotLeaderEpochs().size();

    boolean success = true;
    StringBuilder builder = new StringBuilder();
    Map<String, SlotAccess> slotAccessMap = Maps.newHashMapWithExpectedSize(dataCenterSize);
    Map<String, SubDatum> datumMap = Maps.newHashMapWithExpectedSize(dataCenterSize);
    for (Entry<String, Long> entry : request.getSlotTableEpochs().entrySet()) {
      String dataCenter = entry.getKey();
      SlotAccessGenericResponse<SubDatum> res =
          processSingleDataCenter(
              dataCenter,
              request.getDataInfoId(),
              entry.getValue(),
              request.getSlotLeaderEpochs().get(dataCenter),
              request.getAcceptEncodes());

      if (!res.isSuccess()) {
        success = false;
        builder.append(StringFormatter.format("{}:{}.", dataCenter, res.getMessage()));
      }
      slotAccessMap.put(dataCenter, res.getSlotAccess());
      datumMap.put(dataCenter, res.getData());
    }
    MultiSubDatum data = new MultiSubDatum(request.getDataInfoId(), datumMap);

    return new MultiSlotAccessGenericResponse(success, builder.toString(), data, slotAccessMap);
  }

  @Override
  public CommonResponse buildFailedResponse(String msg) {
    return MultiSlotAccessGenericResponse.failedResponse(msg);
  }
}
