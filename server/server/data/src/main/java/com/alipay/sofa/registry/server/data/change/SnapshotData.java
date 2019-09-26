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
package com.alipay.sofa.registry.server.data.change;

import java.util.Map;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.alipay.sofa.registry.common.model.store.Publisher;

/**
 * changed data
 *
 * @author kezhu.wukz
 * @version $Id: ChangeData.java, v 0.1 2019-07-12 16:23 kezhu.wukz Exp $
 */
public class SnapshotData extends ChangeData {

    private String                 dataInfoId;

    private Map<String, Publisher> toBeDeletedPubMap;

    private Map<String, Publisher> snapshotPubMap;

    public SnapshotData(String dataInfoId, Map<String, Publisher> toBeDeletedPubMap,
                        Map<String, Publisher> snapshotPubMap) {
        super(null, 0, DataSourceTypeEnum.SNAPSHOT, null);
        this.dataInfoId = dataInfoId;
        this.toBeDeletedPubMap = toBeDeletedPubMap;
        this.snapshotPubMap = snapshotPubMap;
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
     * Getter method for property <tt>toBeDeletedPubMap</tt>.
     *
     * @return property value of toBeDeletedPubMap
     */
    public Map<String, Publisher> getToBeDeletedPubMap() {
        return toBeDeletedPubMap;
    }

    /**
     * Getter method for property <tt>snapshotPubMap</tt>.
     *
     * @return property value of snapshotPubMap
     */
    public Map<String, Publisher> getSnapshotPubMap() {
        return snapshotPubMap;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(Delayed o) {
        return -1;
    }
}