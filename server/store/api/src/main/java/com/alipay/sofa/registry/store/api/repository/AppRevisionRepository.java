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
package com.alipay.sofa.registry.store.api.repository;

import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.store.api.multi.MultiDataCenterListener;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionRepository.java, v 0.1 2021年01月17日 13:54 xiaojian.xj Exp $
 */
public interface AppRevisionRepository extends MultiDataCenterListener {

  /**
   * persistence appRevision
   *
   * @param appRevision appRevision
   */
  void register(AppRevision appRevision) throws Exception;

  /**
   * check if revisionId exist
   *
   * @param revisionId revisionId
   * @return boolean
   */
  boolean exist(String revisionId);
  /**
   * get AppRevision
   *
   * @param revision revision
   * @return
   */
  AppRevision queryRevision(String revision);

  boolean heartbeat(String revision);

  boolean heartbeatDB(String revision);

  Collection<String> availableRevisions();

  List<AppRevision> listFromStorage(long start, int limit);

  void startSynced();

  void waitSynced();

  List<AppRevision> getExpired(Date beforeTime, int limit);

  void replace(AppRevision appRevision);

  int cleanDeleted(Date beforeTime, int limit);

  Map<String, Integer> countByApp();

  Set<String> allRevisionIds();
}
