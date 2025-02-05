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
package com.alipay.sofa.registry.store.api.meta;

import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSyncRepository.java, v 0.1 2022年04月13日 16:53 xiaojian.xj Exp $
 */
public interface MultiClusterSyncRepository {

  /**
   * insert
   *
   * @param syncInfo syncInfo
   * @return boolean
   */
  boolean insert(MultiClusterSyncInfo syncInfo);

  /**
   * update with cas
   *
   * @param syncInfo syncInfo
   * @return boolean
   */
  boolean update(MultiClusterSyncInfo syncInfo, long expectVersion);

  /**
   * query MultiClusterSyncInfo
   *
   * @return Set
   */
  public Set<MultiClusterSyncInfo> queryLocalSyncInfos();

  /**
   * remove provideData
   *
   * @param remoteDataCenter remoteDataCenter
   * @param dataVersion dataVersion
   * @return int
   */
  public int remove(String remoteDataCenter, long dataVersion);

  MultiClusterSyncInfo query(String remoteDataCenter);
}
