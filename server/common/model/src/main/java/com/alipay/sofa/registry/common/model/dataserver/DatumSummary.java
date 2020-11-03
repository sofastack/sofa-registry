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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-05 14:27 yuzhi.lyz Exp $
 */
public class DatumSummary implements Serializable {
    private String                           dataInfoId;
    private Map<String/*registerId*/, Long> publisherDigests;

    public DatumSummary(String dataInfoId) {
        this.dataInfoId = dataInfoId;
    }

    /**
     * Getter method for property <tt>dataInfoId</tt>.
     * @return property value of dataInfoId
     */
    public String getDataInfoId() {
        return dataInfoId;
    }

    /**
     * Setter method for property <tt>dataInfoId</tt>.
     * @param dataInfoId value to be assigned to property dataInfoId
     */
    public void setDataInfoId(String dataInfoId) {
        this.dataInfoId = dataInfoId;
    }

    /**
     * Getter method for property <tt>publisherDigests</tt>.
     * @return property value of publisherDigests
     */
    public Map<String, Long> getPublisherDigests() {
        return publisherDigests;
    }

    /**
     * Setter method for property <tt>publisherDigests</tt>.
     * @param publisherDigests value to be assigned to property publisherDigests
     */
    public void setPublisherDigests(Map<String, Long> publisherDigests) {
        this.publisherDigests = publisherDigests;
    }

    @Override
    public String toString() {
        return "DatumSummary{" + "dataInfoId='" + dataInfoId + '\'' + ", publisherDigests="
               + publisherDigests + '}';
    }
}
