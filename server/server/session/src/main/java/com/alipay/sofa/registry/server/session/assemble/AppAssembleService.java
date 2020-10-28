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
import com.alipay.sofa.registry.server.session.cache.SessionDatumCacheDecorator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 *
 * @author xiaojian.xj
 * @version $Id: AppAssembleService.java, v 0.1 2020年11月24日 19:34 xiaojian.xj Exp $
 */
public class AppAssembleService implements AssembleService {

    @Autowired
    private SessionDatumCacheDecorator sessionDatumCacheDecorator;

    @Override
    public AssembleType support() {
        return AssembleType.sub_app;
    }

    @Override
    public Map<String/*datacenter*/, Datum> getAssembleDatum(Subscriber subscriber) {
        return sessionDatumCacheDecorator.getDatumsCache(subscriber.getDataInfoId());
    }

    @Override
    public Datum getAssembleDatum(String datacenter, Subscriber subscriber) {
        return sessionDatumCacheDecorator.getDatumCache(datacenter, subscriber.getDataInfoId());
    }
}