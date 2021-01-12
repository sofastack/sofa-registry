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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alipay.sofa.registry.common.model.AppRegisterServerDataBox;
import com.alipay.sofa.registry.common.model.PublisherInternUtil;
import com.alipay.sofa.registry.common.model.store.AppPublisher;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.WordCache;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * datum store in dataserver
 *
 * @author qian.lqlq
 * @version $Id: Datum.java, v 0.1 2017-12-07 11:19 qian.lqlq Exp $
 */
public class Datum implements Serializable {

    private static final long            serialVersionUID = 5307489721610438103L;

    private String                       dataInfoId;

    private String                       dataCenter;

    private String                       dataId;

    private String                       instanceId;

    private String                       group;
    //key=registerId
    private final Map<String, Publisher> pubMap           = Maps.newHashMap();

    private long                         version;

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
        addPublisher(publisher);
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
        addPublisher(publisher);
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

    public static Datum internDatum(Datum datum) {
        datum.setDataCenter(datum.getDataCenter());
        datum.setDataInfoId(datum.getDataInfoId());
        datum.setDataId(datum.getDataId());
        datum.setGroup(datum.getGroup());
        datum.setInstanceId(datum.getInstanceId());

        synchronized (datum) {
            datum.pubMap.forEach((registerId, publisher) -> {
                // let registerId == pub.getRegisterId in every <registerId, pub>, for reducing old gen memory
                // because this Datum is put into Memory directly, by DatumCache.coverDatum
                publisher.setRegisterId(registerId);
                // change publisher word cache
                PublisherInternUtil.internPublisher(publisher);
            });
        }

        return datum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("Datum={").append(dataInfoId).append(", dataCenter=").append(dataCenter)
            .append(", size=").append(publisherSize()).append(", ver=").append(version).append('}');
        return sb.toString();
    }

    public synchronized boolean addPublisher(Publisher publisher) {
        Publisher existing = pubMap.computeIfAbsent(publisher.getRegisterId(), k -> publisher);
        if (existing == publisher) {
            return true;
        }
        if (!existing.publisherVersion().orderThan(publisher.publisherVersion())) {
            return false;
        }
        pubMap.put(publisher.getRegisterId(), publisher);
        return true;
    }

    public synchronized int publisherSize() {
        return pubMap.size();
    }

    public synchronized void addPublishers(Map<String, Publisher> publisherMap) {
        if (publisherMap != null) {
            publisherMap.values().forEach(p->addPublisher(p));
        }
    }

    public synchronized Map<String, Publisher> getPubMap() {
        return Collections.unmodifiableMap(Maps.newHashMap(pubMap));
    }

    /**
     * should not call that, just for json serde
     */
    public synchronized void setPubMap(Map<String, Publisher> pubMap) {
        this.pubMap.clear();
        if (pubMap != null) {
            this.pubMap.putAll(pubMap);
        }
    }

    public synchronized Set<String> revisions() {
        Set<String> revisions = Sets.newHashSet();

        for (Publisher publisher : pubMap.values()) {
            if (publisher instanceof AppPublisher) {
                AppPublisher appPublisher = (AppPublisher) publisher;
                for (AppRegisterServerDataBox dataBox : appPublisher.getAppDataList()) {
                    revisions.add(dataBox.getRevision());
                }
            }
        }
        return revisions;
    }

}
