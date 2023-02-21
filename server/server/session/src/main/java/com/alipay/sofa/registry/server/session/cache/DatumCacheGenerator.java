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
package com.alipay.sofa.registry.server.session.cache;

import com.alipay.sofa.registry.common.model.store.MultiSubDatum;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author shangyu.wh
 * @version $Id: DatumCacheGenerator.java, v 0.1 2018-11-19 16:15 shangyu.wh Exp $
 */
public class DatumCacheGenerator implements CacheGenerator {
  private static final Logger LOGGER = LoggerFactory.getLogger("CACHE-GEN");
  /** DataNode service */
  @Autowired DataNodeService dataNodeService;

  @Override
  public Value generatePayload(Key key) {
    EntityType entityType = key.getEntityType();
    if (entityType instanceof DatumKey) {
      DatumKey datumKey = (DatumKey) entityType;

      final Set<String> dataCenters = datumKey.getDataCenters();
      final String dataInfoId = datumKey.getDataInfoId();
      ParaCheckUtil.checkNotEmpty(dataCenters, "dataCenter");
      ParaCheckUtil.checkNotBlank(dataInfoId, "dataInfoId");
      final long now = System.currentTimeMillis();
      MultiSubDatum datum = dataNodeService.fetch(dataInfoId, dataCenters);
      final long span = System.currentTimeMillis() - now;
      if (datum == null || CollectionUtils.isEmpty(datum.getDatumMap())) {
        LOGGER.info("loadNil,{},{},span={}", dataInfoId, dataCenters, span);
      } else {
        for (String dataCenter : dataCenters) {
          SubDatum subDatum = datum.getSubDatum(dataCenter);
          // some datacenter datum not exist
          if (subDatum == null) {
            LOGGER.info("loadNil,{},{},span={}", dataInfoId, dataCenter, span);
          }
        }
        LOGGER.info(
            "loadD,{},{},{},{},{},span={}",
            dataInfoId,
            dataCenters,
            datum.getPubNum(),
            datum.getDataBoxBytes(),
            datum.getVersion(),
            span);
      }
      return new Value(datum);
    }
    throw new IllegalArgumentException("unsupported key type:" + entityType);
  }
}
