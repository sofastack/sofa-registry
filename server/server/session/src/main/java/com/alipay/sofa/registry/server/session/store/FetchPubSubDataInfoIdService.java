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
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.sessionserver.PubSubDataInfoIdResp;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : FetchPubSubDataInfoIdService.java, v 0.1 2021年08月04日 11:05 xiaojian.xj Exp $
 */
public class FetchPubSubDataInfoIdService {

  @Autowired private ConnectionsService connectionsService;

  /** store subscribers */
  @Autowired protected Interests sessionInterests;

  /** store publishers */
  @Autowired protected DataStore sessionDataStore;

  public PubSubDataInfoIdResp queryByIps(Set<String> ips) {

    PubSubDataInfoIdResp resp = new PubSubDataInfoIdResp();
    List<ConnectId> connectIds = connectionsService.getIpConnects(ips);
    if (CollectionUtils.isEmpty(connectIds)) {
      return resp;
    }

    Map<ConnectId, Map<String, Publisher>> connectIdPubMap =
        sessionDataStore.queryByConnectIds(Sets.newHashSet(connectIds));
    Map<ConnectId, Map<String, Subscriber>> connectIdSubMap =
        sessionInterests.queryByConnectIds(Sets.newHashSet(connectIds));

    // collect pub and sub dataInfoIds
    Map<String, Set<String>> pubs = parsePubDataInfoIds(connectIdPubMap);
    Map<String, Set<String>> subs = parseSubDataInfoIds(connectIdSubMap);

    resp.setPubDataInfoIds(pubs);
    resp.setSubDataInfoIds(subs);
    return resp;
  }

  protected Map<String, Set<String>> parsePubDataInfoIds(
      Map<ConnectId, Map<String, Publisher>> connectIdPubMap) {
    Map<String, Set<String>> pubs = Maps.newHashMapWithExpectedSize(connectIdPubMap.size());
    for (Entry<ConnectId, Map<String, Publisher>> pubEntry : connectIdPubMap.entrySet()) {
      if (CollectionUtils.isEmpty(pubEntry.getValue())) {
        continue;
      }
      Set<String> set =
          pubs.computeIfAbsent(pubEntry.getKey().getClientHostAddress(), k -> Sets.newHashSet());
      Set<String> collect =
          pubEntry.getValue().values().stream()
              .map(Publisher::getDataInfoId)
              .collect(Collectors.toSet());
      set.addAll(collect);
    }
    return pubs;
  }

  protected Map<String, Set<String>> parseSubDataInfoIds(
      Map<ConnectId, Map<String, Subscriber>> connectIdSubMap) {
    Map<String, Set<String>> subs = Maps.newHashMapWithExpectedSize(connectIdSubMap.size());
    for (Entry<ConnectId, Map<String, Subscriber>> subEntry : connectIdSubMap.entrySet()) {
      if (CollectionUtils.isEmpty(subEntry.getValue())) {
        continue;
      }
      Set<String> set =
          subs.computeIfAbsent(subEntry.getKey().getClientHostAddress(), k -> Sets.newHashSet());
      Set<String> collect =
          subEntry.getValue().values().stream()
              .map(Subscriber::getDataInfoId)
              .collect(Collectors.toSet());
      set.addAll(collect);
    }
    return subs;
  }
}
