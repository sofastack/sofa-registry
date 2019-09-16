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
package com.alipay.sofa.registry.server.data.remoting.dataserver;

import com.alipay.remoting.Connection;
import com.alipay.remoting.InvokeCallback;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.SyncData;
import com.alipay.sofa.registry.common.model.dataserver.SyncDataRequest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.change.DataChangeTypeEnum;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;
import com.alipay.sofa.registry.server.data.change.event.DataChangeEventCenter;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 *
 * @author qian.lqlq
 * @version $Id: SyncDataCallback.java, v 0.1 2018-03-08 14:22 qian.lqlq Exp $
 */
public class SyncDataCallback implements InvokeCallback {

    private static final Logger   LOGGER      = LoggerFactory.getLogger(SyncDataCallback.class);

    private static final Executor EXECUTOR    = ExecutorFactory.newFixedThreadPool(20,
                                                  SyncDataCallback.class.getSimpleName());

    private static final int      RETRY_COUNT = 3;

    private Connection            connection;

    private SyncDataRequest       request;

    private GetSyncDataHandler    getSyncDataHandler;

    private int                   retryCount;

    private DataChangeEventCenter dataChangeEventCenter;

    /**
     * constructor
     * @param connection
     * @param request
     */
    public SyncDataCallback(GetSyncDataHandler getSyncDataHandler, Connection connection,
                            SyncDataRequest request, DataChangeEventCenter dataChangeEventCenter) {
        this.getSyncDataHandler = getSyncDataHandler;
        this.connection = connection;
        this.request = request;
        this.retryCount = RETRY_COUNT;
        this.dataChangeEventCenter = dataChangeEventCenter;
    }

    @Override
    public void onResponse(Object obj) {
        GenericResponse<SyncData> response = (GenericResponse) obj;
        if (!response.isSuccess()) {
            getSyncDataHandler.syncData(this);
        } else {
            SyncData syncData = response.getData();
            Collection<Datum> datums = syncData.getDatums();
            DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.valueOf(request
                .getDataSourceType());
            LOGGER
                .info(
                    "[SyncDataCallback] get syncDatas,datums size={},wholeTag={},dataCenter={},dataInfoId={}",
                    datums.size(), syncData.getWholeDataTag(), syncData.getDataCenter(),
                    syncData.getDataInfoId());
            if (syncData.getWholeDataTag()) {
                //handle all data, replace cache with these datum directly
                for (Datum datum : datums) {
                    if (datum == null) {
                        datum = new Datum();
                        datum.setDataInfoId(syncData.getDataInfoId());
                        datum.setDataCenter(syncData.getDataCenter());
                    }
                    processDatum(datum);
                    dataChangeEventCenter.sync(DataChangeTypeEnum.COVER, dataSourceTypeEnum, datum);
                    break;
                }
            } else {
                //handle incremental data one by one
                if (!CollectionUtils.isEmpty(datums)) {
                    for (Datum datum : datums) {
                        if (datum != null) {
                            processDatum(datum);
                            dataChangeEventCenter.sync(DataChangeTypeEnum.MERGE,
                                dataSourceTypeEnum, datum);
                        }
                    }
                } else {
                    LOGGER.info("[SyncDataCallback] get no syncDatas");
                }
            }
        }
    }

    private void processDatum(Datum datum) {
        if (datum != null) {
            Map<String, Publisher> publisherMap = datum.getPubMap();

            if (publisherMap != null && !publisherMap.isEmpty()) {
                publisherMap.forEach((registerId, publisher) -> Publisher.processPublisher(publisher));
            }
        }
    }

    @Override
    public void onException(Throwable e) {
        GenericResponse genericResponse = new GenericResponse();
        genericResponse.fillFailed(e.getMessage());
        onResponse(genericResponse);
    }

    @Override
    public Executor getExecutor() {
        return EXECUTOR;
    }

    /**
     * Getter method for property <tt>connection</tt>.
     *
     * @return property value of connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Getter method for property <tt>request</tt>.
     *
     * @return property value of request
     */
    public SyncDataRequest getRequest() {
        return request;
    }

    /**
     * Getter method for property <tt>retryCount</tt>.
     *
     * @return property value of retryCount
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Setter method for property <tt>retryCount</tt>.
     *
     * @param retryCount  value to be assigned to property retryCount
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}