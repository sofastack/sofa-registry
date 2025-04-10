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

import com.alipay.sofa.registry.common.model.dataserver.DatumRevisionData;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author huicha
 * @date 2025/3/11
 */
public class SubDatumRevisionData implements Serializable {

  private static final long serialVersionUID = 2820427609913590925L;

  private final String dataCenter;

  private final String dataInfoId;

  private final long version;

  private final List<SubPublisher> addPublishers;

  private final Map<String /* zone */, List<String> /* register id */> deletePublisherRegisterIds;

  public static SubDatumRevisionData empty(String dataCenter, String dataInfoId, long version) {
    return new SubDatumRevisionData(
        dataCenter, dataInfoId, version, Collections.emptyList(), Collections.emptyMap());
  }

  public static SubDatumRevisionData from(
      String dataCenter,
      String dataInfoId,
      long version,
      List<SubPublisher> addPublishers,
      Map<String, List<String>> deletePublisherRegisterIds) {
    return new SubDatumRevisionData(
        dataCenter, dataInfoId, version, addPublishers, deletePublisherRegisterIds);
  }

  public static SubDatumRevisionData from(DatumRevisionData datumRevisionData) {
    List<Publisher> publishers = datumRevisionData.getAddPublishers();

    // todo(xidong.rxd): 这里的转换逻辑是否存在问题 ???
    List<SubPublisher> subPublishers;
    if (CollectionUtils.isNotEmpty(publishers)) {
      subPublishers =
          publishers.stream()
              .map(
                  publisher -> {
                    final URL srcAddress = publisher.getSourceAddress();
                    // temp publisher the srcAddress maybe null
                    final String srcAddressString =
                        srcAddress == null ? null : srcAddress.buildAddressString();
                    return new SubPublisher(
                        publisher.getRegisterId(),
                        publisher.getCell(),
                        publisher.getDataList(),
                        publisher.getClientId(),
                        publisher.getVersion(),
                        srcAddressString,
                        publisher.getRegisterTimestamp(),
                        publisher.getPublishSource());
                  })
              .collect(Collectors.toList());
    } else {
      subPublishers = Collections.emptyList();
    }

    return new SubDatumRevisionData(
        datumRevisionData.getDataCenter(),
        datumRevisionData.getDataInfoId(),
        datumRevisionData.getVersion(),
        subPublishers,
        datumRevisionData.getDeletePublisherRegisterIds());
  }

  public SubDatumRevisionData(
      String dataCenter,
      String dataInfoId,
      long version,
      List<SubPublisher> addPublishers,
      Map<String, List<String>> deletePublisherRegisterIds) {
    this.dataCenter = dataCenter;
    this.dataInfoId = dataInfoId;
    this.version = version;
    this.addPublishers = addPublishers;
    this.deletePublisherRegisterIds = deletePublisherRegisterIds;
  }

  public String getDataCenter() {
    return dataCenter;
  }

  public String getDataInfoId() {
    return dataInfoId;
  }

  public long getVersion() {
    return version;
  }

  public List<SubPublisher> getAddPublishers() {
    return Collections.unmodifiableList(addPublishers);
  }

  public Map<String, List<String>> getDeletePublisherRegisterIds() {
    return Collections.unmodifiableMap(deletePublisherRegisterIds);
  }

  public boolean isEmpty() {
    return CollectionUtils.isEmpty(this.addPublishers)
        && MapUtils.isEmpty(this.deletePublisherRegisterIds);
  }

  public SubDatumRevisionData intern() {
    List<SubPublisher> publishers;
    if (CollectionUtils.isEmpty(this.addPublishers)) {
      publishers = Collections.emptyList();
    } else {
      publishers =
          this.addPublishers.stream()
              .map(
                  publisher -> {
                    final String cell = WordCache.getWordCache(publisher.getCell());
                    return new SubPublisher(
                        publisher.getRegisterId(),
                        cell,
                        publisher.getDataList(),
                        publisher.getClientId(),
                        publisher.getVersion(),
                        publisher.getSrcAddressString(),
                        publisher.getRegisterTimestamp(),
                        publisher.getPublishSource());
                  })
              .collect(Collectors.toList());
    }

    Map<String, List<String>> registerIds;
    if (MapUtils.isEmpty(this.deletePublisherRegisterIds)) {
      registerIds = Collections.emptyMap();
    } else {
      registerIds = new HashMap<>(this.deletePublisherRegisterIds.size());
      for (Map.Entry<String, List<String>> entry : this.deletePublisherRegisterIds.entrySet()) {
        String zone = entry.getKey();
        List<String> zoneDeleteRegisterIds = entry.getValue();
        registerIds.put(zone, new ArrayList<>(zoneDeleteRegisterIds));
      }
    }

    return new SubDatumRevisionData(
        WordCache.getWordCache(this.dataCenter),
        WordCache.getWordCache(this.dataInfoId),
        this.version,
        publishers,
        registerIds);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
