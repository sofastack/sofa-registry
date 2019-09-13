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
package com.alipay.sofa.registry.common.model.dataserver;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.util.DatumVersionUtil;

/**
 * datum store in dataserver
 *
 * @author qian.lqlq
 * @version $Id: Datum.java, v 0.1 2017-12-07 11:19 qian.lqlq Exp $
 */
public class Datum implements Serializable {

    private static final long                     serialVersionUID = 5307489721610438103L;

    private String                                dataInfoId;

    private String                                dataCenter;

    private String                                dataId;

    private String                                instanceId;

    private String                                group;

    private Map<String/*registerId*/, Publisher> pubMap           = new ConcurrentHashMap<>();

    private long                                  version;

    private boolean                               containsUnPub    = false;

    /**
     * constructor
     */
    public Datum() {
    }

    /**
     * constructor
     * @param dataInfoId
     * @param dataCenter
     */
    public Datum(String dataInfoId, String dataCenter) {
        this.dataInfoId = WordCache.getInstance().getWordCache(dataInfoId);
        this.dataCenter = WordCache.getInstance().getWordCache(dataCenter);
        updateVersion();
    }

    /**
     * constructor
     * @param publisher
     * @param dataCenter
     */
    public Datum(Publisher publisher, String dataCenter) {
        this(publisher.getDataInfoId(), dataCenter);
        this.dataId = publisher.getDataId();
        this.instanceId = publisher.getInstanceId();
        this.group = publisher.getGroup();
        pubMap.put(publisher.getRegisterId(), publisher);
    }

    /**
     * constructor
     * @param publisher
     * @param dataCenter
     * @param version
     */
    public Datum(Publisher publisher, String dataCenter, long version) {
        this.dataInfoId = publisher.getDataInfoId();
        this.dataCenter = WordCache.getInstance().getWordCache(dataCenter);
        this.version = version;
        this.dataId = publisher.getDataId();
        this.instanceId = publisher.getInstanceId();
        this.group = publisher.getGroup();
        pubMap.put(publisher.getRegisterId(), publisher);
    }

    public void updateVersion() {
        this.version = DatumVersionUtil.nextId();
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
     * Setter method for property <tt>dataInfoId</tt>.
     *
     * @param dataInfoId  value to be assigned to property dataInfoId
     */
    public void setDataInfoId(String dataInfoId) {
        this.dataInfoId = WordCache.getInstance().getWordCache(dataInfoId);
    }

    /**
     * Getter method for property <tt>dataCenter</tt>.
     *
     * @return property value of dataCenter
     */
    public String getDataCenter() {
        return dataCenter;
    }

    /**
     * Setter method for property <tt>dataCenter</tt>.
     *
     * @param dataCenter  value to be assigned to property dataCenter
     */
    public void setDataCenter(String dataCenter) {
        this.dataCenter = WordCache.getInstance().getWordCache(dataCenter);
    }

    /**
     * Getter method for property <tt>dataId</tt>.
     *
     * @return property value of dataId
     */
    public String getDataId() {
        return dataId;
    }

    /**
     * Setter method for property <tt>dataId</tt>.
     *
     * @param dataId  value to be assigned to property dataId
     */
    public void setDataId(String dataId) {
        this.dataId = WordCache.getInstance().getWordCache(dataId);
    }

    /**
     * Getter method for property <tt>instanceId</tt>.
     *
     * @return property value of instanceId
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Setter method for property <tt>instanceId</tt>.
     *
     * @param instanceId  value to be assigned to property instanceId
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = WordCache.getInstance().getWordCache(instanceId);
    }

    /**
     * Getter method for property <tt>group</tt>.
     *
     * @return property value of group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Setter method for property <tt>group</tt>.
     *
     * @param group  value to be assigned to property group
     */
    public void setGroup(String group) {
        this.group = WordCache.getInstance().getWordCache(group);
    }

    /**
     * Getter method for property <tt>pubMap</tt>.
     *
     * @return property value of pubMap
     */
    public Map<String, Publisher> getPubMap() {
        return pubMap;
    }

    /**
     * Setter method for property <tt>pubMap</tt>.
     *
     * @param pubMap  value to be assigned to property pubMap
     */
    public void setPubMap(Map<String, Publisher> pubMap) {
        this.pubMap = pubMap;
    }

    /**
     * Getter method for property <tt>version</tt>.
     *
     * @return property value of version
     */
    public long getVersion() {
        return version;
    }

    /**
     * Setter method for property <tt>version</tt>.
     *
     * @param version  value to be assigned to property version
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * Getter method for property <tt>containsUnPub</tt>.
     *
     * @return property value of containsUnPub
     */
    public boolean isContainsUnPub() {
        return containsUnPub;
    }

    /**
     * Setter method for property <tt>containsUnPub</tt>.
     *
     * @param containsUnPub  value to be assigned to property containsUnPub
     */
    public void setContainsUnPub(boolean containsUnPub) {
        this.containsUnPub = containsUnPub;
    }

    public static Datum internDatum(Datum datum) {
        datum.setDataCenter(datum.getDataCenter());
        datum.setDataInfoId(datum.getDataInfoId());
        datum.setDataId(datum.getDataId());
        datum.setGroup(datum.getGroup());
        datum.setInstanceId(datum.getInstanceId());

        Map<String, Publisher> pubMap = datum.getPubMap();
        if (pubMap != null && !pubMap.isEmpty()) {
            pubMap.forEach((registerId, publisher) -> Publisher.internPublisher(publisher));
        }

        return datum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[Datum] dataInfoId=").append(dataInfoId)
            .append(", dataId=").append(dataId).append(", dataCenter=").append(dataCenter)
            .append(", instanceId=").append(instanceId).append(", version=").append(version)
            .append(", pubMap=").append(pubMap);
        return sb.toString();
    }
}
