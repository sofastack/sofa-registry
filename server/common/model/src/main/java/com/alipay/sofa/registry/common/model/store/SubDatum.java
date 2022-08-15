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

import com.alipay.sofa.registry.cache.Sizer;
import com.alipay.sofa.registry.compress.CompressDatumKey;
import com.alipay.sofa.registry.util.CollectionUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.StringUtils;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.util.Assert;

public final class SubDatum implements Serializable, Sizer {
  private static final long serialVersionUID = 5307489721610438103L;

  private final String dataInfoId;

  private final String dataCenter;

  private final String dataId;

  private final String instanceId;

  private final String group;

  private final List<SubPublisher> publishers;

  private final long version;

  private final List<Long> recentVersions;

  private final ZipSubPublisherList zipPublishers;

  private final int byteSize;
  private final int dataBoxSizeCache;

  public static SubDatum emptyOf(
      String dataInfoId,
      String dataCenter,
      long version,
      String dataId,
      String instanceId,
      String group) {
    return new SubDatum(
        dataInfoId,
        dataCenter,
        version,
        Collections.emptyList(),
        dataId,
        instanceId,
        group,
        Collections.emptyList(),
        null);
  }

  public static SubDatum normalOf(
      String dataInfoId,
      String dataCenter,
      long version,
      Collection<SubPublisher> publishers,
      String dataId,
      String instanceId,
      String group,
      List<Long> recentVersions) {
    return new SubDatum(
        dataInfoId,
        dataCenter,
        version,
        publishers,
        dataId,
        instanceId,
        group,
        recentVersions,
        null);
  }

  public static SubDatum zipOf(
      String dataInfoId,
      String dataCenter,
      long version,
      String dataId,
      String instanceId,
      String group,
      List<Long> recentVersions,
      ZipSubPublisherList zipPublishers) {
    return new SubDatum(
        dataInfoId,
        dataCenter,
        version,
        null,
        dataId,
        instanceId,
        group,
        recentVersions,
        zipPublishers);
  }

  private SubDatum(
      String dataInfoId,
      String dataCenter,
      long version,
      Collection<SubPublisher> publishers,
      String dataId,
      String instanceId,
      String group,
      List<Long> recentVersions,
      ZipSubPublisherList zipPublishers) {

    //        only one must be not null between publishers and zipPublishers
    this.dataInfoId = dataInfoId;
    this.dataCenter = dataCenter;
    this.version = version;
    if (publishers != null) {
      this.publishers = Lists.newArrayList(publishers);
    } else {
      this.publishers = null;
    }
    this.dataId = dataId;
    this.instanceId = instanceId;
    this.group = group;
    this.recentVersions = recentVersions;
    this.zipPublishers = zipPublishers;

    this.byteSize = calcSize();
    this.dataBoxSizeCache = calcDataBoxBytes();
  }

  public String getDataInfoId() {
    return dataInfoId;
  }

  public String getDataCenter() {
    return dataCenter;
  }

  public String getDataId() {
    return dataId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public String getGroup() {
    return group;
  }

  public int getPubNum() {
    if (zipPublishers != null) {
      return zipPublishers.getPubNum();
    }
    return publishers.size();
  }

  public List<SubPublisher> mustGetPublishers() {
    mustUnzipped();
    return publishers;
  }

  public long getVersion() {
    return version;
  }

  private int calcDataBoxBytes() {
    if (zipPublishers != null) {
      return zipPublishers.getOriginSize();
    }
    int bytes = 0;
    bytes += CollectionUtils.fuzzyTotalSize(publishers, SubPublisher::getDataBoxBytes);
    return bytes;
  }

  public int getDataBoxBytes() {
    return dataBoxSizeCache;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "SubDatum{{},{},ver={},num={},bytes={}}",
        dataInfoId,
        dataCenter,
        version,
        getPubNum(),
        getDataBoxBytes());
  }

  public static SubDatum intern(SubDatum datum) {
    if (datum == null) {
      return null;
    }

    final String dataInfoId = WordCache.getWordCache(datum.dataInfoId);
    final String dataCenter = WordCache.getWordCache(datum.dataCenter);

    final String dataId = WordCache.getWordCache(datum.dataId);
    final String instanceId = WordCache.getWordCache(datum.instanceId);
    final String group = WordCache.getWordCache(datum.group);

    List<SubPublisher> publishers = null;
    if (datum.publishers != null) {
      publishers = Lists.newArrayListWithCapacity(datum.mustGetPublishers().size());
      for (SubPublisher publisher : datum.mustGetPublishers()) {
        final String cell = WordCache.getWordCache(publisher.getCell());
        publishers.add(
            new SubPublisher(
                publisher.getRegisterId(),
                cell,
                publisher.getDataList(),
                publisher.getClientId(),
                publisher.getVersion(),
                publisher.getSrcAddressString(),
                publisher.getRegisterTimestamp(),
                publisher.getPublishSource()));
      }
    }
    return new SubDatum(
        dataInfoId,
        dataCenter,
        datum.version,
        publishers,
        dataId,
        instanceId,
        group,
        datum.recentVersions,
        datum.zipPublishers);
  }

  public List<Long> getRecentVersions() {
    return recentVersions;
  }

  private int calcSize() {
    int size = 50;
    size +=
        StringUtils.sizeof(dataInfoId)
            + StringUtils.sizeof(dataCenter)
            + StringUtils.sizeof(dataId)
            + StringUtils.sizeof(instanceId);
    size += CollectionUtils.fuzzyTotalSize(publishers, SubPublisher::size);
    if (zipPublishers != null) {
      size += zipPublishers.size();
    }
    return size;
  }

  @Override
  public int size() {
    return byteSize;
  }

  public CompressDatumKey compressKey(String encoding) {
    return new CompressDatumKey(encoding, dataInfoId, dataCenter, version, getPubNum());
  }

  public ZipSubPublisherList getZipPublishers() {
    return zipPublishers;
  }

  public void mustUnzipped() {
    Assert.notNull(publishers, "publishers must be not null");
  }
}
