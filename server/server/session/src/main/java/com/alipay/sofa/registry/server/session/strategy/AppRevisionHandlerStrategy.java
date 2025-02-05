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
package com.alipay.sofa.registry.server.session.strategy;

import com.alipay.sofa.registry.common.model.client.pb.GetRevisionsResponse;
import com.alipay.sofa.registry.common.model.client.pb.MetaHeartbeatResponse;
import com.alipay.sofa.registry.common.model.client.pb.ServiceAppMappingResponse;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import java.util.List;

public interface AppRevisionHandlerStrategy {

  /**
   * appRevision register
   *
   * @param appRevision appRevision
   * @param response response
   */
  void handleAppRevisionRegister(
      AppRevision appRevision, RegisterResponse response, String remoteAddress);

  /**
   * query apps by services
   *
   * @param services services
   * @return ServiceAppMappingResponse
   */
  ServiceAppMappingResponse queryApps(List<String> services, String remoteIp);

  /**
   * query appRevision
   *
   * @param revisions revisions
   * @return GetRevisionsResponse
   */
  GetRevisionsResponse queryRevision(List<String> revisions);

  /**
   * revision heartbeat
   *
   * @param revisions revisions
   * @return MetaHeartbeatResponse
   */
  MetaHeartbeatResponse heartbeat(List<String> revisions);
}
