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
package com.alipay.sofa.registry.server.data.change.event;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.cache.UnPublisher;
import com.alipay.sofa.registry.server.data.change.DataChangeTypeEnum;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;

/**
 *
 * @author qian.lqlq
 * @version $Id: DataChangeEventCenter.java, v 0.1 2018-03-09 14:25 qian.lqlq Exp $
 */
public class DataChangeEventCenter {
    private AtomicBoolean          isInited = new AtomicBoolean(false);

    /**
     * count of DataChangeEventQueue
     */
    private int                    queueCount;

    /**
     * queues of DataChangeEvent
     */
    private DataChangeEventQueue[] dataChangeEventQueues;

    @Autowired
    private DataServerConfig       dataServerConfig;

    @Autowired
    private DatumCache             datumCache;

    @PostConstruct
    public void init() {
        if (isInited.compareAndSet(false, true)) {
            queueCount = dataServerConfig.getQueueCount();
            dataChangeEventQueues = new DataChangeEventQueue[queueCount];
            for (int idx = 0; idx < queueCount; idx++) {
                dataChangeEventQueues[idx] = new DataChangeEventQueue(idx, dataServerConfig, this,
                    datumCache);
                dataChangeEventQueues[idx].start();
            }
        }
    }

    /**
     * receive changed publisher, then wrap it into the DataChangeEvent and put it into dataChangeEventQueue
     *
     * @param publisher
     * @param dataCenter
     */
    public void onChange(Publisher publisher, String dataCenter) {
        int idx = hash(publisher.getDataInfoId());
        Datum datum = new Datum(publisher, dataCenter);
        if (publisher instanceof UnPublisher) {
            datum.setContainsUnPub(true);
        }
        if (publisher.getPublishType() != PublishType.TEMPORARY) {
            dataChangeEventQueues[idx].onChange(new DataChangeEvent(DataChangeTypeEnum.MERGE,
                DataSourceTypeEnum.PUB, datum));
        } else {
            dataChangeEventQueues[idx].onChange(new DataChangeEvent(DataChangeTypeEnum.MERGE,
                DataSourceTypeEnum.PUB_TEMP, datum));
        }
    }

    /**
     *
     * @param event
     */
    public void onChange(ClientChangeEvent event) {
        for (DataChangeEventQueue dataChangeEventQueue : dataChangeEventQueues) {
            dataChangeEventQueue.onChange(event);
        }
    }

    /**
     *
     * @param changeType
     * @param datum
     */
    public void sync(DataChangeTypeEnum changeType, DataSourceTypeEnum sourceType, Datum datum) {
        int idx = hash(datum.getDataInfoId());
        DataChangeEvent event = new DataChangeEvent(changeType, sourceType, datum);
        dataChangeEventQueues[idx].onChange(event);
    }

    /**
     * compute target DataChangeEventQueue
     *
     * @param key
     * @return
     */
    public int hash(String key) {
        if (queueCount > 1) {
            return Math.abs(key.hashCode() % queueCount);
        } else {
            return 0;
        }
    }

    /**
     *
     * @return
     */
    public DataChangeEventQueue[] getQueues() {
        return dataChangeEventQueues;
    }

}