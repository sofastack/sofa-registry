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
package com.alipay.sofa.registry.common.model.sessionserver;

import com.alipay.sofa.registry.common.model.store.WordCache;

import java.io.Serializable;

/**
 * request to notify sessionserver when data changed
 *
 * @author qian.lqlq
 * @version $Id: DataChangeRequest.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class DataChangeRequest implements Serializable {

    private static final long serialVersionUID = -7674982522990222894L;

    private String            dataInfoId;

    private String            dataCenter;

    private long              version;

    /**
     * constructor
     */
    public DataChangeRequest() {
    }

    /**
     * constructor
     * @param dataInfoId
     * @param dataCenter
     * @param version
     */
    public DataChangeRequest(String dataInfoId, String dataCenter, long version) {
        this.dataInfoId = dataInfoId;
        this.dataCenter = dataCenter;
        this.version = version;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataChangeRequest{");
        sb.append("dataInfoId='").append(dataInfoId).append('\'');
        sb.append(", dataCenter='").append(dataCenter).append('\'');
        sb.append(", version=").append(version);
        sb.append('}');
        return sb.toString();
    }

}
