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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.alipay.sofa.registry.common.model.ElementType;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.core.model.AssembleType;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

/**
 * @author shangyu.wh
 * @version $Id: Subscriber.java, v 0.1 2017-11-30 16:03 shangyu.wh Exp $
 */
public class Subscriber extends BaseInfo {

    /**
     * UID
     */
    private static final long                             serialVersionUID = 98433360274932292L;
    /**
     *
     */
    private ScopeEnum                                     scope;
    /**
     *
     */
    private ElementType                                   elementType;
    /**
     *
     */
    private AssembleType                                  assembleType;

    /**
     * last push context
     */
    private final Map<String/*dataCenter*/, PushContext> lastPushContexts = new HashMap<>(4);

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
     * Getter method for property <tt>assembleType</tt>.
     *
     * @return property value of assembleType
     */
    public AssembleType getAssembleType() {
        return assembleType;
    }

    /**
     * Setter method for property <tt>assembleType</tt>.
     *
     * @param assembleType value to be assigned to property assembleType
     */
    public void setAssembleType(AssembleType assembleType) {
        this.assembleType = assembleType;
    }

    public synchronized Set<String> getPushedDataInfoIds() {
        final Set<String> ret = new HashSet<>(4);
        for (PushContext ctx : lastPushContexts.values()) {
            ret.addAll(ctx.pushDatums.keySet());
        }
        return ret;
    }

    public synchronized boolean checkVersion(String dataCenter, Map<String, Long> datumVersions) {
        final PushContext ctx = lastPushContexts.get(dataCenter);
        if (ctx == null || ctx.fetchSeqEnd == 0) {
            return true;
        }

        // has diff dataInfoId
        if (!datumVersions.keySet().equals(ctx.pushDatums.keySet())) {
            return true;
        }
        for (Map.Entry<String, Long> version : datumVersions.entrySet()) {
            DatumPushContext datumCtx = ctx.pushDatums.get(version.getKey());
            if (datumCtx.version < version.getValue()) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean checkAndUpdateVersion(String dataCenter, long pushVersion, Map<String, Long> datumVersion,
                                                      long fetchSeqStart, long fetchSeqEnd) {
        final PushContext ctx = lastPushContexts.computeIfAbsent(dataCenter, k -> new PushContext());

        if (ctx.pushVersion >= pushVersion || ctx.fetchSeqEnd >= fetchSeqStart) {
            return false;
        }
        ctx.pushVersion = pushVersion;
        ctx.fetchSeqEnd = fetchSeqEnd;
        ctx.update(datumVersion);
        return true;
    }

    public synchronized boolean checkVersion(String dataCenter, long fetchSeqStart) {
        final PushContext ctx = lastPushContexts.get(dataCenter);
        if (ctx == null || ctx.fetchSeqEnd < fetchSeqStart) {
            return true;
        }
        return false;
    }

    /**
     * If the pushed data is empty, check the last push, for avoid continuous empty datum push
     */
    public boolean allowPush(String dataCenter, int pubCount) {
        // TODO
        //        boolean allowPush = true;
        //        // condition of no push:
        //        // 1. last push count is 0 and this time is also 0
        //        // 2. last push is a valid push (version > 1)
        //        if (pubCount == 0) {
        //            PushContext pushContext = lastPushContexts.get(dataCenter);
        //            allowPush = !(pushContext != null && pushContext.pushPubCount == 0
        //            //last push is a valid push
        //                          && pushContext.pushVersion != null && pushContext.pushVersion > ValueConstants.DEFAULT_NO_DATUM_VERSION);
        //        }
        //        return allowPush;
        return true;
    }

    /**
     * Setter method for property <tt>elementType</tt>.
     *
     * @param elementType value to be assigned to property elementType
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
        sb.append("pushVersion=").append(lastPushContexts);
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

    /**
     * change subscriber word cache
     *
     * @param subscriber
     * @return
     */
    public static Subscriber internSubscriber(Subscriber subscriber) {
        subscriber.setRegisterId(subscriber.getRegisterId());
        subscriber.setDataInfoId(subscriber.getDataInfoId());
        subscriber.setInstanceId(subscriber.getInstanceId());
        subscriber.setGroup(subscriber.getGroup());
        subscriber.setDataId(subscriber.getDataId());
        subscriber.setClientId(subscriber.getClientId());
        subscriber.setCell(subscriber.getCell());
        subscriber.setProcessId(subscriber.getProcessId());
        subscriber.setAppName(subscriber.getAppName());

        return subscriber;
    }

    private static class DatumPushContext {
        final long version;

        DatumPushContext(long version) {
            this.version = version;
        }

        @Override
        public String toString() {
            return "DatumPushContext{" + "version=" + version + '}';
        }
    }

    private static class PushContext {
        //dataInfoId as key
        Map<String, DatumPushContext> pushDatums = Collections.emptyMap();
        long                          pushVersion;
        long                          fetchSeqEnd;

        void update(Map<String, Long> datumVersions) {
            Map<String, DatumPushContext> map = new HashMap<>(datumVersions.size());
            datumVersions.forEach((k, v) -> map.put(k, new DatumPushContext(v)));
            this.pushDatums = map;
        }

        @Override
        public String toString() {
            return "PushContext{" + "pushVersion=" + pushVersion + ", fetchSeqEnd=" + fetchSeqEnd
                   + ", pushDatums=" + pushDatums + '}';
        }
    }

}