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

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_DATA_CENTER;
import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.GetDataRequest;
import com.alipay.sofa.registry.common.model.sessionserver.DataChangeRequest;
import com.alipay.sofa.registry.common.model.store.BaseInfo.ClientVersion;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.bolt.BoltClient;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.remoting.dataserver.DataServerConnectionFactory;
import com.alipay.sofa.registry.server.data.remoting.dataserver.handler.DataSyncServerConnectionHandler;
import com.alipay.sofa.registry.server.data.remoting.handler.AbstractServerHandler;
import com.alipay.sofa.registry.server.session.cache.DatumKey;
import com.alipay.sofa.registry.server.session.cache.Key;
import com.alipay.sofa.registry.server.session.cache.Key.KeyType;
import com.alipay.sofa.registry.server.session.cache.SessionCacheService;
import com.alipay.sofa.registry.server.session.store.SessionInterests;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import com.alipay.sofa.registry.util.ParaCheckUtil;

/**
 * @author xuanbei
 * @since 2019/1/16
 */
@RunWith(SpringRunner.class)
public class SessionNotifyTest extends BaseIntegrationTest {
    public static String                       DATA_ID           = "test-dataId-"
                                                                   + System.currentTimeMillis();

    private static final int                   TEST_SYNC_PORT    = 9677;
    private static DataServerConnectionFactory dataServerConnectionFactory;
    private static Server                      dataSyncServer;
    private static String                      remoteIP;
    private static BoltChannel                 boltChannel;

    private static Map<String, BoltChannel>    boltChannelMap    = new ConcurrentHashMap<>();

    private static Map<Integer, BoltChannel>   boltChannelMapInt = new ConcurrentHashMap<>();

    private static final int                   connectNum        = 20;

    private static final int                   threadNum         = 20;

    private static final int                   idNum             = 100;

    private static SessionCacheService         sessionCacheService;

    private static SessionInterests            sessionInterests;

    private static Datum                       datum;

    private static BoltClient                  boltClientFetch   = new BoltClient(1);

    private static ThreadPoolExecutor          executor          = new ThreadPoolExecutor(100, 100,
                                                                     0L, TimeUnit.MILLISECONDS,
                                                                     new LinkedBlockingQueue<>(),
                                                                     new NamedThreadFactory(
                                                                         "TestSessionNotifyFetch"));

    @BeforeClass
    public static void beforeClass() throws Exception {
        startServerIfNecessary();
        BoltExchange boltExchange = (BoltExchange) dataApplicationContext.getBean("boltExchange");
        dataServerConnectionFactory = dataApplicationContext.getBean("dataServerConnectionFactory",
            DataServerConnectionFactory.class);

        sessionCacheService = sessionApplicationContext.getBean("sessionCacheService",
            SessionCacheService.class);

        sessionInterests = sessionApplicationContext.getBean("sessionInterests",
            SessionInterests.class);

        Map<String, Publisher> publisherMap = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            Publisher publisher = new Publisher();

            String connectId = "123.123.123." + i;
            publisher.setAppName("APP");
            //ZONE MUST BE CURRENT SESSION ZONE
            publisher.setCell("CELL");
            publisher.setClientId(connectId);
            publisher.setDataId(DATA_ID);
            publisher.setGroup(DEFAULT_GROUP);
            publisher.setInstanceId(DEFAULT_INSTANCE_ID);
            publisher.setRegisterId(UUID.randomUUID().toString());
            publisher.setProcessId(connectId);
            publisher.setVersion(Long.valueOf(i));

            //registerTimestamp must happen from server,client time maybe different cause pub and unPublisher fail
            publisher.setRegisterTimestamp(System.currentTimeMillis());

            publisher.setClientRegisterTimestamp(System.currentTimeMillis());
            publisher.setSourceAddress(new URL(connectId, 5050));

            publisher.setClientVersion(ClientVersion.StoreData);

            DataInfo dataInfo = new DataInfo(DEFAULT_INSTANCE_ID, DATA_ID, DEFAULT_GROUP);
            publisher.setDataInfoId(dataInfo.getDataInfoId());

            ServerDataBox serverDataBox = new ServerDataBox("");
            List<ServerDataBox> list = new ArrayList<>();
            list.add(serverDataBox);

            publisher.setDataList(list);
            publisherMap.put(publisher.getRegisterId(), publisher);
        }

        datum = new Datum();
        datum.setDataId(DATA_ID);
        datum.setInstanceId(DEFAULT_INSTANCE_ID);
        datum.setGroup(DEFAULT_GROUP);
        //no datum set version current timestamp
        datum.setVersion(DatumVersionUtil.nextId());
        datum.setPubMap(publisherMap);
        datum.setDataCenter(ValueConstants.DEFAULT_DATA_CENTER);

        // open sync port and connect it
        dataSyncServer = boltExchange.open(new URL(NetUtil.getLocalAddress().getHostAddress(),
            TEST_SYNC_PORT), new ChannelHandler[] { new MockGetDataHandler(),
                dataApplicationContext.getBean(DataSyncServerConnectionHandler.class) });

        for (int i = 0; i < connectNum; i++) {

            URL urltemp = new URL(LOCAL_ADDRESS, TEST_SYNC_PORT);

            Client client = boltExchange.connect(Exchange.DATA_SERVER_TYPE + i, urltemp,
                new ChannelHandler[] { new MockDataChangeRequestHandler() });

            BoltChannel boltChannel = (BoltChannel) client.getChannel(urltemp);

            String key = boltChannel.getConnection().getLocalIP() + ":"
                         + boltChannel.getConnection().getLocalPort();
            boltChannelMap.put(key, boltChannel);
            System.out.println("testsyncserver remote connect remote:"
                               + boltChannel.getConnection().getRemoteAddress() + " local:" + key);
        }

        for (int i = 0; i < connectNum; i++) {

            URL urltemp = new URL(LOCAL_ADDRESS, TEST_SYNC_PORT);

            Client client = boltExchange.connect(Exchange.DATA_SERVER_TYPE + i + 10, urltemp,
                new ChannelHandler[] { new MockDataChangeRequestHandler() });

            BoltChannel boltChannel = (BoltChannel) client.getChannel(urltemp);

            String key = boltChannel.getConnection().getLocalIP() + ":"
                         + boltChannel.getConnection().getLocalPort();
            boltChannelMapInt.put(i, boltChannel);
            System.out.println("testsyncserver remote connect remote:"
                               + boltChannel.getConnection().getRemoteAddress() + " local:" + key);
        }

        Thread.sleep(500);
    }

    @Ignore
    @Test
    public void doTest() throws Exception {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(threadNum, threadNum, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new NamedThreadFactory("TestSessionNotify"));
        for (int i = 0; i < idNum; i++) {

            int finalI = i;
            executor.submit(()->{

                // post sync data request
                DataChangeRequest request = new DataChangeRequest(DataInfo.toDataInfoId(
                        DATA_ID, DEFAULT_INSTANCE_ID, DEFAULT_GROUP), LOCAL_DATACENTER,
                        finalI);

                boltChannelMap.forEach((connect,boltChannel)->{

                    CommonResponse commonResponse = null;
                    try {
                        dataSyncServer.sendCallback(dataSyncServer
                                        .getChannel(new URL(boltChannel.getConnection().getLocalIP(), boltChannel.getConnection().getLocalPort())), request,
                                new NotifyCallback(boltChannel.getConnection(),request),3000);
                        //assertTrue(commonResponse.isSuccess());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        }

        while (true){
            TimeUnit.SECONDS.sleep(10);
        }

    }

    public static class MockDataChangeRequestHandler extends
                                                    AbstractServerHandler<DataChangeRequest> {

        @Override
        public void checkParam(DataChangeRequest request) throws RuntimeException {

        }

        @Override
        public Object doHandle(Channel channel, DataChangeRequest request) {

            DataChangeRequest dataChangeRequest = (DataChangeRequest) request;

            dataChangeRequest.setDataCenter(dataChangeRequest.getDataCenter());
            dataChangeRequest.setDataInfoId(dataChangeRequest.getDataInfoId());
            long version = dataChangeRequest.getVersion();


            //update cache when change
            sessionCacheService.invalidate(new Key(KeyType.OBJ, DatumKey.class.getName(), new DatumKey(
                    dataChangeRequest.getDataInfoId(), dataChangeRequest.getDataCenter())));


            try {
                boolean resultT = sessionInterests.checkInterestVersions(
                        dataChangeRequest.getDataCenter(), dataChangeRequest.getDataInfoId(),
                        dataChangeRequest.getVersion());
                //if (!resultT) {
                //    return null;
                //}

                System.out.println(String.format(
                        "Data version has change,and will fetch to update!Request=%s,URL=%s",
                        dataChangeRequest, channel.getRemoteAddress()));
                //TimeUnit.MILLISECONDS.sleep(2000);

                //fireChangFetch(dataChangeRequest);
                //if(version%3==0) {
                    executor.submit(() -> {

                        Object result = boltClientFetch.sendSync(boltChannelMapInt.get(Math.abs((DATA_ID+version).hashCode() % connectNum)),
                                new GetDataRequest(DATA_ID, DEFAULT_DATA_CENTER), 1000);
                        GenericResponse genericResponse = (GenericResponse) result;
                        if (genericResponse.isSuccess()) {
                            Map<String, Datum> map = (Map<String, Datum>) genericResponse.getData();
                            if (map == null || map.isEmpty()) {
                                System.out.println(String.format("GetDataRequest get response contains no datum!dataInfoId=%s",
                                        DATA_ID));
                            } else {
                                System.out.println(String.format("GetDataRequest get response contains datum!dataInfoId=%s,size=%s",
                                        DATA_ID, map.get(DEFAULT_DATA_CENTER).getPubMap().size()));
                            }
                        } else {
                            throw new RuntimeException(
                                    String.format("GetDataRequest has got fail response!dataInfoId:%s msg:%s", DATA_ID,
                                            genericResponse.getMessage()));
                        }
                    });
                //}

            } catch (Exception e) {
                //LOGGER.error("DataChange Request error!", e);
                throw new RuntimeException("DataChangeRequest Request error!", e);
            }
            //System.out.println(String.format("There are not Subscriber Existed! Who are interest with dataInfoId %s !",
            //        request.getDataInfoId()));
            return null;
        }

        @Override
        public GenericResponse buildFailedResponse(String msg) {
            return new GenericResponse().fillFailed(msg);
        }

        @Override
        public HandlerType getType() {
            return HandlerType.PROCESSER;
        }

        @Override
        public Class interest() {
            return DataChangeRequest.class;
        }
    }

    private static class MockGetDataHandler extends AbstractServerHandler<GetDataRequest> {
        public void checkParam(GetDataRequest request) throws RuntimeException {
            ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "GetDataRequest.dataInfoId");
        }

        @Override
        public Object doHandle(Channel channel, GetDataRequest request) {
            Map<String, Datum> map = new HashMap<>();
            map.put(DEFAULT_DATA_CENTER, datum);

            return new GenericResponse<Map<String, Datum>>().fillSucceed(map);
        }

        @Override
        public GenericResponse<Map<String, Datum>> buildFailedResponse(String msg) {
            return new GenericResponse<Map<String, Datum>>().fillFailed(msg);
        }

        @Override
        public HandlerType getType() {
            return HandlerType.PROCESSER;
        }

        @Override
        public Class interest() {
            return GetDataRequest.class;
        }

        @Override
        protected Node.NodeType getConnectNodeType() {
            return Node.NodeType.DATA;
        }
    }

    private class NotifyCallback implements CallbackHandler {

        private int               retryTimes = 0;
        private Connection        connection;
        private DataChangeRequest request;

        public NotifyCallback(Connection connection, DataChangeRequest request) {
            this.connection = connection;
            this.request = request;
        }

        @Override
        public void onCallback(Channel channel, Object message) {
            CommonResponse result = (CommonResponse) message;
            if (result != null && !result.isSuccess()) {
                System.out
                    .println(String
                        .format(
                            "response not success when notify sessionServer(%s), retryTimes=%s, request=%s, response=%s",
                            connection.getRemoteAddress(), retryTimes, request, result));
                //onFailed(this);
            }
        }

        @Override
        public void onException(Channel channel, Throwable e) {
            System.err.println(String.format(
                "exception when notify sessionServer(%s), retryTimes=%s, request=%s,error=%s",
                connection.getRemoteAddress(), retryTimes, request, e));
            e.printStackTrace();
            //onFailed(this);
        }

        @Override
        public Executor getExecutor() {
            return null;
        }

    }
}
