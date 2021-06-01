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

import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerAddressMapper.java, v 0.1 2021年05月12日 19:38 xiaojian.xj Exp $
 */
public interface ClientManagerAddressMapper {

  int queryTotalCount(@Param("dataCenter") String dataCenter);

  /**
   * query after than maxId
   *
   * @param maxId
   * @param limit
   * @return
   */
  List<ClientManagerAddress> queryAfterThanByLimit(
      @Param("dataCenter") String dataCenter,
      @Param("maxId") long maxId,
      @Param("limit") long limit);

  List<ClientManagerAddress> queryAfterThan(
      @Param("dataCenter") String dataCenter, @Param("maxId") long maxId);

  /**
   * @param clientManagerAddress
   * @return effect rows
   */
  int update(ClientManagerAddress clientManagerAddress);

  /**
   * insert on replace
   *
   * @param clientManagerAddress
   * @return
   */
  int insertOnReplace(ClientManagerAddress clientManagerAddress);
}
