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
package com.alipay.sofa.registry.test.pubsub;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;
import static org.junit.Assert.assertEquals;

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.model.UserData;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.server.session.node.service.DataNodeService;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.test.BaseIntegrationTest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xuanbei
 * @since 2019/1/15
 */
@RunWith(SpringRunner.class)
public class TempPublisherTest extends BaseIntegrationTest {
  static AtomicInteger count = new AtomicInteger();

  @Test
  public synchronized void doTest() throws Exception {
    String dataId = "test-dataId-" + this.getClass().getName() + System.currentTimeMillis();
    String value = "test publish temp data";
    AtomicReference<String> dataIdRef = new AtomicReference<>();
    AtomicReference<UserData> userDataRef = new AtomicReference<>();
    // register SubscriberRegistration
    SubscriberRegistration subReg =
        new SubscriberRegistration(
            dataId,
            (dataIdIn, data) -> {
              dataIdRef.set(dataIdIn);
              userDataRef.set(data);
              System.out.println(count.getAndIncrement() + "-@@@:" + dataIdIn + ":" + data);
            });

    subReg.setScopeEnum(ScopeEnum.zone);
    registryClient1.register(subReg);
    Thread.sleep(5000L);
    assertEquals(dataId, dataIdRef.get());
    assertEquals(0, userDataRef.get().getZoneData().size());

    // publish temp data
    Publisher tempPublisher = new Publisher();
    tempPublisher.setPublishType(PublishType.TEMPORARY);
    tempPublisher.setCell(LOCAL_REGION);
    tempPublisher.setDataId(dataId);
    tempPublisher.setGroup(DEFAULT_GROUP);
    tempPublisher.setInstanceId(DEFAULT_INSTANCE_ID);
    tempPublisher.setVersion(System.currentTimeMillis());
    tempPublisher.setRegisterTimestamp(System.currentTimeMillis());
    tempPublisher.setClientRegisterTimestamp(System.currentTimeMillis());
    tempPublisher.setRegisterId(UUID.randomUUID().toString());
    tempPublisher.setDataInfoId(DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP));
    tempPublisher.setSessionProcessId(ServerEnv.PROCESS_ID);
    List<ServerDataBox> dataBoxData = new ArrayList(1);
    dataBoxData.add(new ServerDataBox(object2bytes(value)));
    tempPublisher.setDataList(dataBoxData);
    sessionApplicationContext.getBean(DataNodeService.class).register(tempPublisher);

    // data size is 1
    Thread.sleep(5000L);
    assertEquals(dataId, dataIdRef.get());
    assertEquals(1, userDataRef.get().getZoneData().size());

    userDataRef.set(null);
    dataIdRef.set(null);
    registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);

    // register another SubscriberRegistration, data size is 0
    subReg =
        new SubscriberRegistration(
            dataId,
            (dataIdIn, data) -> {
              dataIdRef.set(dataIdIn);
              userDataRef.set(data);
            });
    subReg.setScopeEnum(ScopeEnum.zone);
    registryClient1.register(subReg);
    Thread.sleep(5000L);
    assertEquals(dataId, dataIdRef.get());
    assertEquals(0, userDataRef.get().getZoneData().size());
    registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
  }

  @Test
  public synchronized void doTestPubAndTempPubSameTime() throws Exception {
    String dataId = "test-same-time-pub&tempPub-" + System.currentTimeMillis();
    String value = "test same time publish";
    AtomicReference<String> dataIdRef = new AtomicReference<>();
    AtomicReference<UserData> userDataRef = new AtomicReference<>();

    SubscriberRegistration subReg =
        new SubscriberRegistration(
            dataId,
            (dataIdIn, data) -> {
              dataIdRef.set(dataIdIn);
              userDataRef.set(data);
            });
    subReg.setScopeEnum(ScopeEnum.zone);
    registryClient1.register(subReg);
    Thread.sleep(5000L);

    // publish data
    PublisherRegistration registration = new PublisherRegistration(dataId);
    registryClient1.register(registration, "test publish");

    Thread.sleep(5000L);

    // publish temp data
    Publisher tempPublisher = new Publisher();
    tempPublisher.setPublishType(PublishType.TEMPORARY);
    tempPublisher.setCell(LOCAL_REGION);
    tempPublisher.setDataId(dataId);
    tempPublisher.setGroup(DEFAULT_GROUP);
    tempPublisher.setInstanceId(DEFAULT_INSTANCE_ID);
    tempPublisher.setVersion(System.currentTimeMillis());
    tempPublisher.setRegisterTimestamp(System.currentTimeMillis());
    tempPublisher.setClientRegisterTimestamp(System.currentTimeMillis());
    tempPublisher.setRegisterId(UUID.randomUUID().toString());
    tempPublisher.setDataInfoId(DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP));
    List<ServerDataBox> dataBoxData = new ArrayList(1);
    dataBoxData.add(new ServerDataBox(object2bytes(value)));
    tempPublisher.setDataList(dataBoxData);
    tempPublisher.setSessionProcessId(ServerEnv.PROCESS_ID);
    sessionApplicationContext.getBean(DataNodeService.class).register(tempPublisher);

    Thread.sleep(5000L);

    assertEquals(1, userDataRef.get().getZoneData().size());
    assertEquals(2, userDataRef.get().getZoneData().get(LOCAL_REGION).size());

    registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
    registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
  }
}
