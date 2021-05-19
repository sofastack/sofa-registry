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
package com.alipay.sofa.registry.server.meta.lease.filter;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.collect.Sets;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chen.zhu
 *     <p>Mar 18, 2021
 */
@Component
public class DefaultForbiddenServerManager implements RegistryForbiddenServerManager {

  @Autowired private ProvideDataService provideDataService;

  private static final Long NOT_FOUND_VERSION = 0L;

  public DefaultForbiddenServerManager() {}

  public DefaultForbiddenServerManager(ProvideDataService provideDataService) {
    this.provideDataService = provideDataService;
  }

  private ForbiddenServer getForbiddenServers() {
    DBResponse<PersistenceData> response =
        provideDataService.queryProvideData(ValueConstants.REGISTRY_SERVER_BLACK_LIST_DATA_ID);

    if (response.getOperationStatus() == OperationStatus.SUCCESS) {
      PersistenceData persistenceData = response.getEntity();
      Set<String> servers = JsonUtils.read(persistenceData.getData(), Set.class);
      return new ForbiddenServer(persistenceData.getVersion(), servers);
    }

    return new ForbiddenServer(NOT_FOUND_VERSION, Sets.newHashSet());
  }

  @Override
  public boolean addToBlacklist(String ip) {
    ForbiddenServer servers = getForbiddenServers();
    if (servers.servers.add(ip)) {
      return store(servers);
    }
    return true;
  }

  @Override
  public boolean removeFromBlacklist(String ip) {
    ForbiddenServer servers = getForbiddenServers();
    if (servers.servers.remove(ip)) {
      return store(servers);
    }
    return true;
  }

  @Override
  public boolean allowSelect(Lease<Node> lease) {
    ForbiddenServer servers = getForbiddenServers();
    return !servers.servers.contains(lease.getRenewal().getNodeUrl().getIpAddress());
  }

  protected boolean store(ForbiddenServer servers) {
    PersistenceData persistence =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.REGISTRY_SERVER_BLACK_LIST_DATA_ID,
            JsonUtils.writeValueAsString(servers.servers));

    return provideDataService.saveProvideData(persistence, servers.version);
  }

  class ForbiddenServer {
    final long version;
    final Set<String> servers;

    public ForbiddenServer(long version, Set<String> servers) {
      this.version = version;
      this.servers = servers;
    }
  }
}
