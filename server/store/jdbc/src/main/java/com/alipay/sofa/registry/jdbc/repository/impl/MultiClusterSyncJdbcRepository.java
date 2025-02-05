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

import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.jdbc.constant.TableEnum;
import com.alipay.sofa.registry.jdbc.convertor.MultiClusterSyncConvertor;
import com.alipay.sofa.registry.jdbc.domain.MultiClusterSyncDomain;
import com.alipay.sofa.registry.jdbc.mapper.MultiClusterSyncMapper;
import com.alipay.sofa.registry.jdbc.version.config.BaseConfigRepository;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.config.DefaultCommonConfig;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.alipay.sofa.registry.store.api.meta.RecoverConfig;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSyncJdbcRepository.java, v 0.1 2022年04月14日 15:12 xiaojian.xj Exp $
 */
public class MultiClusterSyncJdbcRepository implements MultiClusterSyncRepository, RecoverConfig {

  private static final Logger LOG =
      LoggerFactory.getLogger("MULTI-CLUSTER-CONFIG", "[UpdateSyncInfo]");

  @Autowired private MultiClusterSyncMapper multiClusterSyncMapper;

  @Autowired protected DefaultCommonConfig defaultCommonConfig;

  private Configer configer;

  public MultiClusterSyncJdbcRepository() {
    configer = new Configer();
  }

  class Configer extends BaseConfigRepository<MultiClusterSyncDomain> {
    public Configer() {
      super("MultiClusterSyncInfo", LOG);
    }

    @Override
    protected MultiClusterSyncDomain queryExistVersion(MultiClusterSyncDomain entry) {
      return multiClusterSyncMapper.query(entry.getDataCenter(), entry.getRemoteDataCenter());
    }

    @Override
    protected long insert(MultiClusterSyncDomain entry) {
      return multiClusterSyncMapper.save(entry);
    }

    @Override
    protected int updateWithExpectVersion(MultiClusterSyncDomain entry, long exist) {
      return multiClusterSyncMapper.update(entry, exist);
    }
  }

  @Override
  public boolean insert(MultiClusterSyncInfo syncInfo) {
    try {
      MultiClusterSyncDomain domain =
          MultiClusterSyncConvertor.convert2Domain(
              syncInfo, defaultCommonConfig.getClusterId(tableName()));
      MultiClusterSyncDomain exist = configer.queryExistVersion(domain);
      if (exist != null) {
        LOG.error("multi cluster sync info: {} exist when insert.", syncInfo);
        return false;
      }
      // it will throw duplicate key exception when parallel invocation
      configer.insert(domain);
    } catch (Throwable t) {
      LOG.error("multi cluster sync info: {} insert error", syncInfo, t);
      return false;
    }

    return true;
  }

  @Override
  public boolean update(MultiClusterSyncInfo syncInfo, long expectVersion) {
    return configer.put(
        MultiClusterSyncConvertor.convert2Domain(
            syncInfo, defaultCommonConfig.getClusterId(tableName())),
        expectVersion);
  }

  @Override
  public Set<MultiClusterSyncInfo> queryLocalSyncInfos() {
    List<MultiClusterSyncDomain> domains =
        multiClusterSyncMapper.queryByCluster(defaultCommonConfig.getClusterId(tableName()));
    return MultiClusterSyncConvertor.convert2Infos(domains);
  }

  @Override
  public int remove(String remoteDataCenter, long dataVersion) {
    return multiClusterSyncMapper.remove(
        defaultCommonConfig.getClusterId(tableName()), remoteDataCenter, dataVersion);
  }

  @Override
  public MultiClusterSyncInfo query(String remoteDataCenter) {
    MultiClusterSyncDomain query =
        multiClusterSyncMapper.query(
            defaultCommonConfig.getClusterId(tableName()), remoteDataCenter);
    return MultiClusterSyncConvertor.convert2Info(query);
  }

  @Override
  public String tableName() {
    return TableEnum.MULTI_CLUSTER_SYNC_INFO.getTableName();
  }
}
