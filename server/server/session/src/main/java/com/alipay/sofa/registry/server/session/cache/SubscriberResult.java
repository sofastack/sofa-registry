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
package com.alipay.sofa.registry.server.session.cache;

import com.alipay.sofa.registry.core.model.ScopeEnum;

/**
 *
 * @author shangyu.wh
 * @version $Id: SubscriberResult.java, v 0.1 2017-12-06 17:16 shangyu.wh Exp $
 */
public class SubscriberResult implements EntityType {

    private final String    dataInfoId;

    private final ScopeEnum scope;

    public SubscriberResult(String dataInfoId, ScopeEnum scope) {
        this.dataInfoId = dataInfoId;
        this.scope = scope;
    }

    @Override
    public String getUniqueKey() {
        StringBuilder sb = new StringBuilder(dataInfoId);
        sb.append(COMMA).append(scope);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        String hashKey = getUniqueKey();
        return hashKey.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SubscriberResult) {
            return getUniqueKey().equals(((SubscriberResult) other).getUniqueKey());
        } else {
            return false;
        }
    }

    /**
     * Getter method for property <tt>dataInfoId</tt>.
     *
     * @return property value of dataInfoId
     */
    public String getDataInfoId() {
        return dataInfoId;
    }

    /**
     * Getter method for property <tt>scope</tt>.
     *
     * @return property value of scope
     */
    public ScopeEnum getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return "SubscriberResult{" + "dataInfoId='" + dataInfoId + '\'' + ", scope=" + scope + '}';
    }
}