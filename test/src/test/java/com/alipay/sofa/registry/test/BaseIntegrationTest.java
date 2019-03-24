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
package com.alipay.sofa.registry.test;

import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.client.api.ConfigDataObserver;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.SubscriberDataObserver;
import com.alipay.sofa.registry.client.api.model.ConfigData;
import com.alipay.sofa.registry.client.api.model.UserData;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClient;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClientConfigBuilder;
import com.alipay.sofa.registry.client.remoting.Client;
import com.alipay.sofa.registry.client.remoting.ClientConnection;
import com.alipay.sofa.registry.client.task.AbstractWorkerThread;
import com.alipay.sofa.registry.client.task.WorkerThread;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.sessionserver.CancelAddressRequest;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.test.TestRegistryMain;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

/**
 * @author xuanbei 18/12/1
 */
@SpringBootConfiguration
public class BaseIntegrationTest {
    private static final AtomicBoolean              STARTED          = new AtomicBoolean(false);

    public static final String                      LOCAL_ADDRESS    = NetUtil.getLocalAddress()
                                                                         .getHostAddress();
    public static final String                      LOCAL_DATACENTER = "DefaultDataCenter";
    public static final String                      LOCAL_REGION     = "DEFAULT_ZONE";
    protected static ConfigurableApplicationContext metaApplicationContext;
    protected static ConfigurableApplicationContext sessionApplicationContext;
    protected static ConfigurableApplicationContext dataApplicationContext;

    protected DefaultRegistryClient                 registryClient1;

    protected DefaultRegistryClient                 registryClient2;

    protected Channel                               sessionChannel;

    protected Channel                               dataChannel;

    protected Channel                               metaChannel;

    protected volatile String                       dataId;

    protected volatile UserData                     userData;

    protected volatile ConfigData                   configData;

    @Value("${session.server.httpServerPort}")
    protected int                                   sessionPort;

    @Value("${meta.server.httpServerPort}")
    protected int                                   metaPort;

    @Value("${data.server.httpServerPort}")
    protected int                                   dataPort;

    @Value("${meta.server.raftServerPort}")
    protected int                                   raftPort;

    @Value("${session.server.serverPort}")
    protected int                                   sessionServerPort;

    @Value("${data.server.syncDataPort}")
    protected int                                   syncDataPort;

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (STARTED.compareAndSet(false, true)) {
            Map<String, String> configs = new HashMap<>();
            configs.put("nodes.metaNode", LOCAL_DATACENTER + ":" + LOCAL_ADDRESS);
            configs.put("nodes.localDataCenter", LOCAL_DATACENTER);
            configs.put("nodes.localRegion", LOCAL_REGION);

            TestRegistryMain testRegistryMain = new TestRegistryMain();
            testRegistryMain.startRegistryWithConfig(configs);
            metaApplicationContext = testRegistryMain.getMetaApplicationContext();
            sessionApplicationContext = testRegistryMain.getSessionApplicationContext();
            dataApplicationContext = testRegistryMain.getDataApplicationContext();
        }
    }

    @Before
    public void before() throws Exception {
        if (registryClient1 == null) {
            RegistryClientConfig config = DefaultRegistryClientConfigBuilder.start()
                .setAppName("testApp1").setDataCenter(LOCAL_DATACENTER).setZone(LOCAL_REGION)
                .setRegistryEndpoint(LOCAL_ADDRESS).setRegistryEndpointPort(sessionPort).build();
            registryClient1 = new DefaultRegistryClient(config);
            registryClient1.init();
        }

        if (registryClient2 == null) {
            RegistryClientConfig config = DefaultRegistryClientConfigBuilder.start()
                .setAppName("testApp2").setDataCenter(LOCAL_DATACENTER)
                .setRegistryEndpoint(LOCAL_ADDRESS).setZone(LOCAL_REGION)
                .setRegistryEndpointPort(sessionPort).build();
            registryClient2 = new DefaultRegistryClient(config);
            registryClient2.init();
        }

        if (sessionChannel == null || dataChannel == null || metaChannel == null) {
            sessionChannel = JerseyClient.getInstance()
                .connect(new URL(LOCAL_ADDRESS, sessionPort));
            dataChannel = JerseyClient.getInstance().connect(new URL(LOCAL_ADDRESS, dataPort));
            metaChannel = JerseyClient.getInstance().connect(new URL(LOCAL_ADDRESS, metaPort));
        }

        dataId = null;
        userData = null;
    }

    @After
    public void after() throws Exception {
        dataId = null;
        userData = null;
        configData = null;
    }

    public class MySubscriberDataObserver implements SubscriberDataObserver {
        @Override
        public void handleData(String dataId, UserData data) {
            BaseIntegrationTest.this.dataId = dataId;
            BaseIntegrationTest.this.userData = data;
        }
    }

    public class MyConfigDataObserver implements ConfigDataObserver {
        @Override
        public void handleData(String dataId, ConfigData configData) {
            BaseIntegrationTest.this.dataId = dataId;
            BaseIntegrationTest.this.configData = configData;
        }
    }

    protected void clientOff() throws Exception {
        List<String> connectIds = new ArrayList<>();
        connectIds.add(LOCAL_ADDRESS + ":" + getSourcePort(registryClient1));
        connectIds.add(LOCAL_ADDRESS + ":" + getSourcePort(registryClient2));
        CommonResponse response = sessionChannel
            .getWebTarget()
            .path("api/clients/off")
            .request()
            .post(Entity.entity(new CancelAddressRequest(connectIds), MediaType.APPLICATION_JSON),
                CommonResponse.class);
        assertTrue(response.isSuccess());
        Thread.sleep(1000);
    }

    protected int getSourcePort(DefaultRegistryClient registryClient) throws Exception {
        Field workerThreadField = DefaultRegistryClient.class.getDeclaredField("workerThread");
        workerThreadField.setAccessible(true);
        WorkerThread workerThread = (WorkerThread) workerThreadField.get(registryClient);

        Field clientField = AbstractWorkerThread.class.getDeclaredField("client");
        clientField.setAccessible(true);
        Client client = (Client) clientField.get(workerThread);
        client.ensureConnected();

        Field clientConnectionField = ClientConnection.class.getDeclaredField("clientConnection");
        clientConnectionField.setAccessible(true);
        Connection clientConnection = (Connection) clientConnectionField.get(client);
        return clientConnection.getLocalPort();
    }

    protected Object bytes2Object(byte[] bytes) throws IOException, ClassNotFoundException {
        Object object = null;
        if (bytes != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream input = null;
            try {
                input = new ObjectInputStream(bis);
                object = input.readObject();
            } finally {
                if (input != null) {
                    input.close();
                }
            }
        }
        return object;
    }

    protected byte[] object2bytes(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream javaos = null;
        try {
            javaos = new ObjectOutputStream(bos);
            javaos.writeObject(object);
        } finally {
            try {
                javaos.close();
            } catch (IOException ioe) {
                //do nothing
            }
        }
        return bos.toByteArray();
    }
}