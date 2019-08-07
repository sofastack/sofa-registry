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

import com.alipay.sofa.registry.common.model.ElementType;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author shangyu.wh
 * @version $Id: Subscriber.java, v 0.1 2017-11-30 16:03 shangyu.wh Exp $
 */
public class Subscriber extends BaseInfo {

    /** UID */
    private static final long                serialVersionUID = 98433360274932292L;
    /** */
    private ScopeEnum                        scope;
    /** */
    private ElementType                      elementType;

    /**
     * all dataCenter push dataInfo version
     */
    private Map<String/*dataCenter*/, Long> lastPushVersions = new ConcurrentHashMap<>();

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

        Long oldVersion = lastPushVersions.get(dataCenter);
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

        while (true) {
            Long oldVersion = lastPushVersions.putIfAbsent(dataCenter, version);
            // Add firstly
            if (oldVersion == null) {
                break;
            } else {
                if (version > oldVersion) {
                    if (lastPushVersions.replace(dataCenter, oldVersion, version)) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
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
        sb.append("lastPushVersion=").append(lastPushVersions);
        return sb.toString();
    }

    /**
     * Getter method for property <tt>lastPushVersions</tt>.
     *
     * @return property value of lastPushVersions
     */
    public Map<String, Long> getLastPushVersions() {
        return lastPushVersions;
    }

    /**
     * Setter method for property <tt>lastPushVersions </tt>.
     *
     * @param lastPushVersions  value to be assigned to property lastPushVersions
     */
    public void setLastPushVersions(Map<String, Long> lastPushVersions) {
        this.lastPushVersions = lastPushVersions;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Subscriber{");
        sb.append("scope=").append(scope);
        sb.append(", elementType=").append(elementType);
        sb.append(", lastPushVersions=").append(lastPushVersions);
        sb.append(", super=").append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}