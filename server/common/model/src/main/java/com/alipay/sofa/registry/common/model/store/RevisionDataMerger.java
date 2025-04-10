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
package com.alipay.sofa.registry.common.model.store;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.client.pb.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author huicha
 * @date 2025/3/14
 */
public class RevisionDataMerger {

  private static final Logger LOGGER = LoggerFactory.getLogger(RevisionDataMerger.class);

  private final SubDatum targetDatum;

  private final Map<String /* register id */, SubPublisher> totalAddPublishers;

  private final Map<String /* zone */, Set<String> /* register id */>
      totalDeletePublisherRegisterIds;

  public static RevisionDataMerger from(SubDatum targetDatum) {
    return new RevisionDataMerger(targetDatum);
  }

  public RevisionDataMerger(SubDatum targetDatum) {
    this.targetDatum = targetDatum;
    this.totalAddPublishers = new HashMap<>();
    this.totalDeletePublisherRegisterIds = new HashMap<>();
  }

  public void merge(SubDatumRevisionData datumRevisionData) {
    List<SubPublisher> addPublishers = datumRevisionData.getAddPublishers();
    if (CollectionUtils.isNotEmpty(addPublishers)) {
      for (SubPublisher addPublisher : addPublishers) {
        this.addPublisher(addPublisher);
      }
    }

    Map<String /* zone */, List<String> /* register id */> deletePublisherRegisterIds =
        datumRevisionData.getDeletePublisherRegisterIds();
    if (MapUtils.isNotEmpty(deletePublisherRegisterIds)) {
      for (Map.Entry<String, List<String>> entry : deletePublisherRegisterIds.entrySet()) {
        String zone = entry.getKey();
        List<String> registerIds = entry.getValue();
        this.deletePublisher(zone, registerIds);
      }
    }
  }

  public int getDataCount() {
    int addSize = this.totalAddPublishers.size();
    int deleteSize = 0;
    for (Set<String> registerIds : this.totalDeletePublisherRegisterIds.values()) {
      deleteSize += registerIds.size();
    }
    return addSize + deleteSize;
  }

  private void addPublisher(SubPublisher publisher) {
    String registerId = publisher.getRegisterId();
    String zone = publisher.getCell();
    this.totalAddPublishers.put(registerId, publisher);

    Set<String> registerIds = this.totalDeletePublisherRegisterIds.get(zone);
    if (null != registerIds) {
      registerIds.remove(registerId);
    }
  }

  private void deletePublisher(String zone, List<String> registerIds) {
    Set<String> zoneDeletePubRegisterIds =
        this.totalDeletePublisherRegisterIds.computeIfAbsent(zone, key -> new HashSet<>());
    zoneDeletePubRegisterIds.addAll(registerIds);

    for (String registerId : registerIds) {
      this.totalAddPublishers.remove(registerId);
    }
  }

  public DeltaReceivedDataBodyPb intoPb() {
    // 1. 整理出新增的 Publisher
    Map<String /* zone */, List<SubPublisher>> zoneAddPublishers =
        this.totalAddPublishers.values().stream()
            .collect(Collectors.groupingBy(SubPublisher::getCell));

    DeltaReceivedDataBodyPb.Builder deltaDataPbBuilder = DeltaReceivedDataBodyPb.newBuilder();
    for (Map.Entry<String /* zone */, List<SubPublisher>> entry : zoneAddPublishers.entrySet()) {
      String zone = entry.getKey();
      List<SubPublisher> addPublishers = entry.getValue();

      PushResourcesPb.Builder zoneResPbBuilder = PushResourcesPb.newBuilder();
      for (SubPublisher addPublisher : addPublishers) {
        String registerId = addPublisher.getRegisterId();
        int registryIdHash = registerId.hashCode();
        long version = addPublisher.getVersion();
        long timestamp = addPublisher.getRegisterTimestamp();

        PushResourcePb.Builder resPbBuilder = PushResourcePb.newBuilder();
        resPbBuilder.setRegisterId(registerId);
        resPbBuilder.setRegisterIdHash(registryIdHash);
        resPbBuilder.setVersion(version);
        resPbBuilder.setTimestamp(timestamp);

        List<ServerDataBox> dataBoxes = addPublisher.getDataList();
        for (ServerDataBox dataBox : dataBoxes) {
          try {
            DataBoxPb data = DataBoxPb.newBuilder().setData((String) dataBox.extract()).build();
            resPbBuilder.addResources(data);
          } catch (Throwable throwable) {
            LOGGER.error("Convert data box of publisher fail when merge revision data", throwable);
          }
        }

        PushResourcePb resPb = resPbBuilder.build();
        zoneResPbBuilder.addResources(resPb);
      }

      PushResourcesPb zoneResPb = zoneResPbBuilder.build();
      deltaDataPbBuilder.putAddPublishers(zone, zoneResPb);
    }

    // 2. 整理出删除的 Publisher Register Id
    for (Map.Entry<String, Set<String>> entry : this.totalDeletePublisherRegisterIds.entrySet()) {
      String zone = entry.getKey();
      Set<String> deleteRegisterIds = entry.getValue();

      RegisterIdsPb.Builder deleteRegisterIdsPbBuilder = RegisterIdsPb.newBuilder();

      for (String deleteRegisterId : deleteRegisterIds) {
        deleteRegisterIdsPbBuilder.addRegisterIds(deleteRegisterId);
      }

      RegisterIdsPb deleteRegisterIdsPb = deleteRegisterIdsPbBuilder.build();

      deltaDataPbBuilder.putDeletePublisherRegisterIds(zone, deleteRegisterIdsPb);
    }

    // 3. 计算全量数据的签名，客户端 merge 增量推送后会检查签名是否一致
    // todo(xidong.rxd): 考虑下缓存???
    List<SubPublisher> publishers = this.targetDatum.mustGetPublishers();
    Set<String> zones = new HashSet<>();
    Map<String, Long> zonePublisherCount = new HashMap<>();
    Map<String, Long> zoneDataCount = new HashMap<>();
    Map<String, Long> zoneRegisterIdHashSum = new HashMap<>();
    Map<String, Long> zoneRegisterIdHashSumXOR = new HashMap<>();
    Map<String, Long> zoneRegisterTimestampSum = new HashMap<>();
    Map<String, Long> zoneRegisterTimestampSumXOR = new HashMap<>();
    for (SubPublisher publisher : publishers) {
      String zone = publisher.getCell();
      List<ServerDataBox> dataList = publisher.getDataList();
      Long registerTimestamp = publisher.getRegisterTimestamp();
      long registerIdHashCode = publisher.getRegisterId().hashCode();
      long dataCount = null == dataList ? 0 : dataList.size();

      zones.add(zone);
      zonePublisherCount.merge(zone, 1L, Long::sum);
      zoneDataCount.merge(zone, dataCount, Long::sum);
      zoneRegisterIdHashSum.merge(zone, registerIdHashCode, Long::sum);
      zoneRegisterIdHashSumXOR.merge(zone, registerIdHashCode, (left, right) -> left ^ right);
      zoneRegisterTimestampSum.merge(zone, registerTimestamp, Long::sum);
      zoneRegisterTimestampSumXOR.merge(zone, registerTimestamp, (left, right) -> left ^ right);
    }

    for (String zone : zones) {
      Long publisherCount = zonePublisherCount.get(zone);
      Long dataCount = zoneDataCount.get(zone);
      Long registerIdHashSum = zoneRegisterIdHashSum.get(zone);
      Long registerIdHashSumXOR = zoneRegisterIdHashSumXOR.get(zone);
      Long registerTimestampSum = zoneRegisterTimestampSum.get(zone);
      Long registerTimestampSumXOR = zoneRegisterTimestampSumXOR.get(zone);

      DeltaDigestPb.Builder deltaDigestPbBuilder = DeltaDigestPb.newBuilder();
      deltaDigestPbBuilder.setPublisherCount(publisherCount);
      deltaDigestPbBuilder.setDataCount(dataCount);
      deltaDigestPbBuilder.setRegisterIdHashSum(registerIdHashSum);
      deltaDigestPbBuilder.setRegisterIdHashXOR(registerIdHashSumXOR);
      deltaDigestPbBuilder.setRegisterTimestampSum(registerTimestampSum);
      deltaDigestPbBuilder.setRegisterTimestampXOR(registerTimestampSumXOR);
      DeltaDigestPb deltaDigestPb = deltaDigestPbBuilder.build();

      deltaDataPbBuilder.putZoneDigests(zone, deltaDigestPb);
    }

    return deltaDataPbBuilder.build();
  }
}
