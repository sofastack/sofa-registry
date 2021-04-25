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

import com.alipay.sofa.registry.common.model.client.pb.ResultPb;
import com.alipay.sofa.registry.common.model.client.pb.SyncConfigResponsePb;
import com.alipay.sofa.registry.core.model.SyncConfigResponse;

/**
 * @author bystander
 * @version $Id: SyncConfigResponseConvertor.java, v 0.1 2018年03月21日 2:06 PM bystander Exp $
 */
public final class SyncConfigResponseConvertor {
  private SyncConfigResponseConvertor() {}

  public static SyncConfigResponse convert2Java(SyncConfigResponsePb syncConfigResponsePb) {

    if (syncConfigResponsePb == null) {
      return null;
    }

    SyncConfigResponse syncConfigResponse = new SyncConfigResponse();

    syncConfigResponse.setAvailableSegments(
        ListStringConvertor.convert2Java(syncConfigResponsePb.getAvailableSegmentsList()));

    syncConfigResponse.setRetryInterval(syncConfigResponsePb.getRetryInterval());
    syncConfigResponse.setMessage(syncConfigResponsePb.getResult().getMessage());
    syncConfigResponse.setSuccess(syncConfigResponsePb.getResult().getSuccess());

    return syncConfigResponse;
  }

  public static SyncConfigResponsePb convert2Pb(SyncConfigResponse syncConfigResponseJava) {

    if (syncConfigResponseJava == null) {
      return null;
    }

    SyncConfigResponsePb.Builder builder = SyncConfigResponsePb.newBuilder();
    builder.setRetryInterval(syncConfigResponseJava.getRetryInterval());
    builder.addAllAvailableSegments(syncConfigResponseJava.getAvailableSegments());

    ResultPb.Builder resultPbBuilder = ResultPb.newBuilder();
    if (syncConfigResponseJava.getMessage() != null) {
      resultPbBuilder.setMessage(syncConfigResponseJava.getMessage());
    }
    resultPbBuilder.setSuccess(syncConfigResponseJava.isSuccess());

    builder.setResult(resultPbBuilder.build());

    return builder.build();
  }
}
