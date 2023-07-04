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

import com.alipay.sofa.registry.jdbc.domain.ProvideDataDomain;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @author xiaojian.xj
 * @version $Id: ProvideDataMapper.java, v 0.1 2021年03月12日 11:05 xiaojian.xj Exp $
 */
public interface ProvideDataMapper {

  /**
   * insert or update provideData
   *
   * @param data data
   * @return int
   */
  public int save(ProvideDataDomain data);

  /**
   * insert or update provideData
   *
   * @param data data
   * @param exceptVersion exceptVersion
   * @return int
   */
  public int update(
      @Param("data") ProvideDataDomain data, @Param("exceptVersion") long exceptVersion);

  /**
   * query provideData
   *
   * @param dataCenter dataCenter
   * @param dataKey dataKey
   * @return ProvideDataDomain
   */
  public ProvideDataDomain query(
      @Param("dataCenter") String dataCenter, @Param("dataKey") String dataKey);

  /**
   * remove provideData
   *
   * @param dataCenter dataCenter
   * @param dataKey dataKey
   * @param dataVersion dataVersion
   * @return int
   */
  public int remove(
      @Param("dataCenter") String dataCenter,
      @Param("dataKey") String dataKey,
      @Param("dataVersion") long dataVersion);

  /**
   * query by page
   *
   * @param dataCenter dataCenter
   * @param start start
   * @param limit limit
   * @return List
   */
  List<ProvideDataDomain> queryByPage(
      @Param("dataCenter") String dataCenter, @Param("start") int start, @Param("limit") int limit);

  /**
   * query total count
   *
   * @param dataCenter dataCenter
   * @return int
   */
  int selectTotalCount(@Param("dataCenter") String dataCenter);
}
