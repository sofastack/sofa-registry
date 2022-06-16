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
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.common.model.metaserver.NodeServerOperateInfo;
import com.alipay.sofa.registry.common.model.metaserver.OperationInfo;
import com.alipay.sofa.registry.common.model.metaserver.blacklist.RegistryForbiddenServerRequest;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.provide.data.NodeOperatingService;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.util.JsonUtils;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Mar 18, 2021
 */
public class DefaultForbiddenServerManager implements RegistryForbiddenServerManager {

  protected final Logger LOGGER =
      LoggerFactory.getLogger(
          "WRAPPER-INTERCEPTOR", String.format("[%s]", getClass().getSimpleName()));

  @Autowired private ProvideDataService provideDataService;

  @Autowired private NodeOperatingService nodeOperatingService;

  private static final Long NOT_FOUND_VERSION = 0L;

  public DefaultForbiddenServerManager() {}

  public DefaultForbiddenServerManager(
      ProvideDataService provideDataService, NodeOperatingService nodeOperatingService) {
    this.provideDataService = provideDataService;
    this.nodeOperatingService = nodeOperatingService;
  }

  protected ForbiddenServer getForbiddenServers() {
    Tuple<Long, NodeServerOperateInfo> operateInfo =
        nodeOperatingService.queryOperateInfoAndVersion();

    if (operateInfo == null || operateInfo.o1 == null || operateInfo.o2 == null) {
      return new ForbiddenServer();
    }
    return new ForbiddenServer(operateInfo.o1, operateInfo.o2);
  }

  @Override
  public boolean addToBlacklist(RegistryForbiddenServerRequest request) {
    ForbiddenServer servers = getForbiddenServers();
    if (servers.add(request.getNodeType(), request.getCell(), request.getIp())) {
      return store(servers);
    }
    return true;
  }

  @Override
  public boolean removeFromBlacklist(RegistryForbiddenServerRequest request) {
    ForbiddenServer servers = getForbiddenServers();
    if (servers.remove(request.getNodeType(), request.getCell(), request.getIp())) {
      return store(servers);
    }
    return true;
  }

  @Override
  public boolean allowSelect(Lease<Node> lease) {
    ForbiddenServer servers = getForbiddenServers();
    return !servers.contains(lease.getRenewal().getNodeUrl().getIpAddress());
  }

  protected boolean store(ForbiddenServer servers) {
    PersistenceData persistence =
        PersistenceDataBuilder.createPersistenceData(
            ValueConstants.NODE_SERVER_OPERATING_DATA_ID,
            JsonUtils.writeValueAsString(servers.server));

    return provideDataService.saveProvideData(persistence, servers.version);
  }

  protected class ForbiddenServer {
    final long version;
    final NodeServerOperateInfo server;

    public ForbiddenServer() {
      this(NOT_FOUND_VERSION, new NodeServerOperateInfo());
    }

    public ForbiddenServer(long version, NodeServerOperateInfo server) {
      this.version = version;
      this.server = server;
    }

    public boolean add(NodeType nodeType, String cell, String address) {
      switch (nodeType) {
        case META:
          return server.addMetas(cell, address);
        case DATA:
          return server.addDatas(cell, address);
        case SESSION:
          return server.addSessions(cell, address);
        default:
          throw new SofaRegistryRuntimeException("unexpected node type: " + nodeType);
      }
    }

    public boolean remove(NodeType nodeType, String cell, String address) {
      switch (nodeType) {
        case META:
          return server.removeMetas(cell, address);
        case DATA:
          return server.removeDatas(cell, address);
        case SESSION:
          return server.removeSessions(cell, address);
        default:
          throw new SofaRegistryRuntimeException("unexpected node type: " + nodeType);
      }
    }

    public boolean contains(String address) {

      return contains(NodeType.META, address)
          || contains(NodeType.DATA, address)
          || contains(NodeType.SESSION, address);
    }

    public boolean contains(NodeType nodeType, String address) {
      Set<OperationInfo> set;
      switch (nodeType) {
        case META:
          set = server.getMetas();
          break;
        case DATA:
          set = server.getDatas();
          break;
        case SESSION:
          set = server.sessionNodes();
          break;
        default:
          throw new SofaRegistryRuntimeException("unexpected node type: " + nodeType);
      }
      long count =
          set.stream()
              .map(OperationInfo::getAddress)
              .filter(operating -> StringUtils.equals(address, operating))
              .count();
      return count == 1;
    }

    /**
     * Getter method for property <tt>version</tt>.
     *
     * @return property value of version
     */
    public long getVersion() {
      return version;
    }

    /**
     * Getter method for property <tt>server</tt>.
     *
     * @return property value of server
     */
    public NodeServerOperateInfo getServer() {
      return server;
    }
  }
}
