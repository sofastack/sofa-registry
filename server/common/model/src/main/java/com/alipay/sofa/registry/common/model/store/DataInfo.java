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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.util.ParaCheckUtil;

import javax.xml.bind.ValidationEvent;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataInfo.java, v 0.1 2017-11-30 16:09 shangyu.wh Exp $
 */
public class DataInfo implements Serializable {

    private String             dataInfoId;

    private String             instanceId;

    private String             dataId;

    private String             dataType;

    /** symbol : */
    public static final String DELIMITER    = "#@#";

    public static final int    DATAID_LENTH = 3;

    /**
     * @param instanceId
     * @param dataId
     * @param dataType
     */
    public DataInfo(String instanceId, String dataId, String dataType) {
        this.instanceId = instanceId;
        this.dataId = dataId;
        this.dataType = dataType;
        this.dataInfoId = toDataInfoId(dataId, instanceId, dataType);
    }

    /**
     * @param dataId
     * @param instanceId
     * @param dataType
     * @return
     */
    public static String toDataInfoId(String dataId, String instanceId, String dataType) {
        StringBuilder buf = new StringBuilder();
        if (dataId == null || dataId.isEmpty()) {
            throw new IllegalArgumentException("error dataId:" + dataId);
        }
        if (ValueConstants.DISABLE_DATA_ID_CASE_SENSITIVE) {
            buf.append(dataId.toUpperCase());
        } else {
            buf.append(dataId);
        }

        if (instanceId == null || instanceId.isEmpty()) {
            throw new IllegalArgumentException("error instanceId:" + instanceId);
        }
        buf.append(DELIMITER).append(instanceId);

        if (dataType == null || dataType.isEmpty()) {
            throw new IllegalArgumentException("error dataType:" + dataType);
        }
        buf.append(DELIMITER).append(dataType);

        return buf.toString();
    }

    /**
     *
     * @param dataInfoId
     * @return
     */
    public static DataInfo valueOf(String dataInfoId) {
        ParaCheckUtil.checkNotBlank(dataInfoId, "dataInfoId");
        String[] str = dataInfoId.split(DELIMITER);
        if (str.length != DATAID_LENTH) {
            throw new IllegalArgumentException("dataInfoId input error!");
        }

        return new DataInfo(str[1], str[0], str[2]);
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
        this.instanceId = instanceId;
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
        this.dataId = dataId;
    }

    /**
     * Getter method for property <tt>dataType</tt>.
     *
     * @return property value of dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Setter method for property <tt>dataType</tt>.
     *
     * @param dataType  value to be assigned to property dataType
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DataInfo dataInfo = (DataInfo) o;
        return dataInfoId.equals(dataInfo.dataInfoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataInfoId);
    }

}