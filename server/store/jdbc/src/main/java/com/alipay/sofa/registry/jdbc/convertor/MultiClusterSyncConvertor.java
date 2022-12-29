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
package com.alipay.sofa.registry.jdbc.convertor;

import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.jdbc.domain.MultiClusterSyncDomain;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSyncConvertor.java, v 0.1 2022年04月15日 14:20 xiaojian.xj Exp $
 */
public class MultiClusterSyncConvertor {

  public static final TypeReference<Set<String>> FORMAT = new TypeReference<Set<String>>() {};

  public static MultiClusterSyncDomain convert2Domain(MultiClusterSyncInfo info, String clusterId) {
    if (info == null) {
      return null;
    }
    return new MultiClusterSyncDomain(
        clusterId,
        info.getRemoteDataCenter(),
        info.getRemoteMetaAddress(),
        info.isEnableSyncDatum() ? "true" : "false",
        info.isEnablePush() ? "true" : "false",
        JsonUtils.writeValueAsString(info.getSyncDataInfoIds()),
        JsonUtils.writeValueAsString(info.getSynPublisherGroups()),
        JsonUtils.writeValueAsString(info.getIgnoreDataInfoIds()),
        info.getDataVersion());
  }

  public static MultiClusterSyncInfo convert2Info(MultiClusterSyncDomain domain) {
    if (domain == null) {
      return null;
    }
    return new MultiClusterSyncInfo(
        domain.getDataCenter(),
        domain.getRemoteDataCenter(),
        domain.getRemoteMetaAddress(),
        Boolean.parseBoolean(domain.getEnableSyncDatum()),
        Boolean.parseBoolean(domain.getEnablePush()),
        JsonUtils.read(domain.getSyncDataInfoIds(), FORMAT),
        JsonUtils.read(domain.getSynPublisherGroups(), FORMAT),
        JsonUtils.read(domain.getIgnoreDataInfoIds(), FORMAT),
        domain.getDataVersion());
  }

  public static Set<MultiClusterSyncInfo> convert2Infos(List<MultiClusterSyncDomain> domains) {
    if (CollectionUtils.isEmpty(domains)) {
      return Collections.emptySet();
    }
    return domains.stream()
        .map(MultiClusterSyncConvertor::convert2Info)
        .collect(Collectors.toSet());
  }
}
