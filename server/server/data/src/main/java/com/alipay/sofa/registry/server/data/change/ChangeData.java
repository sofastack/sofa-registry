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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import com.alipay.sofa.registry.common.model.dataserver.Datum;

/**
 * changed data
 *
 * @author qian.lqlq
 * @version $Id: ChangeData.java, v 0.1 2017-12-08 16:23 qian.lqlq Exp $
 */
public class ChangeData implements Delayed {

    /** data changed */
    private Datum              datum;

    /** change time */
    private Long               gmtCreate;

    /** timeout */
    private long               timeout;

    private DataSourceTypeEnum sourceType;

    private DataChangeTypeEnum changeType;

    /**
     * constructor
     * @param datum
     * @param timeout
     * @param sourceType
     * @param changeType
     */
    public ChangeData(Datum datum, long timeout, DataSourceTypeEnum sourceType,
                      DataChangeTypeEnum changeType) {
        this.datum = datum;
        this.gmtCreate = System.currentTimeMillis();
        this.timeout = timeout;
        this.sourceType = sourceType;
        this.changeType = changeType;
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
     * Getter method for property <tt>changeType</tt>.
     *
     * @return property value of changeType
     */
    public DataChangeTypeEnum getChangeType() {
        return changeType;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit
            .convert(gmtCreate + timeout - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o == this) {
            return 0;
        }
        if (o instanceof ChangeData) {
            ChangeData other = (ChangeData) o;
            if (this.gmtCreate < other.gmtCreate) {
                return -1;
            } else if (this.gmtCreate > other.gmtCreate) {
                return 1;
            } else {
                return 0;
            }
        }
        return -1;
    }
}