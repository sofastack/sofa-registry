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

import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.ProvideData.PROVIDE_DATA_QUERY_COUNTER;
import static com.alipay.sofa.registry.jdbc.repository.impl.MetadataMetrics.ProvideData.PROVIDE_DATA_UPDATE_COUNTER;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.jdbc.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jdbc.convertor.ProvideDataDomainConvertor;
import com.alipay.sofa.registry.jdbc.domain.ProvideDataDomain;
import com.alipay.sofa.registry.jdbc.mapper.ProvideDataMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.util.MathUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: ProvideDataJdbcRepository.java, v 0.1 2021年03月13日 19:20 xiaojian.xj Exp $
 */
public class ProvideDataJdbcRepository implements ProvideDataRepository {

  private static final Logger LOG = LoggerFactory.getLogger("META-PROVIDEDATA", "[ProvideData]");

  @Autowired private ProvideDataMapper provideDataMapper;

  @Autowired private DefaultCommonConfig defaultCommonConfig;

  private static final Integer batchQuerySize = 1000;

  @Override
  public boolean put(PersistenceData persistenceData, long expectVersion) {

    ProvideDataDomain exist =
        provideDataMapper.query(
            defaultCommonConfig.getClusterId(),
            PersistenceDataBuilder.getDataInfoId(persistenceData));

    PROVIDE_DATA_QUERY_COUNTER.inc();
    ProvideDataDomain domain =
        ProvideDataDomainConvertor.convert2ProvideData(
            persistenceData, defaultCommonConfig.getClusterId());
    int affect;
    try {
      if (exist == null) {
        affect = provideDataMapper.save(domain);
        if (LOG.isInfoEnabled()) {
          LOG.info("save provideData: {}, affect rows: {}", persistenceData, affect);
        }
      } else {
        affect = provideDataMapper.update(domain, expectVersion);
        if (LOG.isInfoEnabled()) {
          LOG.info(
              "update provideData: {}, expectVersion: {}, affect rows: {}",
              domain,
              expectVersion,
              affect);
        }
      }
      PROVIDE_DATA_UPDATE_COUNTER.inc();

      if (affect == 0) {
        PersistenceData query = get(domain.getDataKey());
        LOG.error(
            "put provideData fail, query: {}, update: {}, expectVersion: {}",
            query,
            domain,
            expectVersion);
      }

    } catch (Throwable t) {
      LOG.error("put provideData: {} error.", domain, t);
      return false;
    }

    return affect > 0;
  }

  @Override
  public PersistenceData get(String key) {
    PROVIDE_DATA_QUERY_COUNTER.inc();
    return ProvideDataDomainConvertor.convert2PersistenceData(
        provideDataMapper.query(defaultCommonConfig.getClusterId(), key));
  }

  @Override
  public boolean remove(String key, long version) {
    PROVIDE_DATA_UPDATE_COUNTER.inc();
    int affect = provideDataMapper.remove(defaultCommonConfig.getClusterId(), key, version);
    if (LOG.isInfoEnabled()) {
      LOG.info(
          "remove provideData, dataCenter: {}, key: {}, version: {}, affect rows: {}",
          defaultCommonConfig.getClusterId(),
          key,
          version,
          affect);
    }
    return affect > 0;
  }

  @Override
  public Collection<PersistenceData> getAll() {

    Collection<PersistenceData> responses = new ArrayList<>();
    int total = provideDataMapper.selectTotalCount(defaultCommonConfig.getClusterId());
    int round = MathUtils.divideCeil(total, batchQuerySize);
    for (int i = 0; i < round; i++) {
      int start = i * batchQuerySize;
      List<ProvideDataDomain> provideDataDomains =
          provideDataMapper.queryByPage(defaultCommonConfig.getClusterId(), start, batchQuerySize);
      responses.addAll(ProvideDataDomainConvertor.convert2PersistenceDatas(provideDataDomains));
    }
    PROVIDE_DATA_QUERY_COUNTER.inc();
    return responses;
  }
}
