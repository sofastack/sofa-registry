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

import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.net.InetSocketAddress;
import java.util.*;

public final class SubscriberUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriberUtils.class);

    private SubscriberUtils() {
    }

    public static Map<InetSocketAddress, Map<String, Subscriber>> groupBySourceAddress(Collection<Subscriber> subscribers) {
        if (subscribers.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<InetSocketAddress, Map<String, Subscriber>> ret = Maps.newHashMap();
        subscribers.forEach(s -> {
            InetSocketAddress address = new InetSocketAddress(s.getSourceAddress()
                    .getIpAddress(), s.getSourceAddress().getPort());
            Map<String, Subscriber> subs = ret.computeIfAbsent(address, k -> Maps.newHashMap());
            subs.put(s.getRegisterId(), s);
        });
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

    public static Set<String> getPushedDataInfoIds(Collection<Subscriber> subscribers) {
        final Set<String> ret = new HashSet<>(256);
        subscribers.forEach(s -> ret.add(s.getDataInfoId()));
        return ret;
    }

    public static ScopeEnum getAndAssertHasSameScope(Collection<Subscriber> subscribers) {
        ScopeEnum scope = subscribers.stream().findFirst().get().getScope();
        for (Subscriber subscriber : subscribers) {
            if (scope != subscriber.getScope()) {
                throw new RuntimeException(String.format("conflict scope, first={}, one is {}",
                    scope, subscriber));
            }
        }
        return scope;
    }
}
