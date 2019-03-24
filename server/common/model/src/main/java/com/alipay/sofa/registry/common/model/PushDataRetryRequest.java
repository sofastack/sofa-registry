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

import com.alipay.sofa.registry.common.model.store.URL;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author shangyu.wh
 * @version $Id: PushDataRetryRequest.java, v 0.1 2019-02-20 14:47 shangyu.wh Exp $
 */
public class PushDataRetryRequest<T> {

    private AtomicInteger retryTimes = new AtomicInteger();

    private final T       pushObj;
    private final URL     url;

    public PushDataRetryRequest(T pushObj, URL url) {
        this.pushObj = pushObj;
        this.url = url;
    }

    /**
     * Getter method for property <tt>retryTimes</tt>.
     *
     * @return property value of retryTimes
     */
    public AtomicInteger getRetryTimes() {
        return retryTimes;
    }

    /**
     * Getter method for property <tt>pushObj</tt>.
     *
     * @return property value of pushObj
     */
    public T getPushObj() {
        return pushObj;
    }

    /**
     * Getter method for property <tt>url</tt>.
     *
     * @return property value of url
     */
    public URL getUrl() {
        return url;
    }
}