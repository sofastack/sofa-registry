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

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.console.PersistenceDataBuilder;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.jdbc.domain.ProvideDataDomain;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version $Id: ProvideDataDomainConvertor.java, v 0.1 2021年04月08日 17:49 xiaojian.xj Exp $
 */
public class ProvideDataDomainConvertor {

  public static ProvideDataDomain convert2ProvideData(
      PersistenceData persistenceData, String dataCenter) {
    if (persistenceData == null) {
      return null;
    }

    return new ProvideDataDomain(
        dataCenter,
        PersistenceDataBuilder.getDataInfoId(persistenceData),
        persistenceData.getData(),
        persistenceData.getVersion());
  }

  public static PersistenceData convert2PersistenceData(ProvideDataDomain provideData) {

    if (provideData == null) {
      return null;
    }

    DataInfo dataInfo = DataInfo.valueOf(provideData.getDataKey());
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setDataId(dataInfo.getDataId());
    persistenceData.setGroup(dataInfo.getGroup());
    persistenceData.setInstanceId(dataInfo.getInstanceId());
    persistenceData.setData(provideData.getDataValue());
    persistenceData.setVersion(provideData.getDataVersion());
    return persistenceData;
  }

  public static List<PersistenceData> convert2PersistenceDatas(
      List<ProvideDataDomain> provideDataDomains) {

    if (CollectionUtils.isEmpty(provideDataDomains)) {
      return Collections.emptyList();
    }
    return provideDataDomains.stream()
        .map(ProvideDataDomainConvertor::convert2PersistenceData)
        .collect(Collectors.toList());
  }
}
