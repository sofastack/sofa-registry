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
package com.alipay.sofa.registry.server.data.cache;

import com.alipay.sofa.registry.common.model.store.Publisher;
import java.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author huicha
 * @date 2025/3/12
 */
public class PublisherChanges {

  private static final Logger LOGGER = LoggerFactory.getLogger(PublisherChanges.class);

  public static PublisherChanges empty() {
    return new PublisherChanges(Collections.emptyList(), Collections.emptyMap());
  }

  public static PublisherChanges of(
      List<Publisher> addPublishers, Map<String, List<String>> deletePublisherRegisterIds) {
    return new PublisherChanges(addPublishers, deletePublisherRegisterIds);
  }

  public static PublisherChanges of(Map<String, List<String>> deletePublisherRegisterIds) {
    return new PublisherChanges(Collections.emptyList(), deletePublisherRegisterIds);
  }

  public static PublisherChanges parse(List<Publisher> changedPublishers) {
    if (CollectionUtils.isEmpty(changedPublishers)) {
      return PublisherChanges.empty();
    }

    List<Publisher> addPublishers = new ArrayList<>();
    Map<String, List<String>> deletePublisherRegisterIds = new HashMap<>();
    for (Publisher changedPublisher : changedPublishers) {
      switch (changedPublisher.getDataType()) {
        case PUBLISHER:
          addPublishers.add(changedPublisher);
          break;
        case UN_PUBLISHER:
          String zone = changedPublisher.getCell();
          List<String> zoneDeleteRegisterIds =
              deletePublisherRegisterIds.computeIfAbsent(zone, key -> new ArrayList<>());
          zoneDeleteRegisterIds.add(changedPublisher.getRegisterId());
          break;
        default:
          // 不可能会到这里，这里只记录个日志
          LOGGER.error(
              "[DatumRevisionStorage] Invalid publisher type: {}, ignore it.",
              changedPublisher.getDataType().name());
      }
    }

    return PublisherChanges.of(addPublishers, deletePublisherRegisterIds);
  }

  private List<Publisher> addPublishers;

  private Map<String /* zone */, List<String> /* register id */> deletePublisherRegisterIds;

  private PublisherChanges(
      List<Publisher> addPublishers, Map<String, List<String>> deletePublisherRegisterIds) {
    this.addPublishers = addPublishers;
    this.deletePublisherRegisterIds = deletePublisherRegisterIds;
  }

  public List<Publisher> getAddPublishers() {
    return addPublishers;
  }

  public void setAddPublishers(List<Publisher> addPublishers) {
    this.addPublishers = addPublishers;
  }

  public Map<String, List<String>> getDeletePublisherRegisterIds() {
    return deletePublisherRegisterIds;
  }

  public void setDeletePublisherRegisterIds(Map<String, List<String>> deletePublisherRegisterIds) {
    this.deletePublisherRegisterIds = deletePublisherRegisterIds;
  }
}
