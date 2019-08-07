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
package com.alipay.sofa.registry.client.api;

import com.alipay.sofa.registry.client.api.model.UserData;
import com.alipay.sofa.registry.core.model.ScopeEnum;

/**
 * The interface Subscriber multi.
 *
 * @author zhuoyu.sjw
 * @version $Id : Subscriber.java, v 0.1 2017-11-23 14:35 zhuoyu.sjw Exp $$
 */
public interface Subscriber extends Register {

    /**
     * Gets data observer.
     *
     * @return the data observer
     */
    SubscriberDataObserver getDataObserver();

    /**
     * Sets data observer.
     *
     * @param observer the observer
     */
    void setDataObserver(SubscriberDataObserver observer);

    /**
     * Peek data user data multi.
     *
     * @return the user data multi
     */
    UserData peekData();

    /**
     * Gets scope enum.
     *
     * @return the scope enum
     */
    ScopeEnum getScopeEnum();
}
