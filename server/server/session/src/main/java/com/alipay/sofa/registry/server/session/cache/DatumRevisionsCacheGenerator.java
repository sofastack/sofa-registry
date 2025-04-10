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

import com.alipay.sofa.registry.common.model.store.MultiSubDatumRevisions;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huicha
 * @date 2025/3/13
 */
public class DatumRevisionsCacheGenerator implements CacheGenerator {

  @Autowired private DataNodeService dataNodeService;

  @Override
  public Value generatePayload(Key key) {
    EntityType entityType = key.getEntityType();
    if (!(entityType instanceof DatumRevisionsKey)) {
      throw new IllegalArgumentException("unsupported key type:" + entityType);
    }

    DatumRevisionsKey datumRevisionsKey = (DatumRevisionsKey) entityType;

    final Set<String> dataCenters = datumRevisionsKey.getDataCenters();
    final String dataInfoId = datumRevisionsKey.getDataInfoId();
    ParaCheckUtil.checkNotEmpty(dataCenters, "dataCenter");
    ParaCheckUtil.checkNotBlank(dataInfoId, "dataInfoId");

    MultiSubDatumRevisions multiSubDatumRevisions =
        this.dataNodeService.fetchRevisions(dataInfoId, dataCenters);
    // 这里缓存的数据不需要 intern 了，在 DataNodeService 中已经 intern 过了
    return new Value(multiSubDatumRevisions);
  }
}
