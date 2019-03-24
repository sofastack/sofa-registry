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
package com.alipay.sofa.registry.common.model.metaserver;

import java.io.Serializable;

/**
 *
 * @author shangyu.wh
 * @version $Id: NotifyProvideDataChange.java, v 0.1 2018-04-18 15:18 shangyu.wh Exp $
 */
public class NotifyProvideDataChange implements Serializable {

    private String       dataInfoId;

    private Long         version;

    private DataOperator dataOperator;

    /**
     * constructor
     * @param dataInfoId
     * @param version
     * @param dataOperator
     */
    public NotifyProvideDataChange(String dataInfoId, Long version, DataOperator dataOperator) {
        this.dataInfoId = dataInfoId;
        this.version = version;
        this.dataOperator = dataOperator;
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
        this.dataInfoId = dataInfoId;
    }

    /**
     * Getter method for property <tt>version</tt>.
     *
     * @return property value of version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Setter method for property <tt>version</tt>.
     *
     * @param version  value to be assigned to property version
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Getter method for property <tt>dataOperator</tt>.
     *
     * @return property value of dataOperator
     */
    public DataOperator getDataOperator() {
        return dataOperator;
    }

    /**
     * Setter method for property <tt>dataOperator</tt>.
     *
     * @param dataOperator  value to be assigned to property dataOperator
     */
    public void setDataOperator(DataOperator dataOperator) {
        this.dataOperator = dataOperator;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NotifyProvideDataChange{");
        sb.append("dataInfoId='").append(dataInfoId).append('\'');
        sb.append(", version=").append(version);
        sb.append(", dataOperator=").append(dataOperator);
        sb.append('}');
        return sb.toString();
    }
}