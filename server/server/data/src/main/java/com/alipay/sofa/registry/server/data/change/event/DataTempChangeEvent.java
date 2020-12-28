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
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-08 19:54 yuzhi.lyz Exp $
 */
public final class DataTempChangeEvent implements IDataChangeEvent {
    private final Datum datum;

    public DataTempChangeEvent(Datum datum) {
        this.datum = datum;
    }

    /**
     * Getter method for property <tt>datum</tt>.
     * @return property value of datum
     */
    public Datum getDatum() {
        return datum;
    }

    @Override
    public DataSourceTypeEnum getSourceType() {
        return DataSourceTypeEnum.PUB_TEMP;
    }

    @Override
    public String toString() {
        return "DataTempChangeEvent{" +
                "dataCenter='" + datum.getDataCenter() + '\'' +
                ", dataInfoId='" + datum.getDataInfoId() + '\'' +
                ", publisherNum='" + datum.publisherSize() + '\'' +
                '}';
    }
}
