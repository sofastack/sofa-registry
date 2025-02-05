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

import com.alipay.sofa.registry.common.model.dataserver.GetDataRequest;
import com.alipay.sofa.registry.common.model.slot.SlotAccessGenericResponse;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.util.ParaCheckUtil;

/**
 * processor to get specific data
 *
 * @author qian.lqlq
 * @version $Id: GetDataProcessor.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class GetDataHandler extends BaseGetDataHandler<GetDataRequest> {

  @Override
  public void checkParam(GetDataRequest request) {
    ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "GetDataRequest.dataInfoId");
    ParaCheckUtil.checkNotBlank(request.getDataCenter(), "GetDataRequest.dataCenter");
    checkSessionProcessId(request.getSessionProcessId());
  }

  @Override
  public SlotAccessGenericResponse<SubDatum> doHandle(Channel channel, GetDataRequest request) {
    processSessionProcessId(channel, request.getSessionProcessId());

    return processSingleDataCenter(
        request.getDataCenter(),
        request.getDataInfoId(),
        request.getSlotTableEpoch(),
        request.getSlotLeaderEpoch(),
        request.getAcceptEncodes());
  }

  @Override
  public Class interest() {
    return GetDataRequest.class;
  }
}
