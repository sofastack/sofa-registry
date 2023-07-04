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

import com.alipay.sofa.registry.common.model.DataCenterPushInfo;
import com.alipay.sofa.registry.common.model.SegmentPushInfo;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.MultiReceivedData;
import com.alipay.sofa.registry.core.model.MultiSegmentData;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

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
   * @param localDataCenter localDataCenter
   * @param pushdataPredicate pushdataPredicate
   * @return received data multi
   */
  public static PushData<ReceivedData> getReceivedData(
      MultiSubDatum unzipDatum,
      ScopeEnum scope,
      List<String> subscriberRegisterIdList,
      String regionLocal,
      String localDataCenter,
      Predicate<String> pushdataPredicate) {

    if (null == unzipDatum || CollectionUtils.isEmpty(unzipDatum.getDatumMap())) {
      return new PushData<>(null, Collections.EMPTY_MAP);
    }
    unzipDatum.mustUnzipped();
    ReceivedData receivedData = new ReceivedData();
    receivedData.setDataId(unzipDatum.getDataId());
    receivedData.setGroup(unzipDatum.getGroup());
    receivedData.setInstanceId(unzipDatum.getInstanceId());
    receivedData.setSubscriberRegistIds(subscriberRegisterIdList);
    receivedData.setScope(scope.name());
    receivedData.setLocalZone(regionLocal);

    DataCenterPushInfo dataCenterPushInfo =
        fillRegionDatas(unzipDatum, receivedData, localDataCenter, pushdataPredicate);

    return new PushData<>(
        receivedData, Collections.singletonMap(receivedData.getSegment(), dataCenterPushInfo));
  }

  public static PushData<MultiReceivedData> getMultiReceivedData(
      MultiSubDatum unzipDatum,
      ScopeEnum scope,
      List<String> subscriberRegisterIdList,
      String regionLocal,
      String localDataCenter,
      Predicate<String> pushdataPredicate,
      Map<String, Set<String>> segmentZones) {
    if (null == unzipDatum || CollectionUtils.isEmpty(unzipDatum.getDatumMap())) {
      return new PushData<>(null, Collections.EMPTY_MAP);
    }
    unzipDatum.mustUnzipped();

    MultiReceivedData receivedData = new MultiReceivedData();
    receivedData.setDataId(unzipDatum.getDataId());
    receivedData.setGroup(unzipDatum.getGroup());
    receivedData.setInstanceId(unzipDatum.getInstanceId());
    receivedData.setSubscriberRegistIds(subscriberRegisterIdList);
    receivedData.setScope(scope.name());
    receivedData.setLocalZone(regionLocal);
    receivedData.setLocalSegment(localDataCenter);

    Map<String, DataCenterPushInfo> dataCenterPushInfo =
        fillMultiRegionData(
            unzipDatum, localDataCenter, receivedData, pushdataPredicate, segmentZones);
    return new PushData<>(receivedData, dataCenterPushInfo);
  }

  private static Map<String, DataCenterPushInfo> fillMultiRegionData(
      MultiSubDatum unzipDatum,
      String localDataCenter,
      MultiReceivedData multiReceivedData,
      Predicate<String> pushdataPredicate,
      Map<String, Set<String>> segmentZones) {

    final Map<String, MultiSegmentData> multiDatas = Maps.newHashMap();
    final Map<String, DataCenterPushInfo> dataCenterPushInfo = Maps.newHashMap();

    for (Entry<String, SubDatum> datumEntry : unzipDatum.getDatumMap().entrySet()) {
      String pushDataCenter = datumEntry.getKey();
      SubDatum subDatum = datumEntry.getValue();

      Map<String, SegmentDataCounter> multiSegmentDatas =
          buildMultiSegmentDataFromSubDatum(
              localDataCenter,
              pushDataCenter,
              subDatum,
              pushdataPredicate,
              segmentZones.get(pushDataCenter));
      if (CollectionUtils.isEmpty(multiSegmentDatas)) {
        continue;
      }

      final Map<String, SegmentPushInfo> segmentPushInfo = Maps.newHashMap();

      for (Entry<String, SegmentDataCounter> entry : multiSegmentDatas.entrySet()) {
        SegmentDataCounter value = entry.getValue();
        multiDatas.put(entry.getKey(), value.getSegmentData());
        segmentPushInfo.put(
            entry.getKey(), new SegmentPushInfo(entry.getKey(), value.getDataCount()));
      }
      dataCenterPushInfo.put(
          pushDataCenter, new DataCenterPushInfo(subDatum.getVersion(), segmentPushInfo));
    }
    multiReceivedData.setMultiData(multiDatas);
    return dataCenterPushInfo;
  }

  private static Map<String, SegmentDataCounter> buildMultiSegmentDataFromSubDatum(
      String localDataCenter,
      String pushDataCenter,
      SubDatum subDatum,
      Predicate<String> pushdataPredicate,
      Set<String> segmentZones) {

    Map<String, SegmentDataCounter> ret = Maps.newHashMap();
    if (StringUtils.equals(localDataCenter, pushDataCenter)) {
      LocalDataCenterPushData localDataCenterPushData =
          localSegmentData(localDataCenter, subDatum, pushdataPredicate, segmentZones);
      if (localDataCenterPushData == null) {
        return null;
      }

      ret.put(localDataCenter, localDataCenterPushData.getLocalSegmentDatas());
      ret.putAll(localDataCenterPushData.getRemoteSegmentDatas());
      return ret;
    }
    return remoteSegmentData(subDatum, segmentZones);
  }

  private static Map<String, SegmentDataCounter> remoteSegmentData(
      SubDatum subDatum, Set<String> segmentZones) {

    Map<String, List<DataBox>> pushData;
    try {
      if (CollectionUtils.isEmpty(segmentZones)) {
        throw new SofaRegistryRuntimeException(StringFormatter.format("segmentZones is empty."));
      }
      pushData = swizzData(subDatum, null);
    } catch (Throwable th) {
      LOGGER.error("build remoteSegmentData error, dataId: {}.", subDatum.getDataId(), th);
      return null;
    }

    Map<String, SegmentDataCounter> ret = Maps.newHashMapWithExpectedSize(segmentZones.size());
    for (String zone : segmentZones) {
      SegmentDataCounter counter =
          new SegmentDataCounter(new MultiSegmentData(zone, subDatum.getVersion()));
      counter.put(zone, pushData.get(zone));
      ret.put(zone, counter);
    }
    return ret;
  }

  private static LocalDataCenterPushData localSegmentData(
      String localDataCenter,
      SubDatum subDatum,
      Predicate<String> pushdataPredicate,
      Set<String> segmentZones) {

    // split into local and remote
    Map<String, List<DataBox>> pushData;
    try {
      if (CollectionUtils.isEmpty(segmentZones)) {
        throw new SofaRegistryRuntimeException(StringFormatter.format("segmentZones is empty."));
      }
      pushData = swizzData(subDatum, null);
    } catch (Throwable th) {
      LOGGER.error("build localSegmentData error, dataId: {}.", subDatum.getDataId(), th);
      return null;
    }

    LocalDataCenterPushData localDataCenterPushData = new LocalDataCenterPushData();
    localDataCenterPushData.from(
        pushData, localDataCenter, subDatum.getVersion(), pushdataPredicate, segmentZones);
    return localDataCenterPushData;
  }

  private static DataCenterPushInfo fillRegionDatas(
      MultiSubDatum unzipDatum,
      ReceivedData receivedData,
      String localDataCenter,
      Predicate<String> pushdataPredicate) {

    ParaCheckUtil.checkEquals(
        unzipDatum.dataCenters(),
        Collections.singleton(localDataCenter),
        "fillRegionDatas.dataCenter");
    SubDatum subDatum = unzipDatum.getSubDatum(localDataCenter);
    receivedData.setSegment(localDataCenter);
    receivedData.setVersion(subDatum.getVersion());

    Map<String, List<DataBox>> data = swizzData(subDatum, pushdataPredicate);
    receivedData.setData(data);

    Map<String, Integer> pushDataCount = Maps.newHashMapWithExpectedSize(data.size());

    int dataCount = 0;
    for (Entry<String, List<DataBox>> entry : data.entrySet()) {
      int size = entry.getValue().size();
      pushDataCount.put(entry.getKey(), size);
      dataCount += size;
    }

    receivedData.setDataCount(pushDataCount);
    DataCenterPushInfo dataCenterPushInfo =
        new DataCenterPushInfo(
            localDataCenter,
            subDatum.getVersion(),
            new SegmentPushInfo(localDataCenter, dataCount));
    return dataCenterPushInfo;
  }

  private static Map<String /*zone*/, List<DataBox>> swizzData(
      SubDatum subDatum, Predicate<String> pushdataPredicate) {
    Map<String /*zone*/, List<DataBox>> swizzMap = new HashMap<>();
    List<SubPublisher> publishers = subDatum.mustGetPublishers();
    if (publishers.isEmpty()) {
      return Collections.EMPTY_MAP;
    }
    for (SubPublisher publisher : publishers) {
      List<ServerDataBox> datas = publisher.getDataList();

      String region = publisher.getCell();

      if (pushdataPredicate != null && pushdataPredicate.test(region)) {
        continue;
      }
      if (null == datas) {
        datas = new ArrayList<>();
      }
      List<DataBox> regionDatas = swizzMap.computeIfAbsent(region, k -> new ArrayList<>());
      fillRegionDatas(regionDatas, datas);
    }
    return swizzMap;
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
