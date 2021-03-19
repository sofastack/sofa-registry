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
package com.alipay.sofa.registry.server.session.converter.pb;

import com.alipay.sofa.registry.common.model.client.pb.SyncConfigRequestPb;
import com.alipay.sofa.registry.core.model.SyncConfigRequest;

/**
 * @author bystander
 * @version $Id: SyncConfigRequestConvertor.java, v 0.1 2018年03月21日 2:07 PM bystander Exp $
 */
public class SyncConfigRequestConvertor {

  public static SyncConfigRequest convert2Java(SyncConfigRequestPb syncConfigRequestPb) {

    if (syncConfigRequestPb == null) {
      return null;
    }

    SyncConfigRequest syncConfigRequest = new SyncConfigRequest();
    syncConfigRequest.setZone(syncConfigRequestPb.getZone());
    syncConfigRequest.setDataCenter(syncConfigRequestPb.getDataCenter());

    return syncConfigRequest;
  }

  public static SyncConfigRequestPb convert2Pb(SyncConfigRequest syncConfigRequestJava) {

    if (syncConfigRequestJava == null) {
      return null;
    }

    SyncConfigRequestPb.Builder builder = SyncConfigRequestPb.newBuilder();
    builder.setZone(syncConfigRequestJava.getZone());
    builder.setDataCenter(syncConfigRequestJava.getDataCenter());

    return builder.build();
  }
}
