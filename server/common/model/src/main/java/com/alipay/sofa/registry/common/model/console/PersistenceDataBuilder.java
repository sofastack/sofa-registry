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
package com.alipay.sofa.registry.common.model.console;

import com.alipay.sofa.registry.common.model.store.DataInfo;

/**
 * @author xiaojian.xj
 * @version $Id: PersistenceDataBuilder.java, v 0.1 2021年04月07日 20:17 xiaojian.xj Exp $
 */
public class PersistenceDataBuilder {

  public static PersistenceData createPersistenceData(String dataInfoId, String data) {
    DataInfo dataInfo = DataInfo.valueOf(dataInfoId);
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setDataId(dataInfo.getDataId());
    persistenceData.setGroup(dataInfo.getGroup());
    persistenceData.setInstanceId(dataInfo.getInstanceId());
    persistenceData.setData(data);
    persistenceData.setVersion(nextVersion());
    return persistenceData;
  }

  public static PersistenceData createPersistenceDataForBool(String dataInfoId, boolean data) {
    return createPersistenceData(dataInfoId, data ? "true" : "false");
  }

  public static String getDataInfoId(PersistenceData persistenceData) {
    return DataInfo.toDataInfoId(
        persistenceData.getDataId(), persistenceData.getInstanceId(), persistenceData.getGroup());
  }

  public static long nextVersion() {
    return System.currentTimeMillis();
  }

  public static String getEntityData(PersistenceData data) {
    if (data != null) {
      return data.getData();
    }
    return null;
  }
}
