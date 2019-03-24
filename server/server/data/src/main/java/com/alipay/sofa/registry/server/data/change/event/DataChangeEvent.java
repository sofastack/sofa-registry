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

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.server.data.change.DataChangeTypeEnum;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;

/**
 * event for data changed
 *
 * @author qian.lqlq
 * @version $Id: DataChangeEvent.java, v 0.1 2017-12-07 18:44 qian.lqlq Exp $
 */
public class DataChangeEvent implements IDataChangeEvent {

    /**
     * type of changed data, MERGE or COVER
     */
    private DataChangeTypeEnum changeType;

    /**
     *
     */
    private DataSourceTypeEnum sourceType;

    /**
     * data changed
     */
    private Datum              datum;

    /**
     * constructor
     * @param changeType
     * @param sourceType
     * @param datum
     */
    public DataChangeEvent(DataChangeTypeEnum changeType, DataSourceTypeEnum sourceType, Datum datum) {
        this.changeType = changeType;
        this.sourceType = sourceType;
        this.datum = datum;
    }

    /**
     * Getter method for property <tt>changeType</tt>.
     *
     * @return property value of changeType
     */
    public DataChangeTypeEnum getChangeType() {
        return changeType;
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

    @Override
    public DataChangeScopeEnum getScope() {
        return DataChangeScopeEnum.DATUM;
    }

}