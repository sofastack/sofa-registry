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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertTrue;

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
import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.common.model.sessionserver.CancelAddressRequest;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataService;
import com.alipay.sofa.registry.server.meta.resource.ClientManagerResource;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.multi.cluster.DataCenterMetadataCache;
import com.alipay.sofa.registry.server.session.providedata.FetchClientOffAddressService;
import com.alipay.sofa.registry.server.session.push.PushSwitchService;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.alipay.sofa.registry.server.session.remoting.console.SessionConsoleExchanger;
import com.alipay.sofa.registry.server.session.resource.PersistenceClientManagerResource;
import com.alipay.sofa.registry.server.session.resource.SessionDigestResource;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.test.TestRegistryMain;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.h2.tools.Server;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

/** @author xuanbei 18/12/1 */
@SpringBootConfiguration
@SpringBootTest
public class BaseIntegrationTest extends AbstractTest {
  protected static final Logger LOGGER = LoggerFactory.getLogger(BaseIntegrationTest.class);
  protected static final AtomicBoolean STARTED = new AtomicBoolean(false);

  public static final String LOCAL_ADDRESS = NetUtil.getLocalAddress().getHostAddress();
  public static final String LOCAL_DATACENTER = "DefaultDataCenter";
  public static final String LOCAL_REGION = "DEFAULT_ZONE";
  private static final int CLIENT_OFF_MAX_WAIT_TIME = 30;
  protected static volatile ConfigurableApplicationContext metaApplicationContext;
  protected static volatile ConfigurableApplicationContext sessionApplicationContext;
  protected static volatile ConfigurableApplicationContext dataApplicationContext;

  protected static volatile DefaultRegistryClient registryClient1;

  protected static volatile DefaultRegistryClient registryClient2;

  protected static volatile Channel sessionChannel;

  protected static volatile Channel dataChannel;

  protected static volatile Channel metaChannel;

  protected static volatile SessionRegistry sessionRegistry;
  protected static volatile Interests sessionInterests;
  protected static volatile DataStore sessionDataStore;
  protected static volatile DatumStorageDelegate datumStorageDelegate;

  protected static volatile ClientManagerResource clientManagerResource;
  protected static volatile SessionDigestResource sessionDigestResource;
  protected static volatile com.alipay.sofa.registry.server.session.resource.ClientManagerResource
      sessionClientManagerResource;
  protected static volatile PersistenceClientManagerResource persistenceClientManagerResource;
  protected static volatile FetchClientOffAddressService fetchClientOffAddressService;
  protected static volatile SessionConsoleExchanger sessionConsoleExchanger;

  protected static volatile SessionServerConfigBean sessionServerConfig;

  protected static volatile PushSwitchService pushSwitchService;
  protected static volatile DataCenterMetadataCache dataCenterMetadataCache;

  protected static volatile DefaultProvideDataService provideDataService;

  protected static int sessionPort = 9603;
  protected static int consolePort = 9604;
  protected static int metaPort = 9615;
  protected static int dataPort = 9622;

  @Value("${session.server.serverPort}")
  protected int sessionServerPort;

  @Value("${data.server.syncDataPort}")
  protected int syncDataPort;

  protected Server h2Server = new Server();

  private Channel checkChannel(Channel channel) {
    if (channel == null) {
      String msg =
          StringFormatter.format(
              "channel is null, {}, {}",
              this.getClass().getSimpleName(),
              System.identityHashCode(this));
      throw new NullPointerException(msg);
    }
    return channel;
  }

  protected Channel getMetaChannel() {
    return checkChannel(metaChannel);
  }

  @BeforeClass
  public static void beforeBaseIntegrationClass() throws Exception {
    System.setProperty(
        LoggingSystem.SYSTEM_PROPERTY,
        "org.springframework.boot.logging.log4j2.Log4J2LoggingSystem");
    System.setProperty("spring.profiles.active", "integrate");
    System.setProperty(Lease.LEASE_DURATION, "2");
    System.setProperty(SlotConfig.KEY_DATA_SLOT_NUM, "16");
    beforeInit();
  }

  @Before
  public void beforeBaseIntegration() throws Exception {
    //        h2Server.start();
    //        Class.forName("org.h2.driver");
    LOGGER.info(
        "beforeBaseIntegrationCalled, {}, {}",
        this.getClass().getSimpleName(),
        System.identityHashCode(this));
    beforeInit();
  }

  protected static void beforeInit() throws Exception {
    startServerIfNecessary();
    initRegistryClientAndChannel();
    openPush();
  }

  public static void startServerIfNecessary() throws Exception {
    if (STARTED.compareAndSet(false, true)) {
      Map<String, String> configs = new HashMap<>();
      configs.put("nodes.metaNode", LOCAL_DATACENTER + ":" + LOCAL_ADDRESS);
      configs.put("nodes.localDataCenter", LOCAL_DATACENTER);
      configs.put("nodes.localRegion", LOCAL_REGION);
      configs.put("nodes.localSegmentRegions", LOCAL_REGION);

      TestRegistryMain testRegistryMain = new TestRegistryMain();
      testRegistryMain.startRegistryWithConfig(configs);
      metaApplicationContext = testRegistryMain.getMetaApplicationContext();
      sessionApplicationContext = testRegistryMain.getSessionApplicationContext();
      dataApplicationContext = testRegistryMain.getDataApplicationContext();
      initRegistryClientAndChannel();
      sessionRegistry = sessionApplicationContext.getBean("sessionRegistry", SessionRegistry.class);
      sessionInterests = sessionApplicationContext.getBean("sessionInterests", Interests.class);
      sessionDataStore = sessionApplicationContext.getBean("sessionDataStore", DataStore.class);

      clientManagerResource =
          metaApplicationContext.getBean("clientManagerResource", ClientManagerResource.class);
      provideDataService =
          metaApplicationContext.getBean("provideDataService", DefaultProvideDataService.class);

      sessionDigestResource =
          sessionApplicationContext.getBean("sessionDigestResource", SessionDigestResource.class);
      fetchClientOffAddressService =
          sessionApplicationContext.getBean(
              "fetchClientOffAddressService", FetchClientOffAddressService.class);
      sessionClientManagerResource =
          sessionApplicationContext.getBean(
              "clientManagerResource",
              com.alipay.sofa.registry.server.session.resource.ClientManagerResource.class);
      persistenceClientManagerResource =
          sessionApplicationContext.getBean(
              "persistenceClientManagerResource", PersistenceClientManagerResource.class);
      sessionConsoleExchanger =
          sessionApplicationContext.getBean(
              "sessionConsoleExchanger", SessionConsoleExchanger.class);
      pushSwitchService =
          sessionApplicationContext.getBean("pushSwitchService", PushSwitchService.class);
      dataCenterMetadataCache =
          sessionApplicationContext.getBean(
              "dataCenterMetadataCache", DataCenterMetadataCache.class);

      sessionServerConfig =
          (SessionServerConfigBean)
              sessionApplicationContext.getBean("sessionServerConfig", SessionServerConfig.class);
      sessionServerConfig.setScanSubscriberIntervalMillis(1000);
      datumStorageDelegate =
          dataApplicationContext.getBean("datumStorageDelegate", DatumStorageDelegate.class);
      LOGGER.info(
          "startServerNecessary, {} loaded by {}",
          BaseIntegrationTest.class,
          BaseIntegrationTest.class.getClassLoader(),
          new Exception("for trace"));
      Thread.sleep(1000 * 20);
    }
  }

  protected static void initRegistryClientAndChannel() throws Exception {
    if (registryClient1 == null) {
      RegistryClientConfig config =
          DefaultRegistryClientConfigBuilder.start()
              .setSyncConfigRetryInterval(60000)
              .setAppName("testApp1")
              .setDataCenter(LOCAL_DATACENTER)
              .setZone(LOCAL_REGION)
              .setRegistryEndpoint(LOCAL_ADDRESS)
              .setRegistryEndpointPort(sessionPort)
              .build();
      registryClient1 = new DefaultRegistryClient(config);
      registryClient1.init();
    }

    if (registryClient2 == null) {
      RegistryClientConfig config =
          DefaultRegistryClientConfigBuilder.start()
              .setAppName("testApp2")
              .setDataCenter(LOCAL_DATACENTER)
              .setRegistryEndpoint(LOCAL_ADDRESS)
              .setZone(LOCAL_REGION)
              .setRegistryEndpointPort(sessionPort)
              .build();
      registryClient2 = new DefaultRegistryClient(config);
      registryClient2.init();
    }

    if (sessionChannel == null) {
      sessionChannel = JerseyClient.getInstance().connect(new URL(LOCAL_ADDRESS, sessionPort));
      LOGGER.info(
          "init Channel for session: {}, {}, {}",
          sessionChannel,
          dataChannel,
          metaChannel,
          new Exception("for trace"));
    }
    if (dataChannel == null) {
      dataChannel = JerseyClient.getInstance().connect(new URL(LOCAL_ADDRESS, dataPort));
      LOGGER.info(
          "init Channel for data: {}, {}, {}",
          sessionChannel,
          dataChannel,
          metaChannel,
          new Exception("for trace"));
    }
    if (metaChannel == null) {
      metaChannel = JerseyClient.getInstance().connect(new URL(LOCAL_ADDRESS, metaPort));
      LOGGER.info(
          "init Channel for meta: {}, {}, {}",
          sessionChannel,
          dataChannel,
          metaChannel,
          new Exception("for trace"));
    }
    ParaCheckUtil.checkNotNull(sessionChannel.getWebTarget(), "sessionChannel");
    ParaCheckUtil.checkNotNull(dataChannel.getWebTarget(), "dataChannel");
    ParaCheckUtil.checkNotNull(metaChannel.getWebTarget(), "metaChannel");
  }

  protected static void openPush() throws InterruptedException, TimeoutException {
    Result result =
        metaChannel.getWebTarget().path("/stopPushDataSwitch/close").request().get(Result.class);
    Assert.assertTrue(result.isSuccess());
  }

  protected static void closePush() {
    Result result =
        metaChannel.getWebTarget().path("/stopPushDataSwitch/open").request().get(Result.class);
    Assert.assertTrue(result.isSuccess());
  }

  public static class MySubscriberDataObserver implements SubscriberDataObserver {
    public volatile String dataId;
    public volatile UserData userData;

    @Override
    public void handleData(String dataId, UserData data) {
      this.dataId = dataId;
      this.userData = data;
      LOGGER.info("handleData: {}, {}", dataId, data);
    }
  }

  public static class MyConfigDataObserver implements ConfigDataObserver {
    public volatile String dataId;
    public volatile ConfigData userData;

    @Override
    public void handleData(String dataId, ConfigData data) {
      this.dataId = dataId;
      this.userData = data;
      LOGGER.info("handleData: {}, {}", dataId, data);
    }
  }

  protected static void clientOff() throws Exception {
    startServerIfNecessary();
    List<ConnectId> connectIds = new ArrayList<>();
    connectIds.add(
        ConnectId.parse(
            LOCAL_ADDRESS
                + ":"
                + getSourcePort(registryClient1)
                + ValueConstants.CONNECT_ID_SPLIT
                + LOCAL_ADDRESS
                + ":9600"));
    connectIds.add(
        ConnectId.parse(
            LOCAL_ADDRESS
                + ":"
                + getSourcePort(registryClient2)
                + ValueConstants.CONNECT_ID_SPLIT
                + LOCAL_ADDRESS
                + ":9600"));
    CommonResponse response =
        sessionChannel
            .getWebTarget()
            .path("api/clients/off")
            .request()
            .post(
                Entity.entity(new CancelAddressRequest(connectIds), MediaType.APPLICATION_JSON),
                CommonResponse.class);
    LOGGER.info("client of {}", connectIds);
    assertTrue(response.isSuccess());
    int times = 0;
    while (times++ < CLIENT_OFF_MAX_WAIT_TIME) {
      if (clientOffSuccess()) {
        return;
      }
      Thread.sleep(500);
    }
    String sessionDigestCount =
        sessionChannel
            .getWebTarget()
            .path("digest/data/count")
            .request(APPLICATION_JSON)
            .get(String.class);
    String dataDigestCount =
        dataChannel
            .getWebTarget()
            .path("digest/datum/count")
            .request(APPLICATION_JSON)
            .get(String.class);

    throw new RuntimeException(
        "clientOff failed, session=" + sessionDigestCount + ", data=" + dataDigestCount);
  }

  private static boolean clientOffSuccess() {
    String sessionDigestCount =
        sessionChannel
            .getWebTarget()
            .path("digest/data/count")
            .request(APPLICATION_JSON)
            .get(String.class);
    String dataDigestCount =
        dataChannel
            .getWebTarget()
            .path("digest/datum/count")
            .request(APPLICATION_JSON)
            .get(String.class);
    return sessionDigestCount.contains("Publisher count: 0, Watcher count: 0")
        && (dataDigestCount.equals("CacheDigest datum cache is empty")
            || dataDigestCount.contains("[Publisher] size of publisher in DefaultDataCenter is 0"));
  }

  protected static int getSourcePort(DefaultRegistryClient registryClient) throws Exception {
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
        // do nothing
      }
    }
    return bos.toByteArray();
  }

  public static boolean isExist(Collection<Publisher> publishers, String localAddress) {
    for (Publisher publisher : publishers) {
      if (StringUtils.equals(publisher.getSourceAddress().getIpAddress(), localAddress)) {
        return true;
      }
    }
    return false;
  }

  protected static boolean isOpenPush() {
    return pushSwitchService.canLocalDataCenterPush();
  }

  protected static boolean isClosePush() {
    return !pushSwitchService.canLocalDataCenterPush();
  }
}
