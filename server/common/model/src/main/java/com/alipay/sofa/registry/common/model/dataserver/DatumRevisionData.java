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
package com.alipay.sofa.registry.common.model.dataserver;

import com.alipay.sofa.registry.common.model.PublisherUtils;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author huicha
 * @date 2025/3/11
 */
public class DatumRevisionData {

  private final String dataCenter;

  private final String dataInfoId;

  private final long version;

  private final List<Publisher> addPublishers;

  private final Map<String /* zone */, List<String> /* register id */> deletePublisherRegisterIds;

  public static DatumRevisionData of(
      String dataCenter,
      String dataInfoId,
      DatumVersion version,
      List<Publisher> addPublishers,
      Map<String, List<String>> deletePublisherRegisterIds) {
    return new DatumRevisionData(
        dataCenter, dataInfoId, version, addPublishers, deletePublisherRegisterIds);
  }

  public static DatumRevisionData empty(
      String dataCenter, String dataInfoId, DatumVersion version) {
    return new DatumRevisionData(
        dataCenter, dataInfoId, version, Collections.emptyList(), Collections.emptyMap());
  }

  public DatumRevisionData(
      String dataCenter,
      String dataInfoId,
      DatumVersion version,
      List<Publisher> addPublishers,
      Map<String, List<String>> deletePublisherRegisterIds) {
    this.dataCenter = WordCache.getWordCache(dataCenter);
    this.dataInfoId = WordCache.getWordCache(dataInfoId);
    this.version = version.getValue();
    // todo(xidong.rxd): 这里是否需要拷贝？？？
    this.addPublishers =
        addPublishers.stream().map(PublisherUtils::clonePublisher).collect(Collectors.toList());
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

  public List<Publisher> getAddPublishers() {
    return addPublishers;
  }

  public Map<String, List<String>> getDeletePublisherRegisterIds() {
    return deletePublisherRegisterIds;
  }

  public boolean isEmpty() {
    return CollectionUtils.isEmpty(this.addPublishers)
        && MapUtils.isEmpty(this.deletePublisherRegisterIds);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
