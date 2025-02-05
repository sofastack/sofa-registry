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

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.store.engine.SimpleMemoryStoreEngine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  public Tuple<Map<String, DatumVersion>, List<Subscriber>> selectSubscribers(String dataCenter) {
    final String localDataCenter = sessionServerConfig.getSessionServerDataCenter();
    final boolean isLocalDataCenter = localDataCenter.equals(dataCenter);

    final Map<String, DatumVersion> versions = new HashMap<>();
    final List<Subscriber> toPushEmptySubscribers = new ArrayList<>();

    Collection<String> dataInfoIdCollection = getNonEmptyDataInfoId();
    if (dataInfoIdCollection == null || dataInfoIdCollection.size() == 0) {
      return Tuple.of(versions, toPushEmptySubscribers);
    }
    for (String dataInfoId : dataInfoIdCollection) {
      Collection<Subscriber> subscribers = getByDataInfoId(dataInfoId);
      if (CollectionUtils.isEmpty(subscribers)) {
        continue;
      }

      long maxVersion = 0;
      for (Subscriber sub : subscribers) {
        // not global sub and not local dataCenter, not interest the other dataCenter's pub
        if (sub.getScope() != ScopeEnum.global && !isLocalDataCenter) {
          continue;
        }
        if (sub.isMarkedPushEmpty(dataCenter)) {
          if (sub.needPushEmpty(dataCenter)) {
            toPushEmptySubscribers.add(sub);
          }
          continue;
        }
        final long pushVersion = sub.getPushedVersion(dataCenter);
        if (maxVersion < pushVersion) {
          maxVersion = pushVersion;
        }
      }
      versions.put(dataInfoId, new DatumVersion(maxVersion));
    }
    return Tuple.of(versions, toPushEmptySubscribers);
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
