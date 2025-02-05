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

import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.alipay.sofa.registry.server.session.store.engine.SimpleMemoryStoreEngine;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.util.CollectionUtils;

/** Implementation of SubscriberStore. */
public class SubscriberStoreImpl extends AbstractClientStore<Subscriber>
    implements SubscriberStore {

  private final SessionServerConfig sessionServerConfig;

  public SubscriberStoreImpl(SessionServerConfig sessionServerConfig) {
    super(new SimpleMemoryStoreEngine<>(1024 * 16));
    this.sessionServerConfig = sessionServerConfig;
  }

  @Override
  public Map<String, Collection<Subscriber>> query(String group, int limit) {
    return storeEngine.filter(group, limit);
  }

  @Override
  public SessionRegistry.SelectSubscriber selectSubscribers(Set<String> dataCenters) {
    final String localDataCenter = sessionServerConfig.getSessionServerDataCenter();

    final Map<String, Map<String, DatumVersion>> versions = new HashMap<>();
    final Map<String, List<Subscriber>> toPushEmptySubscribers = new HashMap<>();
    final List<Subscriber> toRegisterMultiSubscribers = Lists.newArrayListWithCapacity(128);

    for (String dataCenter : dataCenters) {
      versions.put(dataCenter, new HashMap<>());
      toPushEmptySubscribers.put(dataCenter, Lists.newArrayListWithCapacity(256));
    }

    Collection<String> dataInfoIdCollection = getNonEmptyDataInfoId();
    if (dataInfoIdCollection == null || dataInfoIdCollection.isEmpty()) {
      return new SessionRegistry.SelectSubscriber(
          versions, toPushEmptySubscribers, toRegisterMultiSubscribers);
    }

    for (String dataInfoId : dataInfoIdCollection) {
      Collection<Subscriber> subscribers = getByDataInfoId(dataInfoId);
      if (CollectionUtils.isEmpty(subscribers)) {
        continue;
      }

      for (Subscriber sub : subscribers) {
        if (!sub.hasPushed()) {
          toRegisterMultiSubscribers.add(sub);
          continue;
        }
        for (String dataCenter : dataCenters) {
          Map<String, DatumVersion> vers = versions.get(dataCenter);
          List<Subscriber> pushEmpty = toPushEmptySubscribers.get(dataCenter);

          final boolean isLocalDataCenter = localDataCenter.equals(dataCenter);
          // not multi sub and not local dataCenter, not interest the other dataCenter's pub
          if (!sub.acceptMulti() && !isLocalDataCenter) {
            continue;
          }

          if (sub.isMarkedPushEmpty(dataCenter)) {
            if (sub.needPushEmpty(dataCenter)) {
              pushEmpty.add(sub);
            }
            continue;
          }
          final long pushVersion = sub.getPushedVersion(dataCenter);
          DatumVersion maxVersion = vers.computeIfAbsent(dataInfoId, k -> new DatumVersion(0));
          if (maxVersion.getValue() < pushVersion) {
            vers.put(dataInfoId, new DatumVersion(pushVersion));
          }
        }
      }
    }
    return new SessionRegistry.SelectSubscriber(
        versions, toPushEmptySubscribers, toRegisterMultiSubscribers);
  }

  @Override
  public InterestVersionCheck checkInterestVersion(
      String dataCenter, String datumDataInfoId, long version) {
    Collection<Subscriber> subscribers = getByDataInfoId(datumDataInfoId);
    if (CollectionUtils.isEmpty(subscribers)) {
      return InterestVersionCheck.NoSub;
    }
    for (Subscriber subscriber : subscribers) {
      if (subscriber.checkVersion(dataCenter, version)) {
        return InterestVersionCheck.Interested;
      }
    }
    return InterestVersionCheck.Obsolete;
  }
}
