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
package com.alipay.sofa.registry.server.data.datasync;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;

/**
 *
 * @author shangyu.wh
 * @version $Id: Operator.java, v 0.1 2018-03-05 17:01 shangyu.wh Exp $
 */
public class Operator {

    private Long               version;

    private Long               sourceVersion;

    private Datum              datum;

    private DataSourceTypeEnum sourceType;

    /**
     * constructor
     */
    public Operator() {
    }

    /**
     * constructor
     * @param version
     * @param sourceVersion
     * @param datum
     * @param sourceType
     */
    public Operator(Long version, Long sourceVersion, Datum datum, DataSourceTypeEnum sourceType) {
        this.version = version;
        this.sourceVersion = sourceVersion;
        this.datum = datum;
        this.sourceType = sourceType;
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
     * Getter method for property <tt>sourceVersion</tt>.
     *
     * @return property value of sourceVersion
     */
    public Long getSourceVersion() {
        return sourceVersion;
    }

    /**
     * Setter method for property <tt>sourceVersion</tt>.
     *
     * @param sourceVersion  value to be assigned to property sourceVersion
     */
    public void setSourceVersion(Long sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    /**
     * Getter method for property <tt>datum</tt>.
     *
     * @return property value of datum
     */
    public Datum getDatum() {
        return datum;
    }

    /**
     * Setter method for property <tt>datum</tt>.
     *
     * @param datum  value to be assigned to property datum
     */
    public void setDatum(Datum datum) {
        this.datum = datum;
    }

    /**
     * Getter method for property <tt>sourceType</tt>.
     *
     * @return property value of sourceType
     */
    public DataSourceTypeEnum getSourceType() {
        return sourceType;
    }

    /**
     * Setter method for property <tt>sourceType</tt>.
     *
     * @param sourceType  value to be assigned to property sourceType
     */
    public void setSourceType(DataSourceTypeEnum sourceType) {
        this.sourceType = sourceType;
    }
}