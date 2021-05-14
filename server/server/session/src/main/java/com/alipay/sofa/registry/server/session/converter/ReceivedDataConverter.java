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
package com.alipay.sofa.registry.server.session.converter;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.SubPublisher;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The type Received data converter.
 *
 * @author shangyu.wh
 * @version $Id : ReceivedDataConverter.java, v 0.1 2017-12-13 13:42 shangyu.wh Exp $
 */
public final class ReceivedDataConverter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReceivedDataConverter.class);

  private ReceivedDataConverter() {}
  /**
   * Standard RunEnv
   *
   * @param datum the datum
   * @param scope the scope
   * @param subscriberRegisterIdList the subscriber register id list
   * @param regionLocal the region local
   * @return received data multi
   */
  public static ReceivedData getReceivedDataMulti(
      SubDatum datum,
      ScopeEnum scope,
      List subscriberRegisterIdList,
      String regionLocal,
      Predicate<String> zonePredicate) {

    if (null == datum) {
      return null;
    }

    // todo judge server mode to decide local region
    ReceivedData receivedData = new ReceivedData();
    receivedData.setDataId(datum.getDataId());
    receivedData.setGroup(datum.getGroup());
    receivedData.setInstanceId(datum.getInstanceId());
    receivedData.setSubscriberRegistIds(subscriberRegisterIdList);
    receivedData.setSegment(datum.getDataCenter());
    receivedData.setScope(scope.name());

    receivedData.setVersion(datum.getVersion());

    receivedData.setLocalZone(regionLocal);

    Map<String /*zone*/, List<DataBox>> swizzMap = new HashMap<>();

    List<SubPublisher> publishers = datum.getPublishers();
    if (publishers.isEmpty()) {
      receivedData.setData(swizzMap);
      return receivedData;
    }
    for (SubPublisher publisher : publishers) {
      List<ServerDataBox> datas = publisher.getDataList();

      String region = publisher.getCell();

      if (zonePredicate.test(region)) {
        continue;
      }

      if (null == datas) {
        datas = new ArrayList<>();
      }

      List<DataBox> regionDatas = swizzMap.computeIfAbsent(region, k -> new ArrayList<>());
      fillRegionDatas(regionDatas, datas);
    }

    receivedData.setData(swizzMap);

    return receivedData;
  }

  private static void fillRegionDatas(List<DataBox> regionDatas, List<ServerDataBox> datas) {
    for (ServerDataBox data : datas) {
      DataBox box = new DataBox();
      try {
        String dataString = (String) data.extract();
        box.setData(dataString);
        regionDatas.add(box);
      } catch (Exception e) {
        LOGGER.error("ReceivedData convert error", e);
      }
    }
  }

  public static ReceivedConfigData getReceivedConfigData(
      ServerDataBox dataBox, DataInfo dataInfo, Long version) {
    ReceivedConfigData receivedConfigData = new ReceivedConfigData();

    if (dataBox != null) {
      DataBox box = new DataBox();
      String dataString = (String) dataBox.getObject();
      box.setData(dataString);
      receivedConfigData.setDataBox(box);
    }
    receivedConfigData.setDataId(dataInfo.getDataId());
    receivedConfigData.setGroup(dataInfo.getGroup());
    receivedConfigData.setInstanceId(dataInfo.getInstanceId());
    if (version == null) {
      version = DatumVersionUtil.nextId();
    }
    receivedConfigData.setVersion(DatumVersionUtil.transferDatumVersion(version));
    return receivedConfigData;
  }
}
