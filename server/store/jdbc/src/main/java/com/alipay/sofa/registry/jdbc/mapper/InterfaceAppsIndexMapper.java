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

import com.alipay.sofa.registry.jdbc.domain.InterfaceAppIndexQueryModel;
import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import java.sql.Timestamp;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @author xiaojian.xj
 * @version $Id: InterfaceAppsIndexMapper.java, v 0.1 2021年01月24日 17:04 xiaojian.xj Exp $
 */
public interface InterfaceAppsIndexMapper {

  /**
   * query by interfaceName
   *
   * @param dataCenter
   * @param interfaceName
   * @return
   */
  public List<InterfaceAppsIndexDomain> queryByInterfaceName(
      @Param("dataCenter") String dataCenter, @Param("interfaceName") String interfaceName);

  /**
   * batch query by interfaceName
   *
   * @param querys
   * @return
   */
  public List<InterfaceAppsIndexDomain> batchQueryByInterface(
      List<InterfaceAppIndexQueryModel> querys);

  /**
   * insert
   *
   * @param domains
   * @return effect rows number
   */
  public int insert(List<InterfaceAppsIndexDomain> domains);

  /**
   * query domains which gmt_modified is after than maxUpdate
   *
   * @param dataCenter
   * @param maxUpdate
   * @returns
   */
  List<InterfaceAppsIndexDomain> queryModifyAfterThan(
      @Param("dataCenter") String dataCenter,
      @Param("maxUpdate") Timestamp maxUpdate,
      @Param("limitCount") int limitCount);

  /**
   * query domains which gmt_modified equal maxUpdate
   *
   * @param dataCenter
   * @param maxUpdate
   * @returns
   */
  List<InterfaceAppsIndexDomain> queryModifyEquals(
      @Param("dataCenter") String dataCenter, @Param("maxUpdate") Timestamp maxUpdate);

  int getTotalCount(@Param("dataCenter") String dataCenter);
}
