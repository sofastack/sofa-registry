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

import com.alipay.sofa.registry.common.model.*;
import com.alipay.sofa.registry.common.model.client.pb.*;
import com.alipay.sofa.registry.common.model.store.*;
import com.alipay.sofa.registry.compress.CompressedItem;
import com.alipay.sofa.registry.compress.Compressor;
import com.alipay.sofa.registry.compress.ZoneCount;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.MultiReceivedData;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.converter.ReceivedDataConverter;
import com.alipay.sofa.registry.server.session.converter.pb.DeltaReceivedDataConvertor;
import com.alipay.sofa.registry.server.session.converter.pb.ReceivedDataConvertor;
import com.alipay.sofa.registry.server.session.converter.pb.ReceivedDataConvertor.CompressorGetter;
import com.alipay.sofa.registry.server.session.multi.cluster.DataCenterMetadataCache;
import com.alipay.sofa.registry.server.session.predicate.ZonePredicate;
import com.alipay.sofa.registry.server.session.providedata.CompressPushService;
import com.alipay.sofa.registry.server.session.providedata.FetchIncrementalPushSwitchService;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;
import com.google.protobuf.UnsafeByteOperations;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class PushDataGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(RevisionDataMerger.class);

  @Autowired SessionServerConfig sessionServerConfig;

  @Resource CompressPushService compressPushService;

  @Autowired DataCenterMetadataCache dataCenterMetadataCache;

  @Autowired FetchIncrementalPushSwitchService fetchIncrementalPushSwitchService;

  public PushData createPushData(
      MultiSubDatum unzipDatum,
      MultiSubDatumRevisions datumRevisions,
      Map<String, Subscriber> subscriberMap) {
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

    if (this.fetchIncrementalPushSwitchService.useIncrementalPush()) {
      // Session 开启了增量推送
      Map<String, String> attribute = subscriber.getAttributes();
      String subSupport = attribute.get("Support-Incremental-Push");
      if (!StringUtils.isEmpty(subSupport) && Boolean.parseBoolean(subSupport)) {
        // Subscriber 也开启了增量推送，那么无论如何不使用旧数据结构了
        if (!subscriber.acceptMulti()) {
          // todo(xidong.rxd) 增加多租户支持
          try {
            return createDeltaReceivedData(unzipDatum, datumRevisions, clientCell, subscriberMap);
          } catch (Throwable throwable) {
            LOGGER.error("[DeltaPush] create delta received data exception", throwable);
            // 降级回原有逻辑
          }
        }
      }
      // Session 开启了增量推送，但是 Subscriber 不支持，因此降级成原有的逻辑
    }

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

  /**
   * @param unzipDatum
   * @param multiDatumRevisions
   * @param subscriberMap
   * @return
   */
  private PushData createDeltaReceivedData(
      MultiSubDatum unzipDatum,
      MultiSubDatumRevisions multiDatumRevisions,
      String clientZone,
      Map<String, Subscriber> subscriberMap) {
    if (!MultiSubDatumRevisions.valid(multiDatumRevisions)) {
      // 增量缓存数据不可用，降级成全量推送
      return this.createFullReceivedData(unzipDatum, clientZone, subscriberMap);
    }

    // 因为所有的 subscriber 都是同一个 ip + port 的，因此一定是来自同一个 client
    // 目前客户端的实现都是同一个客户端的所有 subscriber 都是同一个配置的，因此这里直接拿一个用就行
    final Subscriber subscriber = subscriberMap.values().iterator().next();

    // 获取需要推送的目标版本
    String localDataCenter = sessionServerConfig.getSessionServerDataCenter();
    long version = unzipDatum.getVersion(localDataCenter);
    SubDatum localDatum = unzipDatum.getSubDatum(localDataCenter);

    // 尝试使用缓存中的增量数据拼装要推送的数据
    long pushedVersion = subscriber.getPushedVersion(localDataCenter);
    if (pushedVersion <= 0) {
      // 理应只有客户端第一次订阅的时候才会走到这里
      return this.createFullReceivedData(unzipDatum, clientZone, subscriberMap);
    }

    List<SubDatumRevisionMark> datumRevisionsInFullData = localDatum.getDatumRevisionMarks();

    // 虽然我们默认是需要推送到全量数据的版本 version 的，但是实际可能还存在 mock 版本
    // 因此这里尝试找到最后一个 mock 版本，作为计算增量推送数据的版本
    long pushEndVersion = version;
    if (CollectionUtils.isNotEmpty(datumRevisionsInFullData)) {
      SubDatumRevisionMark latestDatumRevisionMarkInFullData =
          datumRevisionsInFullData.get(datumRevisionsInFullData.size() - 1);
      if (latestDatumRevisionMarkInFullData.isMock()) {
        pushEndVersion = latestDatumRevisionMarkInFullData.getVersion();
      }
    }

    LOGGER.error(
        "XD merge == pushed version: {}, target version: {}, push end version: {}, sub datum revisions: {}",
        pushedVersion,
        version,
        pushEndVersion,
        datumRevisionsInFullData.stream()
            .map(mark -> mark.getVersion() + " - " + mark.isMock())
            .map(String::valueOf)
            .collect(Collectors.joining(",")));

    SubDatumRevisions datumRevisions = multiDatumRevisions.getDatumRevisions(localDataCenter);
    final RevisionDataMerger merger = RevisionDataMerger.from(localDatum);
    Tuple<Boolean, String> visitResult =
        datumRevisions.tryVisitDatumRevisionDatum(pushedVersion, pushEndVersion, merger::merge);

    if (!visitResult.o1) {
      // 拼装失败，多半是缓存的增量数据丢失了，那么降级回全量推送，这里不记录日志了，记录可能会导致日志过多
      return this.createFullReceivedData(unzipDatum, clientZone, subscriberMap);
    }

    // 成功了，开始拼装 PushData，另外增量推送是不做压缩的
    DeltaReceivedDataBodyPb dataBodyPb = merger.intoPb();

    DeltaReceivedDataPb.Builder deltaReceivedDataPbBuilder = DeltaReceivedDataPb.newBuilder();
    deltaReceivedDataPbBuilder.setDataId(unzipDatum.getDataId());
    deltaReceivedDataPbBuilder.setGroup(unzipDatum.getGroup());
    deltaReceivedDataPbBuilder.setInstanceId(unzipDatum.getInstanceId());
    deltaReceivedDataPbBuilder.setSegment(localDataCenter);
    deltaReceivedDataPbBuilder.setScope(subscriber.getScope().name());
    for (Subscriber theSubscriber : subscriberMap.values()) {
      deltaReceivedDataPbBuilder.addSubscriberRegisterIds(theSubscriber.getRegisterId());
    }
    deltaReceivedDataPbBuilder.setVersion(version);
    deltaReceivedDataPbBuilder.setLocalZone(clientZone);
    deltaReceivedDataPbBuilder.setIsDelta(true);
    deltaReceivedDataPbBuilder.setDeltaBody(dataBodyPb);

    DeltaReceivedDataPb deltaReceivedDataPb = deltaReceivedDataPbBuilder.build();

    SegmentPushInfo segmentPushInfo = new SegmentPushInfo(localDataCenter, merger.getDataCount());
    DataCenterPushInfo dataCenterPushInfo =
        new DataCenterPushInfo(localDataCenter, version, segmentPushInfo);
    return new PushData<>(
        deltaReceivedDataPb, Collections.singletonMap(localDataCenter, dataCenterPushInfo));
  }

  private PushData createFullReceivedData(
      MultiSubDatum unzipDatum, String clientZone, Map<String, Subscriber> subscriberMap) {
    final Subscriber subscriber = subscriberMap.values().iterator().next();
    String localDataCenter = sessionServerConfig.getSessionServerDataCenter();
    long version = unzipDatum.getVersion(localDataCenter);
    SubDatum datum = unzipDatum.getSubDatum(localDataCenter);
    List<SubPublisher> publishers = datum.mustGetPublishers();

    Map<String /* zone */, List<SubPublisher>> zonePublishers =
        publishers.stream().collect(Collectors.groupingBy(SubPublisher::getCell));

    // 统计推送下去的数据大小 (bytes)
    long dataSize = 0;
    // 统计推送下去的 DataBox 数量
    int dataCount = 0;
    // 分 zone 统计推送下去的 DataBox 数量
    List<ZoneCount> zoneCounts = new ArrayList<>();
    FullReceivedDataBodyPb.Builder fullReceivedDataBodyPbBuilder =
        FullReceivedDataBodyPb.newBuilder();
    for (Map.Entry<String /* zone */, List<SubPublisher>> publisherEntry :
        zonePublishers.entrySet()) {
      int zoneDataCount = 0;
      String zone = publisherEntry.getKey();
      List<SubPublisher> publishersOfZone = publisherEntry.getValue();

      PushResourcesPb.Builder zoneResPbBuilder = PushResourcesPb.newBuilder();
      for (SubPublisher publisher : publishersOfZone) {
        String registerId = publisher.getRegisterId();
        int registerIdHash = registerId.hashCode();
        long publisherVersion = publisher.getVersion();
        long publisherTimestamp = publisher.getRegisterTimestamp();

        PushResourcePb.Builder resPbBuilder = PushResourcePb.newBuilder();
        resPbBuilder.setRegisterId(registerId);
        resPbBuilder.setRegisterIdHash(registerIdHash);
        resPbBuilder.setVersion(publisherVersion);
        resPbBuilder.setTimestamp(publisherTimestamp);

        List<ServerDataBox> dataBoxes = publisher.getDataList();
        for (ServerDataBox dataBox : dataBoxes) {
          try {
            String dataStr = (String) dataBox.extract();
            DataBoxPb data = DataBoxPb.newBuilder().setData(dataStr).build();
            resPbBuilder.addResources(data);
            dataSize += dataStr.length();
            dataCount++;
            zoneDataCount++;
          } catch (Throwable throwable) {
            LOGGER.error(
                "Convert data box of publisher fail when create full push data", throwable);
          }
        }

        PushResourcePb resPb = resPbBuilder.build();
        zoneResPbBuilder.addResources(resPb);
      }

      PushResourcesPb zoneResPb = zoneResPbBuilder.build();
      fullReceivedDataBodyPbBuilder.putData(zone, zoneResPb);

      zoneCounts.add(ZoneCount.of(zone, zoneDataCount));
    }

    Compressor compressor =
        this.compressPushService.getCompressor(
            dataSize, subscriber.getAcceptEncodes(), subscriber.getSourceAddress().getIpAddress());

    DeltaReceivedDataPb.Builder deltaReceivedDataPbBuilder = DeltaReceivedDataPb.newBuilder();
    deltaReceivedDataPbBuilder.setDataId(unzipDatum.getDataId());
    deltaReceivedDataPbBuilder.setGroup(unzipDatum.getGroup());
    deltaReceivedDataPbBuilder.setInstanceId(unzipDatum.getInstanceId());
    deltaReceivedDataPbBuilder.setSegment(localDataCenter);
    deltaReceivedDataPbBuilder.setScope(subscriber.getScope().name());
    for (Subscriber theSubscriber : subscriberMap.values()) {
      deltaReceivedDataPbBuilder.addSubscriberRegisterIds(theSubscriber.getRegisterId());
    }
    deltaReceivedDataPbBuilder.setVersion(version);
    deltaReceivedDataPbBuilder.setLocalZone(clientZone);
    deltaReceivedDataPbBuilder.setIsDelta(false);

    if (null != compressor) {
      // 开启压缩
      FullReceivedDataBodyPb fullReceivedDataBodyPb = fullReceivedDataBodyPbBuilder.build();
      byte[] bodyBytes = fullReceivedDataBodyPb.toByteArray();
      CompressedItem compressedItem;

      try {
        compressedItem =
            DeltaReceivedDataConvertor.compressed(
                localDataCenter,
                datum.getDataId(),
                datum.getInstanceId(),
                datum.getGroup(),
                datum.getVersion(),
                fullReceivedDataBodyPb,
                zoneCounts.toArray(new ZoneCount[] {}),
                compressor);
      } catch (Throwable throwable) {
        throw new IllegalStateException(throwable);
      }

      FullReceivedDataBodyPb.Builder compressedBodyPbBuilder = FullReceivedDataBodyPb.newBuilder();
      compressedBodyPbBuilder.setEncoding(compressor.getEncoding());
      compressedBodyPbBuilder.setBody(
          UnsafeByteOperations.unsafeWrap(compressedItem.getCompressedData()));
      compressedBodyPbBuilder.setOriginBodySize(bodyBytes.length);
      deltaReceivedDataPbBuilder.setFullBody(compressedBodyPbBuilder);

      DeltaReceivedDataPb deltaReceivedDataPb = deltaReceivedDataPbBuilder.build();

      SegmentPushInfo segmentPushInfo =
          new SegmentPushInfo(
              localDataCenter,
              dataCount,
              compressor.getEncoding(),
              compressedItem.getCompressedData().length);
      DataCenterPushInfo dataCenterPushInfo =
          new DataCenterPushInfo(localDataCenter, version, segmentPushInfo);
      return new PushData(
          deltaReceivedDataPb, Collections.singletonMap(localDataCenter, dataCenterPushInfo));
    } else {
      // 不开启压缩
      deltaReceivedDataPbBuilder.setFullBody(fullReceivedDataBodyPbBuilder);
      DeltaReceivedDataPb deltaReceivedDataPb = deltaReceivedDataPbBuilder.build();

      SegmentPushInfo segmentPushInfo = new SegmentPushInfo(localDataCenter, dataCount);
      DataCenterPushInfo dataCenterPushInfo =
          new DataCenterPushInfo(localDataCenter, version, segmentPushInfo);
      return new PushData(
          deltaReceivedDataPb, Collections.singletonMap(localDataCenter, dataCenterPushInfo));
    }
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
