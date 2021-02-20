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

import com.alipay.sofa.registry.common.model.SubscriberUtils;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author shangyu.wh
 * @version $Id: AbstractSessionInterests.java, v 0.1 2017-11-30 20:42 shangyu.wh Exp $
 */
public class SessionInterests extends AbstractDataManager<Subscriber> implements Interests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionInterests.class);

    public SessionInterests() {
        super(LOGGER);
    }

    @Override
    public boolean add(Subscriber subscriber) {
        ParaCheckUtil.checkNotNull(subscriber.getScope(), "subscriber.scope");
        ParaCheckUtil.checkNotNull(subscriber.getClientVersion(), "subscriber.clientVersion");

        Subscriber.internSubscriber(subscriber);

        Subscriber existingSubscriber = addData(subscriber);

        if (existingSubscriber != null) {
            LOGGER.warn("dups subscriber, {}, {}, exist={}/{}, input={}/{}",
                existingSubscriber.getDataInfoId(), existingSubscriber.getRegisterId(),
                // not use get registerVersion, avoid the subscriber.version is null
                existingSubscriber.getVersion(), existingSubscriber.getRegisterTimestamp(),
                subscriber.getVersion(), existingSubscriber.getRegisterTimestamp());
        }
        return true;
    }

    @Override
    public boolean checkInterestVersion(String dataCenter, String datumDataInfoId, long version) {
        Collection<Subscriber> subscribers = getInterestOfDatum(datumDataInfoId);
        if (CollectionUtils.isEmpty(subscribers)) {
            return false;
        }
        for (Subscriber subscriber : subscribers) {
            if (subscriber.checkVersion(dataCenter, version)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<Subscriber> getInterestOfDatum(String datumDataInfoId) {
        return getDatas(datumDataInfoId);
    }

    @Override
    public Collection<String> getPushedDataInfoIds() {
        List<Subscriber> subscribers = new ArrayList<>(512);
        for (Map<String, Subscriber> e : stores.values()) {
            subscribers.addAll(e.values());
        }
        return SubscriberUtils.getPushedDataInfoIds(subscribers);
    }

    @Override
    public Collection<Subscriber> getInterestNeverPushed() {
        List<Subscriber> subscribers = new ArrayList<>(512);
        for (Map<String, Subscriber> e : stores.values()) {
            for (Subscriber subscriber : e.values()) {
                if (!subscriber.hasPushed()) {
                    subscribers.add(subscriber);
                }
            }
        }
        return subscribers;
    }
}