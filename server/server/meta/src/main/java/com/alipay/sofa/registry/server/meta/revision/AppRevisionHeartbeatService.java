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
package com.alipay.sofa.registry.server.meta.revision;

import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.store.api.repository.AppRevisionHeartbeatRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionHeartbeatService.java, v 0.1 2021年04月15日 17:52 xiaojian.xj Exp $
 */
public class AppRevisionHeartbeatService {

  @Autowired private AppRevisionHeartbeatRepository appRevisionHeartbeatRepository;

  @Autowired private MetaServerConfig metaServerConfig;

  @Autowired private MetaLeaderService metaLeaderService;

  public void doRevisionGc() {
    if (metaLeaderService.amIStableAsLeader()) {
      appRevisionHeartbeatRepository.doAppRevisionGc(metaServerConfig.getRevisionGcSilenceHour());
    }
  }
}
