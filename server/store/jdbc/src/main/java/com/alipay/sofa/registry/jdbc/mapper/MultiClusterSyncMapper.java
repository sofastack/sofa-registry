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
package com.alipay.sofa.registry.jdbc.mapper;

import com.alipay.sofa.registry.jdbc.domain.MultiClusterSyncDomain;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSyncMapper.java, v 0.1 2022年04月13日 15:00 xiaojian.xj Exp $
 */
public interface MultiClusterSyncMapper {

  /**
   * insert data
   *
   * @param data data
   * @return int
   */
  public int save(MultiClusterSyncDomain data);

  /**
   * update meta address with exceptVersion cas
   *
   * @param data data
   * @param exceptVersion exceptVersion
   * @return int
   */
  public int update(
      @Param("data") MultiClusterSyncDomain data, @Param("exceptVersion") long exceptVersion);

  /**
   * query MultiClusterSyncInfo
   *
   * @param dataCenter dataCenter
   * @return List
   */
  public List<MultiClusterSyncDomain> queryByCluster(@Param("dataCenter") String dataCenter);

  /**
   * query MultiClusterSyncInfo
   *
   * @param dataCenter dataCenter
   * @param remoteDataCenter remoteDataCenter
   * @return MultiClusterSyncDomain
   */
  public MultiClusterSyncDomain query(
      @Param("dataCenter") String dataCenter, @Param("remoteDataCenter") String remoteDataCenter);

  /**
   * remove provideData
   *
   * @param dataCenter dataCenter
   * @param remoteDataCenter remoteDataCenter
   * @param dataVersion dataVersion
   * @return int
   */
  public int remove(
      @Param("dataCenter") String dataCenter,
      @Param("remoteDataCenter") String remoteDataCenter,
      @Param("dataVersion") long dataVersion);
}
