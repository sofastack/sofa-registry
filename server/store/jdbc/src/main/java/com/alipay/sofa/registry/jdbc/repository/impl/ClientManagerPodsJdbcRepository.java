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
package com.alipay.sofa.registry.jdbc.repository.impl;

import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.ProvideData.CLIENT_MANAGER_QUERY_COUNTER;
import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.ProvideData.CLIENT_MANAGER_UPDATE_COUNTER;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerPods;
import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.mapper.ClientManagerPodsMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.meta.ClientManagerPodsRepository;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerPodsJdbcRepository.java, v 0.1 2021年05月12日 19:27 xiaojian.xj Exp $
 */
public class ClientManagerPodsJdbcRepository implements ClientManagerPodsRepository {

  private static final Logger LOG = LoggerFactory.getLogger("META-PROVIDEDATA", "[ClientManager]");

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  @Autowired private ClientManagerPodsMapper clientManagerPodsMapper;

  @Override
  public boolean clientOpen(Set<String> ipSet) {

    try {
      for (String address : ipSet) {
        ClientManagerPods update =
            new ClientManagerPods(
                defaultCommonConfig.getClusterId(), address, ValueConstants.CLIENT_OPEN);
        int effectRows = clientManagerPodsMapper.update(update);

        if (effectRows == 0) {
          clientManagerPodsMapper.insertOnReplace(update);
        }
      }
      CLIENT_MANAGER_UPDATE_COUNTER.inc(ipSet.size());
    } catch (Throwable t) {
      LOG.error("clientOpen:{} error.", ipSet, t);
      return false;
    }
    return true;
  }

  @Override
  public boolean clientOff(Set<String> ipSet) {
    try {

      for (String address : ipSet) {
        ClientManagerPods update =
            new ClientManagerPods(
                defaultCommonConfig.getClusterId(), address, ValueConstants.CLIENT_OFF);
        int effectRows = clientManagerPodsMapper.update(update);

        if (effectRows == 0) {
          clientManagerPodsMapper.insertOnReplace(update);
        }
      }
      CLIENT_MANAGER_UPDATE_COUNTER.inc(ipSet.size());
    } catch (Throwable t) {
      LOG.error("clientOff:{} error.", ipSet, t);
      return false;
    }
    return true;
  }

  @Override
  public List<ClientManagerPods> queryAfterThan(long maxId) {
    return clientManagerPodsMapper.queryAfterThan(defaultCommonConfig.getClusterId(), maxId);
  }

  @Override
  public List<ClientManagerPods> queryAfterThan(long maxId, long limit) {
    CLIENT_MANAGER_QUERY_COUNTER.inc();
    return clientManagerPodsMapper.queryAfterThanByLimit(
        defaultCommonConfig.getClusterId(), maxId, limit);
  }

  @Override
  public int queryTotalCount() {
    CLIENT_MANAGER_QUERY_COUNTER.inc();
    return clientManagerPodsMapper.queryTotalCount(defaultCommonConfig.getClusterId());
  }
}
