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
package com.alipay.sofa.registry.server.data.event;

import com.alipay.sofa.registry.server.data.cache.SessionServerChangeItem;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-04 17:35 yuzhi.lyz Exp $
 */
public class SessionServerChangeEvent implements Event {

    private SessionServerChangeItem sessionServerChangeItem;

    private FromType                fromType;

    public SessionServerChangeEvent(SessionServerChangeItem sessionServerChangeItem,
                                    FromType fromType) {
        this.sessionServerChangeItem = sessionServerChangeItem;
        this.fromType = fromType;
    }

    /**
     * Getter method for property <tt>sessionServerChangeItem</tt>.
     *
     * @return property value of sessionServerChangeItem
     */
    public SessionServerChangeItem getSessionServerChangeItem() {
        return sessionServerChangeItem;
    }

    /**
     * Getter method for property <tt>fromType</tt>.
     *
     * @return property value of fromType
     */
    public FromType getFromType() {
        return fromType;
    }
}
