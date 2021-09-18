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
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Lists;
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
   * @param unzipDatum the datum
   * @param scope the scope
   * @param subscriberRegisterIdList the subscriber register id list
   * @param regionLocal the region local
   * @return received data multi
   */
  public static PushData<ReceivedData> getReceivedDataMulti(
      SubDatum unzipDatum,
      ScopeEnum scope,
      List<String> subscriberRegisterIdList,
      String regionLocal,
      Predicate<String> zonePredicate) {

    if (null == unzipDatum) {
      return new PushData<>(null, 0);
    }
    unzipDatum.mustUnzipped();
    // todo judge server mode to decide local region
    ReceivedData receivedData = new ReceivedData();
    receivedData.setDataId(unzipDatum.getDataId());
    receivedData.setGroup(unzipDatum.getGroup());
    receivedData.setInstanceId(unzipDatum.getInstanceId());
    receivedData.setSubscriberRegistIds(subscriberRegisterIdList);
    receivedData.setSegment(unzipDatum.getDataCenter());
    receivedData.setScope(scope.name());

    receivedData.setVersion(unzipDatum.getVersion());

    receivedData.setLocalZone(regionLocal);

    Map<String /*zone*/, List<DataBox>> swizzMap = new HashMap<>();

    List<SubPublisher> publishers = unzipDatum.mustGetPublishers();
    if (publishers.isEmpty()) {
      receivedData.setData(swizzMap);
      return new PushData<>(receivedData, 0);
    }
    int dataCount = 0;
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
      dataCount += datas.size();
    }

    receivedData.setData(swizzMap);

    return new PushData<>(receivedData, dataCount);
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

  public static ReceivedConfigData createReceivedConfigData(
      Watcher watcher, ProvideData provideData) {
    DataInfo dataInfo = DataInfo.valueOf(watcher.getDataInfoId());
    ReceivedConfigData receivedConfigData =
        ReceivedDataConverter.getReceivedConfigData(
            provideData.getProvideData(), dataInfo, provideData.getVersion());
    receivedConfigData.setConfiguratorRegistIds(Lists.newArrayList(watcher.getRegisterId()));
    return receivedConfigData;
  }
}
