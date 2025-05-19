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

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.dataserver.DatumRevisionKey;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author huicha
 * @date 2025/3/13
 */
public class SubDatumRevisions implements Serializable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubDatumRevisions.class);

  private static final long serialVersionUID = 3542969132244999722L;

  private final String dataCenter;

  private final String dataInfoId;

  private final Map<Long /* datum version */, Integer /* index in datumRevisions */> versionIndexes;

  private final List<SubDatumRevision> datumRevisions;

  private final SubDatumRevision oldestDatumRevision;

  private final SubDatumRevision latestDatumRevision;

  public SubDatumRevisions(
      String dataCenter, String dataInfoId, List<SubDatumRevision> datumRevisions) {
    // 因为至少会有一个最新的数据版本，因此这个地方 datumRevisions 不可能为空
    ParaCheckUtil.checkNotEmpty(datumRevisions, "SubDatumRevisions.datumRevisions");
    this.dataCenter = dataCenter;
    this.dataInfoId = dataInfoId;
    this.datumRevisions = datumRevisions;
    this.versionIndexes = this.prepareVersionIndexes();
    this.oldestDatumRevision = datumRevisions.get(0);
    this.latestDatumRevision = datumRevisions.get(datumRevisions.size() - 1);
  }

  private Map<Long, Integer> prepareVersionIndexes() {
    if (CollectionUtils.isEmpty(this.datumRevisions)) {
      return Collections.emptyMap();
    }

    Map<Long, Integer> versionIndexes = new HashMap<>(this.datumRevisions.size());
    for (int index = 0; index < this.datumRevisions.size(); index++) {
      SubDatumRevision datumRevision = this.datumRevisions.get(index);
      if (null == datumRevision) {
        // 这不会发生
        LOGGER.error(
            "[SubDatumRevisions] prepare version index fail, the datum revision is null, data center: {}, data info id: {}, index: {}",
            this.dataCenter,
            this.dataInfoId,
            index);
        continue;
      }

      DatumRevisionKey datumRevisionKey = datumRevision.getDatumRevisionKey();
      if (null == datumRevisionKey) {
        // 这不会发生
        LOGGER.error(
            "[SubDatumRevisions] prepare version index fail, the datum revision key is null, data center: {}, data info id: {}, index: {}",
            this.dataCenter,
            this.dataInfoId,
            index);
        continue;
      }

      long version = datumRevisionKey.getVersion();
      versionIndexes.put(version, index);
    }

    return versionIndexes;
  }

  public String getDataCenter() {
    return dataCenter;
  }

  public String getDataInfoId() {
    return dataInfoId;
  }

  public Map<Long, Integer> getVersionIndexes() {
    return Collections.unmodifiableMap(versionIndexes);
  }

  public List<SubDatumRevision> getDatumRevisions() {
    return Collections.unmodifiableList(datumRevisions);
  }

  /**
   * 假设 startDatumVersion 的索引为 a，finishDatumVersion 的索引为 b 这个方法会尝试访问 (a, b] 的所有 datumRevisionData
   *
   * <p>todo(xidong.rxd): 补全这个方法的注释
   *
   * @param startDatumVersion
   * @param endDatumVersion
   * @param visitor
   * @return
   */
  public Tuple<Boolean, String> tryVisitDatumRevisionDatum(
      long startDatumVersion, long endDatumVersion, Consumer<SubDatumRevisionData> visitor) {
    if (0 == startDatumVersion) {
      // 客户端第一次订阅的时候才会传递 0，这种场景不走增量推送
      return Tuple.of(Boolean.FALSE, "startDatumVersion == 0");
    }

    if (0 == endDatumVersion) {
      // 非法场景，目标版本号不可能是 0，理应不会出现，保证健壮性
      return Tuple.of(Boolean.FALSE, "endDatumVersion == 0");
    }

    if (startDatumVersion >= endDatumVersion) {
      // 传入的 version 不合法
      return Tuple.of(Boolean.FALSE, "startDatumVersion >= finishDatumVersion");
    }

    // 获取 startDatumVersion 和 finishDatumVersion 对应的 datumRevision 的 index
    // 这些 index 可能是不存在的
    Integer startDatumIndex = this.versionIndexes.get(startDatumVersion);
    if (null == startDatumIndex) {
      return Tuple.of(Boolean.FALSE, "startDatumVersion not exist");
    }

    Integer endDatumIndex = this.versionIndexes.get(endDatumVersion);
    if (null == endDatumIndex) {
      return Tuple.of(Boolean.FALSE, "finishDatumVersion not exist");
    }

    if (startDatumIndex >= endDatumIndex) {
      // 理应不出现
      return Tuple.of(Boolean.FALSE, "startDatumIndex >= finishDatumIndex");
    }

    // startDatumVersion 和 endDatumIndex 对应的 datumRevision 的 index 存在，并且其大小合法
    // index 初始为 startDatumIndex + 1，由于 startDatumIndex < endDatumIndex，并且都是自然数
    // 因此一定有 startDatumIndex + 1 <= endDatumIndex 即 index <= endDatumIndex，直接开始遍历就行
    for (int index = startDatumIndex + 1; index <= endDatumIndex; index++) {
      // 因为 startIndex 和 endDatumIndex 都是初始化的时候整理的，因此必然是在 datumRevisions 范围内的，这里检查是否超过数组范围
      // 同样的，index 上面也做过检查了，因此不会超出范围
      SubDatumRevision datumRevision = this.datumRevisions.get(index);
      if (datumRevision.missData()) {
        return Tuple.of(Boolean.FALSE, "datum revision is missing at " + index);
      }
      visitor.accept(datumRevision.getDatumRevisionData());
    }

    return Tuple.of(Boolean.TRUE, "");
  }

  public boolean latestVersionNewerThanVersion(long version) {
    // 这个 datumRevisionKey 不可能为空，因为创建 SubDatumRevision 的时候会检查这个参数
    DatumRevisionKey datumRevisionKey = this.latestDatumRevision.getDatumRevisionKey();
    return datumRevisionKey.getVersion() >= version;
  }

  public boolean latestVersionOlderThanVersion(long version) {
    // 这个 datumRevisionKey 不可能为空，因为创建 SubDatumRevision 的时候会检查这个参数
    DatumRevisionKey datumRevisionKey = this.latestDatumRevision.getDatumRevisionKey();
    return datumRevisionKey.getVersion() < version;
  }

  public boolean oldestVersionNewerThanVersion(long version) {
    // 这个 datumRevisionKey 不可能为空，因为创建 SubDatumRevision 的时候会检查这个参数
    DatumRevisionKey datumRevisionKey = this.oldestDatumRevision.getDatumRevisionKey();
    return datumRevisionKey.getVersion() >= version;
  }

  public boolean oldestVersionOlderThanVersion(long version) {
    // 这个 datumRevisionKey 不可能为空，因为创建 SubDatumRevision 的时候会检查这个参数
    DatumRevisionKey datumRevisionKey = this.oldestDatumRevision.getDatumRevisionKey();
    return datumRevisionKey.getVersion() < version;
  }

  public SubDatumRevisions intern() {
    List<SubDatumRevision> datumRevisions;
    if (CollectionUtils.isEmpty(this.datumRevisions)) {
      datumRevisions = Collections.emptyList();
    } else {
      datumRevisions =
          this.datumRevisions.stream().map(SubDatumRevision::intern).collect(Collectors.toList());
    }
    return new SubDatumRevisions(
        WordCache.getWordCache(this.dataCenter),
        WordCache.getWordCache(this.dataInfoId),
        datumRevisions);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
