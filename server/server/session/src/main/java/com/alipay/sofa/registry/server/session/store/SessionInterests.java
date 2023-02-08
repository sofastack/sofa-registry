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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry.SelectSubscriber;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author shangyu.wh
 * @version $Id: AbstractSessionInterests.java, v 0.1 2017-11-30 20:42 shangyu.wh Exp $
 */
public class SessionInterests extends AbstractDataManager<Subscriber> implements Interests {

  private static final Logger LOGGER = LoggerFactory.getLogger(SessionInterests.class);

  public SessionInterests() {
    super(LOGGER);
  }

  private final Store<Subscriber> store = new SimpleStore<>(1024 * 16, 256);

  @Override
  public boolean add(Subscriber subscriber) {
    ParaCheckUtil.checkNotNull(subscriber.getScope(), "subscriber.scope");
    ParaCheckUtil.checkNotNull(subscriber.getClientVersion(), "subscriber.clientVersion");

    Subscriber.internSubscriber(subscriber);
    Tuple<Subscriber, Boolean> ret = addData(subscriber);
    return ret.o2;
  }

  @Override
  public InterestVersionCheck checkInterestVersion(
      String dataCenter, String datumDataInfoId, long version) {
    Collection<Subscriber> subscribers = getInterests(datumDataInfoId);
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

  @Override
  public Collection<Subscriber> getInterests(String datumDataInfoId) {
    return getDatas(datumDataInfoId);
  }

  @Override
  public SelectSubscriber selectSubscribers(Set<String> dataCenters) {
    final String localDataCenter = sessionServerConfig.getSessionServerDataCenter();

    Store<Subscriber> store = getStore();
    int dataInfoIdSize = store.getDataInfoIds().size();
    final Map<String, Map<String, DatumVersion>> versions =
        Maps.newHashMapWithExpectedSize(dataCenters.size());
    final Map<String, List<Subscriber>> toPushEmptySubscribers =
        Maps.newHashMapWithExpectedSize(dataCenters.size());
    final List<Subscriber> toRegisterMultiSubscribers = Lists.newArrayListWithCapacity(128);

    for (String dataCenter : dataCenters) {
      versions.put(dataCenter, Maps.newHashMapWithExpectedSize(dataInfoIdSize));
      toPushEmptySubscribers.put(dataCenter, Lists.newArrayListWithCapacity(256));
    }

    store.forEach(
        (String dataInfoId, Map<String, Subscriber> subs) -> {
          if (CollectionUtils.isEmpty(subs)) {
            return;
          }
          for (Subscriber sub : subs.values()) {

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
        });
    return new SelectSubscriber(versions, toPushEmptySubscribers, toRegisterMultiSubscribers);
  }

  @Override
  protected Store<Subscriber> getStore() {
    return store;
  }

  @Override
  public Map<String, List<String>> filterIPs(String group, int limit) {
    Map<String, List<String>> ret = Maps.newHashMapWithExpectedSize(1024);
    forEach(
        (String dataInfoId, Map<String, Subscriber> subs) -> {
          if (CollectionUtils.isEmpty(subs)) {
            return;
          }
          String g = subs.values().stream().findAny().get().getGroup();
          if (!StringUtils.equals(g, group)) {
            return;
          }
          ret.put(
              dataInfoId,
              subs.values().stream()
                  .map(s -> s.getSourceAddress().getIpAddress())
                  .limit(Math.max(limit, 0))
                  .collect(Collectors.toList()));
        });
    return ret;
  }
}
