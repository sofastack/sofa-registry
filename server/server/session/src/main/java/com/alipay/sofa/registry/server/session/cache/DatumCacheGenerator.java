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

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author shangyu.wh
 * @version $Id: DatumCacheGenerator.java, v 0.1 2018-11-19 16:15 shangyu.wh Exp $
 */
public class DatumCacheGenerator implements CacheGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("CACHE-GEN");
    /**
     * DataNode service
     */
    @Autowired
    private DataNodeService     dataNodeService;

    @Override
    public Value generatePayload(Key key) {
        EntityType entityType = key.getEntityType();
        if (entityType instanceof DatumKey) {
            DatumKey datumKey = (DatumKey) entityType;

            String dataCenter = datumKey.getDataCenter();
            String dataInfoId = datumKey.getDataInfoId();
            if (StringUtils.isNotBlank(dataCenter) && StringUtils.isNotBlank(dataInfoId)) {
                Datum datum = dataNodeService.fetchDataCenter(dataInfoId, dataCenter);
                if (datum == null) {
                    LOGGER.info("genCacheNil,{},{}", dataInfoId, dataCenter);
                } else {
                    LOGGER.info("genCache,{},{},{},{}", dataInfoId, dataCenter,
                        datum.publisherSize(), datum.getVersion());
                }
                return new Value(datum);
            }
        }
        throw new IllegalArgumentException("unsupported Key:" + key);
    }
}