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

import com.alipay.sofa.registry.jdbc.domain.InterfaceAppsIndexDomain;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Param;

/**
 * @author xiaojian.xj
 * @version $Id: InterfaceAppsIndexMapper.java, v 0.1 2021年01月24日 17:04 xiaojian.xj Exp $
 */
public interface InterfaceAppsIndexMapper {

  /**
   * insert on replace
   *
   * @param domain domain
   * @return int
   */
  int replace(InterfaceAppsIndexDomain domain);

  /**
   * insert
   *
   * @param domain domain
   * @return effect rows number
   */
  int update(InterfaceAppsIndexDomain domain);

  /**
   * query domains which gmt_modified is after than maxUpdate
   *
   * @param dataCenters dataCenters
   * @param maxId maxId
   * @param limitCount limitCount
   * @return List
   */
  List<InterfaceAppsIndexDomain> queryLargeThan(
      @Param("dataCenters") Set<String> dataCenters,
      @Param("maxId") long maxId,
      @Param("limitCount") int limitCount);
}
