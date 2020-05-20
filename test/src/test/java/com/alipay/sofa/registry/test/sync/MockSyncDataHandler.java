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

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;
import static com.alipay.sofa.registry.test.BaseIntegrationTest.LOCAL_ADDRESS;
import static com.alipay.sofa.registry.test.BaseIntegrationTest.LOCAL_DATACENTER;
import static com.alipay.sofa.registry.test.BaseIntegrationTest.LOCAL_REGION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.SyncData;
import com.alipay.sofa.registry.common.model.dataserver.SyncDataRequest;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.ParaCheckUtil;

/**
 * mock SyncDataHandler, it will return fixed Datum.
 *
 * @author xuanbei
 * @since 2019/1/16
 */
public class MockSyncDataHandler extends AbstractServerHandler<SyncDataRequest> {
    public static String dataId  = "test-dataId-" + System.currentTimeMillis();
    public static String value   = "MockSyncDataHandler";
    public static long   version = DatumVersionUtil.nextId();

    @Override
    public void checkParam(SyncDataRequest request) throws RuntimeException {
        ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "request.dataInfoId");
        ParaCheckUtil.checkNotBlank(request.getDataCenter(), "request.dataCenter");
        ParaCheckUtil.checkNotBlank(String.valueOf(request.getVersion()), "request.currentVersion");
    }

    @Override
    public Object doHandle(Channel channel, SyncDataRequest request) {
        String dataInfoId = DataInfo.toDataInfoId(MockSyncDataHandler.dataId, DEFAULT_INSTANCE_ID,
            DEFAULT_GROUP);
        Datum datum = new Datum();
        datum.setDataInfoId(dataInfoId);
        datum.setDataId(dataId);
        datum.setDataCenter(LOCAL_DATACENTER);
        datum.setGroup(DEFAULT_GROUP);
        datum.setInstanceId(DEFAULT_INSTANCE_ID);
        datum.setVersion(version);
        String registerId = UUID.randomUUID().toString();
        Publisher publisher = createPublisher();
        publisher.setRegisterId(registerId);
        Map<String, Publisher> pubMap = new ConcurrentHashMap<>();
        pubMap.put(registerId, publisher);
        datum.setPubMap(pubMap);

        SyncData syncData = new SyncData(dataInfoId, LOCAL_DATACENTER, true, new ArrayList<>(
            Arrays.asList(datum)));
        return new GenericResponse<SyncData>().fillSucceed(syncData);
    }

    private Publisher createPublisher() {
        Publisher publisher = new Publisher();
        publisher.setDataList(new ArrayList<>(Arrays.asList(new ServerDataBox(ServerDataBox
            .getBytes(value)))));
        publisher.setCell(LOCAL_REGION);
        publisher.setAppName("testApp");
        publisher.setClientRegisterTimestamp(System.currentTimeMillis() - 100000L);
        publisher.setRegisterTimestamp(System.currentTimeMillis() - 80000L);
        publisher.setClientVersion(BaseInfo.ClientVersion.StoreData);
        publisher.setDataId(dataId);
        publisher.setGroup(DEFAULT_GROUP);
        publisher.setInstanceId(DEFAULT_INSTANCE_ID);
        publisher.setVersion(version);
        publisher.setSourceAddress(new URL(LOCAL_ADDRESS, 65343));
        publisher.setTargetAddress(new URL(LOCAL_ADDRESS, 9600));
        publisher.setDataInfoId(DataInfo.toDataInfoId(MockSyncDataHandler.dataId,
            DEFAULT_INSTANCE_ID, DEFAULT_GROUP));
        return publisher;
    }

    @Override
    public GenericResponse<SyncData> buildFailedResponse(String msg) {
        return new GenericResponse<SyncData>().fillFailed(msg);
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return SyncDataRequest.class;
    }

    @Override
    protected Node.NodeType getConnectNodeType() {
        return Node.NodeType.DATA;
    }
}
