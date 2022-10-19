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
package com.alipay.sofa.registry.server.session.push;

import com.alipay.sofa.registry.common.model.DataCenterPushInfo;
import com.alipay.sofa.registry.common.model.SegmentPushInfo;
import com.alipay.sofa.registry.common.model.SubscriberUtils;
import com.alipay.sofa.registry.common.model.client.pb.MultiReceivedDataPb;
import com.alipay.sofa.registry.common.model.client.pb.MultiSegmentDataPb;
import com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.MultiReceivedData;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.converter.pb.ReceivedDataConvertor;
import com.alipay.sofa.registry.server.session.converter.pb.ReceivedDataConvertor.CompressorGetter;
import com.alipay.sofa.registry.server.session.multi.cluster.DataCenterMetadataCache;
import com.alipay.sofa.registry.server.session.predicate.ZonePredicate;
import com.alipay.sofa.registry.server.session.providedata.CompressPushService;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class PushDataGenerator {

  @Autowired SessionServerConfig sessionServerConfig;

  @Resource CompressPushService compressPushService;

  @Autowired DataCenterMetadataCache dataCenterMetadataCache;

  public PushData createPushData(MultiSubDatum unzipDatum, Map<String, Subscriber> subscriberMap) {
    unzipDatum.mustUnzipped();
    if (subscriberMap.size() > 1) {
      SubscriberUtils.getAndAssertHasSameScope(subscriberMap.values());
      SubscriberUtils.getAndAssertAcceptedEncodes(subscriberMap.values());
      SubscriberUtils.getAndAssertAcceptMulti(subscriberMap.values());
    }
    // only supported 4.x
    SubscriberUtils.assertClientVersion(subscriberMap.values(), BaseInfo.ClientVersion.StoreData);

    final Subscriber subscriber = subscriberMap.values().iterator().next();
    String clientCell = sessionServerConfig.getClientCell(subscriber.getCell());

    CompressorGetter compressorGetter =
        (Map<String, List<DataBox>> data) ->
            compressPushService.getCompressor(
                data, subscriber.getAcceptEncodes(), subscriber.getSourceAddress().getIpAddress());
    if (subscriber.acceptMulti()) {
      return createMultiReceivePushData(
          unzipDatum,
          clientCell,
          Lists.newArrayList(subscriberMap.keySet()),
          subscriber,
          compressorGetter);
    }
    return createReceivePushData(
        unzipDatum,
        clientCell,
        Lists.newArrayList(subscriberMap.keySet()),
        subscriber,
        compressorGetter);
  }

  private PushData createMultiReceivePushData(
      MultiSubDatum unzipDatum,
      String clientCell,
      List<String> subscriberRegisterIdList,
      Subscriber subscriber,
      CompressorGetter compressorGetter) {
    Predicate<String> pushDataPredicate =
        ZonePredicate.pushDataPredicate(
            unzipDatum.getDataId(), clientCell, subscriber.getScope(), sessionServerConfig);

    Set<String> pushDataCenters = unzipDatum.getDatumMap().keySet();
    Map<String, Set<String>> segmentZones =
        dataCenterMetadataCache.dataCenterZonesOf(pushDataCenters);

    Set<String> metadataDataCenters = segmentZones.keySet();

    if (!pushDataCenters.equals(metadataDataCenters)) {
      throw new SofaRegistryRuntimeException(
          StringFormatter.format(
              "createMultiReceivePushData error, datum.dataCenters: {}, metadata.dataCenters: {}",
              pushDataCenters,
              metadataDataCenters));
    }
    PushData<MultiReceivedData> pushData =
        ReceivedDataConverter.getMultiReceivedData(
            unzipDatum,
            subscriber.getScope(),
            subscriberRegisterIdList,
            clientCell,
            sessionServerConfig.getSessionServerDataCenter(),
            pushDataPredicate,
            segmentZones);

    final Byte serializerIndex = subscriber.getSourceAddress().getSerializerIndex();
    if (serializerIndex == null || URL.PROTOBUF != serializerIndex) {
      return pushData;
    }

    ParaCheckUtil.checkNotEmpty(pushData.getPayload().getMultiData(), "multiSegmentDatas");
    MultiReceivedDataPb multiReceivedDataPb =
        ReceivedDataConvertor.convert2MultiPb(pushData.getPayload(), compressorGetter);

    fillSegmentPushInfo(pushData, multiReceivedDataPb);

    return new PushData<>(multiReceivedDataPb, pushData.getDataCenterPushInfo());
  }

  private void fillSegmentPushInfo(
      PushData<MultiReceivedData> pushData, MultiReceivedDataPb multiReceivedDataPb) {
    for (Entry<String, MultiSegmentDataPb> entry :
        multiReceivedDataPb.getMultiDataMap().entrySet()) {
      pushData.addSegmentInfo(
          entry.getKey(), entry.getValue().getEncoding(), entry.getValue().getZipData().size());
    }
  }

  private PushData createReceivePushData(
      MultiSubDatum unzipDatum,
      String clientCell,
      List<String> subscriberRegisterIdList,
      Subscriber subscriber,
      CompressorGetter compressorGetter) {
    Predicate<String> pushDataPredicate =
        ZonePredicate.pushDataPredicate(
            unzipDatum.getDataId(), clientCell, subscriber.getScope(), sessionServerConfig);

    PushData<ReceivedData> pushData =
        ReceivedDataConverter.getReceivedData(
            unzipDatum,
            subscriber.getScope(),
            subscriberRegisterIdList,
            clientCell,
            sessionServerConfig.getSessionServerDataCenter(),
            pushDataPredicate);

    final Byte serializerIndex = subscriber.getSourceAddress().getSerializerIndex();
    if (serializerIndex == null || URL.PROTOBUF != serializerIndex) {
      return pushData;
    }

    ParaCheckUtil.checkNotNull(pushData.getPayload().getData(), "datas");
    ReceivedDataPb receivedDataPb =
        ReceivedDataConvertor.convert2Pb(pushData.getPayload(), compressorGetter);

    if (receivedDataPb.getBody() == null || StringUtils.isEmpty(receivedDataPb.getEncoding())) {
      return new PushData(receivedDataPb, pushData.getDataCenterPushInfo());
    } else {
      pushData.addSegmentInfo(
          receivedDataPb.getSegment(),
          receivedDataPb.getEncoding(),
          receivedDataPb.getBody().size());
      return new PushData<>(receivedDataPb, pushData.getDataCenterPushInfo());
    }
  }

  public PushData createPushData(Watcher watcher, ReceivedConfigData data) {
    URL url = watcher.getSourceAddress();
    Object o = data;
    if (url.getSerializerIndex() != null && URL.PROTOBUF == url.getSerializerIndex()) {
      o = ReceivedDataConvertor.convert2Pb(data);
    }

    String dataCenter = sessionServerConfig.getSessionServerDataCenter();
    DataCenterPushInfo dataCenterPushInfo = new DataCenterPushInfo();
    dataCenterPushInfo.setSegmentPushInfos(
        Collections.singletonMap(dataCenter, new SegmentPushInfo(dataCenter, 1)));
    return new PushData(o, Collections.singletonMap(dataCenter, dataCenterPushInfo));
  }
}
