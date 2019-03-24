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
package com.alipay.sofa.registry.server.data.cache;

import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;

/**
 *
 * @author qian.lqlq
 * @version $Id: UnPublisher.java, v 0.1 2018-01-11 20:05 qian.lqlq Exp $
 */
public class UnPublisher extends Publisher {

    /**
     *
     * @param dataInfoId
     * @param registerId
     * @param registerTimeStamp
     */
    public UnPublisher(String dataInfoId, String registerId, long registerTimeStamp) {
        setDataInfoId(dataInfoId);
        setRegisterId(registerId);
        setRegisterTimestamp(registerTimeStamp);
        //avoid new datum dataId is null
        DataInfo dataInfo = DataInfo.valueOf(dataInfoId);
        setDataId(dataInfo.getDataId());
        setGroup(dataInfo.getDataType());
        setInstanceId(dataInfo.getInstanceId());
    }

    @Override
    public DataType getDataType() {
        return DataType.UNPUBLISHER;
    }
}