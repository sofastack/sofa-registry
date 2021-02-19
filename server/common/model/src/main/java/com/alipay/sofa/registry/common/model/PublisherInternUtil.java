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

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;

import java.util.*;

/**
 * @author xiaojian.xj
 * @version $Id: PublisherInternUtil.java, v 0.1 2020年11月12日 16:53 xiaojian.xj Exp $
 */
public class PublisherInternUtil {

    /**
     * change publisher word cache
     *
     * @param publisher
     * @return
     */
    public static Publisher internPublisher(Publisher publisher) {
        publisher.setRegisterId(publisher.getRegisterId());
        publisher.setDataInfoId(publisher.getDataInfoId());
        publisher.setInstanceId(publisher.getInstanceId());
        publisher.setGroup(publisher.getGroup());
        publisher.setDataId(publisher.getDataId());
        publisher.setClientId(publisher.getClientId());
        publisher.setCell(publisher.getCell());
        publisher.setProcessId(publisher.getProcessId());
        publisher.setAppName(publisher.getAppName());
        return publisher;
    }
}