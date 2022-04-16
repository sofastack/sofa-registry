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
package com.alipay.sofa.registry.common.model;

import com.alipay.sofa.registry.common.model.sessionserver.SimpleSubscriber;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.net.InetSocketAddress;
import java.util.*;
import org.apache.commons.collections.CollectionUtils;

public final class SubscriberUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SubscriberUtils.class);

  private SubscriberUtils() {}

  public static Map<InetSocketAddress, Map<String, Subscriber>> groupBySourceAddress(
      Collection<Subscriber> subscribers) {
    if (subscribers.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<InetSocketAddress, Map<String, Subscriber>> ret = Maps.newHashMapWithExpectedSize(128);
    subscribers.forEach(
        s -> {
          InetSocketAddress address =
              new InetSocketAddress(
                  s.getSourceAddress().getIpAddress(), s.getSourceAddress().getPort());
          Map<String, Subscriber> subs = ret.computeIfAbsent(address, k -> Maps.newHashMap());
          subs.put(s.getRegisterId(), s);
        });
    return ret;
  }

  public static Map<Boolean, List<Subscriber>> groupByMulti(List<Subscriber> subscribers) {

    if (subscribers.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Boolean, List<Subscriber>> ret = Maps.newHashMapWithExpectedSize(2);
    for (Subscriber subscriber : subscribers) {
      List<Subscriber> subs =
          ret.computeIfAbsent(subscriber.acceptMulti(), k -> Lists.newArrayList());
      subs.add(subscriber);
    }

    return ret;
  }

  public static Map<ScopeEnum, List<Subscriber>> groupByScope(Collection<Subscriber> subscribers) {
    if (subscribers.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<ScopeEnum, List<Subscriber>> ret = Maps.newHashMap();
    for (Subscriber subscriber : subscribers) {
      final ScopeEnum scopeEnum = subscriber.getScope();
      if (scopeEnum == null) {
        LOGGER.warn("Nil ScopeEnum, {}", subscriber);
        continue;
      }
      List<Subscriber> subList = ret.computeIfAbsent(scopeEnum, k -> Lists.newArrayList());
      subList.add(subscriber);
    }
    return ret;
  }

  public static ScopeEnum getAndAssertHasSameScope(Collection<Subscriber> subscribers) {
    Iterator<Subscriber> iterator = subscribers.iterator();
    Subscriber first = iterator.next();
    ScopeEnum scope = first.getScope();
    while (iterator.hasNext()) {
      Subscriber subscriber = iterator.next();
      if (scope != subscriber.getScope()) {
        throw new RuntimeException(
            StringFormatter.format(
                "conflict scope, one is {}, anther is {}",
                first.shortDesc(),
                subscriber.shortDesc()));
      }
    }
    return scope;
  }

  public static String[] getAndAssertAcceptedEncodes(Collection<Subscriber> subscribers) {
    Iterator<Subscriber> iterator = subscribers.iterator();
    Subscriber first = iterator.next();
    String[] acceptEncodes = first.getAcceptEncodes();
    while (iterator.hasNext()) {
      Subscriber subscriber = iterator.next();
      if (!Arrays.equals(acceptEncodes, subscriber.getAcceptEncodes())) {
        throw new RuntimeException(
            StringFormatter.format(
                "conflict encoding, one is {}, anther is {}",
                first.shortDesc(),
                subscriber.shortDesc()));
      }
    }
    return acceptEncodes;
  }

  public static boolean getAndAssertAcceptMulti(Collection<Subscriber> subscribers) {
    Iterator<Subscriber> iterator = subscribers.iterator();
    Subscriber first = iterator.next();
    boolean acceptMulti = first.acceptMulti();
    while (iterator.hasNext()) {
      Subscriber subscriber = iterator.next();
      if (acceptMulti != subscriber.acceptMulti()) {
        throw new RuntimeException(
            StringFormatter.format(
                "conflict acceptMulti, one is {}, anther is {}",
                first.shortDesc(),
                subscriber.shortDesc()));
      }
    }
    return acceptMulti;
  }

  public static void assertClientVersion(
      Collection<Subscriber> subscribers, BaseInfo.ClientVersion clientVersion) {
    for (Subscriber sub : subscribers) {
      ParaCheckUtil.checkEquals(sub.getClientVersion(), clientVersion, "subscriber.clientVersion");
    }
  }

  public static long getMaxPushedVersion(String dataCenter, Collection<Subscriber> subscribers) {
    long max = 0;
    for (Subscriber sub : subscribers) {
      long v = sub.getPushedVersion(dataCenter);
      if (max < v) {
        max = v;
      }
    }
    return max;
  }

  public static long getMinRegisterTimestamp(Collection<Subscriber> subscribers) {
    long min = Long.MAX_VALUE;
    for (Subscriber sub : subscribers) {
      long v = sub.getRegisterTimestamp();
      if (min > v) {
        min = v;
      }
    }
    return min;
  }

  public static SimpleSubscriber convert(Subscriber subscriber) {
    return new SimpleSubscriber(
        subscriber.getClientId(),
        subscriber.getSourceAddress().buildAddressString(),
        subscriber.getAppName());
  }

  public static List<SimpleSubscriber> convert(Collection<Subscriber> subscribers) {
    if (CollectionUtils.isEmpty(subscribers)) {
      return Collections.emptyList();
    }
    List<SimpleSubscriber> ret = Lists.newArrayListWithCapacity(subscribers.size());
    for (Subscriber s : subscribers) {
      ret.add(convert(s));
    }
    return ret;
  }
}
