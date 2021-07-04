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

import com.alipay.sofa.registry.common.model.slot.GetSlotTableStatusRequest;
import com.alipay.sofa.registry.common.model.slot.SlotTableStatusResponse;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.slot.status.SlotTableStatusService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: GetSlotTableStatusRequestHandler.java, v 0.1 2021年07月05日 9:35 PM xiaojian.xj Exp $
 */
public class GetSlotTableStatusRequestHandler
    extends BaseMetaServerHandler<GetSlotTableStatusRequest> {

  @Autowired SlotTableStatusService slotTableStatusService;

  @Autowired MetaLeaderService metaLeaderService;

  @Override
  public Object doHandle(Channel channel, GetSlotTableStatusRequest request) {

    if (!metaLeaderService.amIStableAsLeader()) {
      return null;
    }

    SlotTableStatusResponse slotTableStatus = slotTableStatusService.getSlotTableStatus();
    return slotTableStatus;
  }

  @Override
  public Class interest() {
    return GetSlotTableStatusRequest.class;
  }
}
