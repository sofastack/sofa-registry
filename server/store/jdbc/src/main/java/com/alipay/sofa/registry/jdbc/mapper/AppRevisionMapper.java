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

import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Param;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionMapper.java, v 0.1 2021年01月18日 17:49 xiaojian.xj Exp $
 */
public interface AppRevisionMapper {

  /**
   * query revision
   *
   * @param dataCenters dataCenters
   * @param revision revision
   * @return List
   */
  List<AppRevisionDomain> queryRevision(
      @Param("dataCenters") Set<String> dataCenters, @Param("revision") String revision);

  List<AppRevisionDomain> listRevisions(
      @Param("dataCenter") String dataCenter,
      @Param("afterId") long afterId,
      @Param("limit") int limit);

  int heartbeat(@Param("dataCenter") String dataCenter, @Param("revision") String revision);

  void replace(AppRevisionDomain domain);

  List<AppRevisionDomain> getExpired(
      @Param("dataCenter") String dataCenter,
      @Param("beforeTime") Date beforeTime,
      @Param("limit") int limit);

  int cleanDeleted(
      @Param("dataCenter") String dataCenter,
      @Param("beforeTime") Date beforeTime,
      @Param("limit") int limit);
}
