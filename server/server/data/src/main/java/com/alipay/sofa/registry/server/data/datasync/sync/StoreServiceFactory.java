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
package com.alipay.sofa.registry.server.data.datasync.sync;

import com.alipay.sofa.registry.server.data.datasync.AcceptorStore;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shangyu.wh
 * @version $Id: StoreServiceFactory.java, v 0.1 2018-03-22 15:49 shangyu.wh Exp $
 */
public class StoreServiceFactory implements ApplicationContextAware {

    private static Map<String/*supportType*/, AcceptorStore> storeServiceMap = new HashMap<>();

    /**
     * get AcceptorStore by storeType
     * @param storeType
     * @return
     */
    public static AcceptorStore getStoreService(String storeType) {
        return storeServiceMap.get(storeType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AcceptorStore> map = applicationContext.getBeansOfType(AcceptorStore.class);

        map.forEach((key, value) -> storeServiceMap.put(value.getType(), value));
    }
}