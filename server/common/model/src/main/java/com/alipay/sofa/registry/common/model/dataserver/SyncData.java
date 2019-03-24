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
import java.util.Collection;

/**
 *
 * @author shangyu.wh
 * @version $Id: SyncData.java, v 0.1 2018-03-07 17:57 shangyu.wh Exp $
 */
public class SyncData implements Serializable {

    private String            dataInfoId;

    private String            dataCenter;

    private Collection<Datum> datums;

    private boolean           wholeDataTag;

    /**
     * construtor
     * @param dataInfoId
     * @param dataCenter
     * @param wholeDataTag
     * @param datums
     */
    public SyncData(String dataInfoId, String dataCenter, boolean wholeDataTag,
                    Collection<Datum> datums) {
        this.dataInfoId = dataInfoId;
        this.dataCenter = dataCenter;
        this.wholeDataTag = wholeDataTag;
        this.datums = datums;
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
        this.dataCenter = dataCenter;
    }

    /**
     * Getter method for property <tt>datums</tt>.
     *
     * @return property value of datums
     */
    public Collection<Datum> getDatums() {
        return datums;
    }

    /**
     * Setter method for property <tt>datums</tt>.
     *
     * @param datums  value to be assigned to property datums
     */
    public void setDatums(Collection<Datum> datums) {
        this.datums = datums;
    }

    /**
     * Getter method for property <tt>wholeDataTag</tt>.
     *
     * @return property value of wholeDataTag
     */
    public boolean getWholeDataTag() {
        return wholeDataTag;
    }

    /**
     * Setter method for property <tt>wholeDataTag</tt>.
     *
     * @param wholeDataTag  value to be assigned to property wholeDataTag
     */
    public void setWholeDataTag(boolean wholeDataTag) {
        this.wholeDataTag = wholeDataTag;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SyncData{");
        sb.append("dataInfoId='").append(dataInfoId).append('\'');
        sb.append(", dataCenter='").append(dataCenter).append('\'');
        sb.append(", wholeDataTag=").append(wholeDataTag);
        sb.append(", datumsSize=").append(datums != null ? String.valueOf(datums.size()) : "");
        sb.append('}');
        return sb.toString();
    }

}