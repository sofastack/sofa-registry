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

import java.util.Collection;

import com.alipay.sofa.registry.common.model.store.Publisher;

/**
 *
 * @author kezhu.wukz
 * @author shangyu.wh
 * @version $Id: PublisherDigestUtil.java, v 0.1 2019-05-30 20:58 shangyu.wh Exp $
 */
public class PublisherDigestUtil {

    public static long getDigestValueSum(Collection<Publisher> publishers) {
        long digest = 0L;
        if (publishers != null && !publishers.isEmpty()) {
            for (Publisher publisher : publishers) {
                digest += getDigestValue(publisher);
            }
        }
        return digest;
    }

    public static long getDigestValue(Publisher publisher) {
        String registerId = publisher.getRegisterId();
        Long version = publisher.getVersion();
        long registerTimestamp = publisher.getRegisterTimestamp();
        long result = registerId != null ? registerId.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (int) (registerTimestamp ^ (registerTimestamp >>> 32));
        return result;
    }
}