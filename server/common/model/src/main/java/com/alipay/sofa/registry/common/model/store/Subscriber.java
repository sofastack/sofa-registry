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
package com.alipay.sofa.registry.common.model.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alipay.sofa.registry.common.model.ElementType;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author shangyu.wh
 * @version $Id: Subscriber.java, v 0.1 2017-11-30 16:03 shangyu.wh Exp $
 */
public class Subscriber extends BaseInfo {

    /** UID */
    private static final long serialVersionUID = 98433360274932292L;
    /** */
    private ScopeEnum         scope;
    /** */
    private ElementType       elementType;

    static class PushContext {
        /**
         * push dataInfo version
         */
        private Long lastPushVersion;

        /**
         * push dataInfo version
         */
        private int  lastPubCount;

        public PushContext(Long lastPushVersion, int lastPubCount) {
            this.lastPushVersion = lastPushVersion;
            this.lastPubCount = lastPubCount;
        }
    }

    /**
     * all dataCenter push dataInfo version
     */
    private Map<String/*dataCenter*/, PushContext> lastPushContexts = new ConcurrentHashMap<>();

    /**
     * Getter method for property <tt>scope</tt>.
     *
     * @return property value of scope
     */
    public ScopeEnum getScope() {
        return scope;
    }

    /**
     * Setter method for property <tt>scope</tt>.
     *
     * @param scope value to be assigned to property scope
     */
    public void setScope(ScopeEnum scope) {
        this.scope = scope;
    }

    public ElementType getElementType() {
        return elementType;
    }

    /**
     * check version input greater than current version
     * @param version
     * @return
     */
    public boolean checkVersion(String dataCenter, Long version) {

        PushContext pushContext = lastPushContexts.get(dataCenter);
        if (pushContext == null) {
            return version != null;
        }
        Long oldVersion = pushContext.lastPushVersion;
        if (oldVersion == null) {
            return version != null;
        } else {
            if (version != null) {
                return version > oldVersion;
            }
            return false;
        }
    }

    /**
     * check version input greater or equal to current version
     * @param version
     * @return
     */
    public void checkAndUpdateVersion(String dataCenter, Long version) {
        checkAndUpdateVersion(dataCenter, version, -1);
    }

    /**
     * check version input greater or equal to current version
     * @param version
     * @return
     */
    public void checkAndUpdateVersion(String dataCenter, Long version, int pubCount) {

        while (true) {
            PushContext pushContext = new PushContext(version, pubCount);
            PushContext oldPushContext = lastPushContexts.putIfAbsent(dataCenter, pushContext);
            // Add firstly
            if (oldPushContext == null) {
                break;
            } else {
                if (oldPushContext.lastPushVersion == null
                    || (pushContext.lastPushVersion != null && pushContext.lastPushVersion > oldPushContext.lastPushVersion)) {
                    if (lastPushContexts.replace(dataCenter, oldPushContext, pushContext)) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }

    /**
     * http://gitlab.alipay-inc.com/sofa-open/sofa-registry/issues/189
     */
    public boolean shouldPush(String dataCenter, int pubCount) {
        // condition of no push:
        // 1. last push count is 0 and this time is also 0
        // 2. last push is a valid push
        if (pubCount == 0) {
            PushContext pushContext = lastPushContexts.get(dataCenter);
            return !(pushContext != null && pushContext.lastPubCount == 0
            //last push is a valid push
                     && pushContext.lastPushVersion != null && pushContext.lastPushVersion > ValueConstants.DEFAULT_NO_DATUM_VERSION);
        }
        return true;
    }

    /**
     * Setter method for property <tt>elementType</tt>.
     *
     * @param elementType  value to be assigned to property elementType
     */
    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    @Override
    @JsonIgnore
    public DataType getDataType() {
        return DataType.SUBSCRIBER;
    }

    @Override
    protected String getOtherInfo() {
        final StringBuilder sb = new StringBuilder("scope=");
        sb.append(scope).append(",");
        sb.append("elementType=").append(elementType).append(",");
        sb.append("lastPushVersion=").append(lastPushContexts);
        return sb.toString();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Subscriber{");
        sb.append("scope=").append(scope);
        sb.append(", elementType=").append(elementType);
        sb.append(", lastPushContexts=").append(lastPushContexts);
        sb.append(", super=").append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}