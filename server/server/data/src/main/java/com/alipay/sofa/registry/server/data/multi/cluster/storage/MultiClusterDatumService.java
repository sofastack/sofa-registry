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
package com.alipay.sofa.registry.server.data.multi.cluster.storage;

import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.common.model.metaserver.RemoteDatumClearEvent;
import com.alipay.sofa.registry.common.model.metaserver.RemoteDatumClearEvent.DatumType;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.change.DataChangeType;
import com.alipay.sofa.registry.server.data.slot.SlotAccessorDelegate;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.google.common.collect.Maps;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : MultiClusterDatumService.java, v 0.1 2022年09月08日 17:31 xiaojian.xj Exp $
 */
public class MultiClusterDatumService {

  private static final Logger LOG = LoggerFactory.getLogger(MultiClusterDatumService.class);

  @Autowired private DatumStorageDelegate datumStorageDelegate;

  @Autowired private SlotAccessorDelegate slotAccessorDelegate;

  @Autowired private MultiClusterSyncRepository multiClusterSyncRepository;

  @Autowired private DataChangeEventCenter dataChangeEventCenter;

  public void clear(RemoteDatumClearEvent request) {

    MultiClusterSyncInfo query = multiClusterSyncRepository.query(request.getRemoteDataCenter());
    if (query != null && query.isEnableSyncDatum()) {
      LOG.error("clear datum forbidden when sync enable, request:{}", request);
      return;
    }

    Map<String, DatumVersion> datumVersionMap = Maps.newHashMap();
    if (request.getDatumType() == DatumType.DATA_INFO_ID) {
      if (!slotAccessorDelegate.isLeader(
          request.getRemoteDataCenter(), slotAccessorDelegate.slotOf(request.getDataInfoId()))) {
        return;
      }
      DatumVersion datumVersion =
          datumStorageDelegate.clearPublishers(
              request.getRemoteDataCenter(), request.getDataInfoId());
      if (datumVersion != null) {
        datumVersionMap.put(request.getDataInfoId(), datumVersion);
      }
    } else if (request.getDatumType() == DatumType.GROUP) {
      datumVersionMap =
          datumStorageDelegate.clearGroupPublishers(
              request.getRemoteDataCenter(), request.getGroup());
    } else {
      throw new SofaRegistryRuntimeException("illegal request datumType:" + request.getDatumType());
    }

    LOG.info("[ClearMultiDatum]req: {}, datumVersionMap:{}", request, datumVersionMap);
    if (!CollectionUtils.isEmpty(datumVersionMap)) {
      dataChangeEventCenter.onChange(
          datumVersionMap.keySet(), DataChangeType.CLEAR, request.getRemoteDataCenter());
    }
  }
}
