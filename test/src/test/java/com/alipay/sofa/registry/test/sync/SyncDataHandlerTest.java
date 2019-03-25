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
package com.alipay.sofa.registry.test.sync;

import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.SyncData;
import com.alipay.sofa.registry.common.model.dataserver.SyncDataRequest;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author xuanbei
 * @since 2019/1/16
 */
@RunWith(SpringRunner.class)
public class SyncDataHandlerTest extends BaseIntegrationTest {
    @Test
    public void doTest() throws Exception {
        // publish data
        String dataId = "test-dataId-" + System.currentTimeMillis();
        String value = "test publish";
        PublisherRegistration registration = new PublisherRegistration(dataId);
        registryClient1.register(registration, value);
        Thread.sleep(500L);

        // request syncData
        DataNodeExchanger dataNodeExchanger = dataApplicationContext.getBean("dataNodeExchanger",
            DataNodeExchanger.class);
        GenericResponse genericResponse = (GenericResponse) dataNodeExchanger.request(
            new Request() {
                @Override
                public Object getRequestBody() {
                    return new SyncDataRequest(DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID,
                        DEFAULT_GROUP), LOCAL_DATACENTER, System.currentTimeMillis() - 100000,
                        DataSourceTypeEnum.BACKUP.toString());
                }

                @Override
                public URL getRequestUrl() {
                    return new URL(LOCAL_ADDRESS, syncDataPort);
                }
            }).getResult();

        // assert result
        assertTrue(genericResponse.isSuccess());
        SyncData syncData = (SyncData) genericResponse.getData();
        assertEquals(DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP),
            syncData.getDataInfoId());
        assertEquals(LOCAL_DATACENTER, syncData.getDataCenter());
        List<Datum> datums = (List<Datum>) syncData.getDatums();
        Datum datum = datums.get(0);
        assertEquals(DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP),
            datum.getDataInfoId());
        assertEquals(LOCAL_DATACENTER, datum.getDataCenter());
        assertEquals(dataId, datum.getDataId());
        assertEquals(1, datum.getPubMap().size());
        assertEquals(LOCAL_REGION, datum.getPubMap().values().iterator().next().getCell());
        assertEquals(1, datum.getPubMap().values().iterator().next().getDataList().size());
        assertEquals(value, bytes2Object(datum.getPubMap().values().iterator().next().getDataList()
            .get(0).getBytes()));

        // clear data
        DatumCache.getAll().clear();
    }
}
