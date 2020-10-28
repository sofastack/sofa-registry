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
package com.alipay.sofa.registry.server.session.assemble;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.AssembleType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author xiaojian.xj
 * @version $Id: DefaultSubscriberAssembleStrategy.java, v 0.1 2020年11月24日 21:33 xiaojian.xj Exp $
 */
public class DefaultSubscriberAssembleStrategy implements SubscriberAssembleStrategy {

    private Map<AssembleType, AssembleService> assembleServiceMap = new ConcurrentHashMap<>();

    @Override
    public void add(AssembleService assembleService) {
        assembleServiceMap.put(assembleService.support(), assembleService);
    }

    @Override
    public Map<String/*datacenter*/, Datum> assembleDatum(AssembleType assembleType,
                                                           Subscriber subscriber) {
        AssembleService service = assembleServiceMap.get(assembleType);

        if (service == null) {
            return null;
        }

        return service.getAssembleDatum(subscriber);

    }

    @Override
    public Datum assembleDatum(AssembleType assembleType, String datacenter, Subscriber subscriber) {
        AssembleService service = assembleServiceMap.get(assembleType);

        if (service == null) {
            return null;
        }

        return service.getAssembleDatum(datacenter, subscriber);
    }
}