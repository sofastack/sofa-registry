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
package com.alipay.sofa.registry.server.session.providedata;

import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.FetchSystemPropertyResult;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.session.acceptor.WriteDataAcceptor;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.providedata.FetchDataInfoIDBlackListService.DataInfoIDBlacklistStorage;
import com.alipay.sofa.registry.server.session.slot.SlotTableCache;
import com.alipay.sofa.registry.server.session.store.SessionDataStore;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.util.JsonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author huicha
 * @date 2024/12/13
 */
public class FetchDataInfoIDBlackListServiceTest {

  private final String dataInfoIdOne;

  private final String dataInfoIdTwo;

  private final String dataIdOne;

  private final String dataIdTwo;

  private final String instanceId;

  private final String group;

  public FetchDataInfoIDBlackListServiceTest() {
    dataIdOne = "dataid.black.list.test";
    dataIdTwo = "dataid.black.list.test2";
    instanceId = "DEFAULT_INSTANCE_ID";
    group = "dataid-black-list-test-group";

    dataInfoIdOne = String.format("%s#@#%s#@#%s", dataIdOne, instanceId, group);
    dataInfoIdTwo = String.format("%s#@#%s#@#%s", dataIdTwo, instanceId, group);
  }

  @Test
  public void testFetchDataAndCleanProvider() {
    FetchDataInfoIDBlackListService fetchDataInfoIDBlackListService = null;
    SessionDataStore sessionDataStore = null;
    try {
      // 创建 Mock 的依赖资源以创建 FetchDataInfoIDBlackListService 对象
      // MetaServerService 需要定制以支持 Mock 更新 Provider Data 中的数据
      MockFetchSystemPropertyAnswer mockFetchSystemPropertyAnswer =
          new MockFetchSystemPropertyAnswer();
      MetaServerService metaServerService = mock(MetaServerService.class);
      when(metaServerService.fetchSystemProperty(anyString(), anyLong()))
          .then(mockFetchSystemPropertyAnswer);

      SessionServerConfig sessionServerConfig = mock(SessionServerConfig.class);
      // 防止后台线程空转
      when(sessionServerConfig.getSystemPropertyIntervalMillis())
          .thenReturn((int) TimeUnit.MILLISECONDS.toMillis(50));

      WriteDataAcceptor writeDataAcceptor = mock(WriteDataAcceptor.class);

      SlotTableCache slotTableCache = mock(SlotTableCache.class);
      when(slotTableCache.slotOf(anyString())).thenReturn(0);
      sessionDataStore = new SessionDataStore();
      sessionDataStore.setSlotTableCache(slotTableCache);

      // 添加 Mock 的 Publisher，后面拉黑后再检查是否删除了
      Publisher publisherOne = new Publisher();
      publisherOne.setDataId(this.dataIdOne);
      publisherOne.setInstanceId(this.instanceId);
      publisherOne.setGroup(this.group);
      publisherOne.setDataInfoId(this.dataInfoIdOne);
      publisherOne.setPublishType(PublishType.NORMAL);
      publisherOne.setRegisterId("1");
      publisherOne.setSourceAddress(URL.valueOf("127.0.0.1:1234"));
      publisherOne.setTargetAddress(URL.valueOf("127.0.0.1:9600"));

      Publisher publisherTwo = new Publisher();
      publisherTwo.setDataId(this.dataIdTwo);
      publisherTwo.setInstanceId(this.instanceId);
      publisherTwo.setGroup(this.group);
      publisherTwo.setDataInfoId(this.dataInfoIdTwo);
      publisherTwo.setPublishType(PublishType.NORMAL);
      publisherTwo.setRegisterId("2");
      publisherTwo.setSourceAddress(URL.valueOf("127.0.0.1:1235"));
      publisherTwo.setTargetAddress(URL.valueOf("127.0.0.1:9600"));

      Publisher publisherThree = new Publisher();
      publisherThree.setDataId(this.dataIdTwo);
      publisherThree.setInstanceId(this.instanceId);
      publisherThree.setGroup(this.group);
      publisherThree.setDataInfoId(this.dataInfoIdTwo);
      publisherThree.setPublishType(PublishType.NORMAL);
      publisherThree.setRegisterId("3");
      publisherThree.setSourceAddress(URL.valueOf("127.0.0.1:1236"));
      publisherThree.setTargetAddress(URL.valueOf("127.0.0.1:9600"));

      // 这里模拟添加了三个不同客户端的 Publisher
      // 第一个发布的 DataIdOne，第二个和第三个都发布的 DataIdTwo
      Assert.assertTrue(sessionDataStore.add(publisherOne));
      Assert.assertNotNull(
          sessionDataStore.queryById(publisherOne.getRegisterId(), publisherOne.getDataInfoId()));
      Assert.assertTrue(sessionDataStore.add(publisherTwo));
      Assert.assertNotNull(
          sessionDataStore.queryById(publisherTwo.getRegisterId(), publisherTwo.getDataInfoId()));
      Assert.assertTrue(sessionDataStore.add(publisherThree));
      Assert.assertNotNull(
          sessionDataStore.queryById(
              publisherThree.getRegisterId(), publisherThree.getDataInfoId()));

      fetchDataInfoIDBlackListService = new FetchDataInfoIDBlackListService();
      fetchDataInfoIDBlackListService.setSessionDataStore(sessionDataStore);
      fetchDataInfoIDBlackListService.setWriteDataAcceptor(writeDataAcceptor);
      fetchDataInfoIDBlackListService.setSessionServerConfig(sessionServerConfig);
      fetchDataInfoIDBlackListService.setMetaNodeService(metaServerService);

      Assert.assertFalse(fetchDataInfoIDBlackListService.isInBlackList(this.dataInfoIdOne));
      Assert.assertFalse(fetchDataInfoIDBlackListService.isInBlackList(this.dataInfoIdTwo));

      // 初期先准备一个模拟数据，版本号为 1
      Set<String> mockDataOne = new HashSet<>();
      mockDataOne.add(this.dataInfoIdOne);
      String mockDataJsonOne = JsonUtils.writeValueAsString(mockDataOne);
      ServerDataBox serverDataBoxOne = new ServerDataBox(mockDataJsonOne);
      ProvideData provideDataOne =
          new ProvideData(serverDataBoxOne, ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID, 1L);
      mockFetchSystemPropertyAnswer.setProvideData(provideDataOne);

      // 开启后台数据拉取
      fetchDataInfoIDBlackListService.start();

      // 等 300ms，让它处理完
      this.sleep(300L, TimeUnit.MILLISECONDS);

      // 获取一下数据，检查是否已经拉取到了
      DataInfoIDBlacklistStorage storageOne = fetchDataInfoIDBlackListService.getStorage().get();
      Assert.assertNotNull(storageOne);
      Set<String> setInStorageOne = storageOne.getDataInfoIds();
      Assert.assertEquals(1, setInStorageOne.size());
      Assert.assertTrue(setInStorageOne.contains(this.dataInfoIdOne));
      Assert.assertEquals(1L, storageOne.getVersion());
      Assert.assertTrue(fetchDataInfoIDBlackListService.isInBlackList(this.dataInfoIdOne));
      Assert.assertFalse(fetchDataInfoIDBlackListService.isInBlackList(this.dataInfoIdTwo));

      // 拉黑了 DataIDOne，预期第一个 Publisher 已经不存在了
      Assert.assertNull(
          sessionDataStore.queryById(publisherOne.getRegisterId(), publisherOne.getDataInfoId()));

      // 将数据更新为版本 2
      Set<String> mockDataTwo = new HashSet<>();
      mockDataTwo.add(this.dataInfoIdOne);
      mockDataTwo.add(this.dataInfoIdTwo);
      String mockDataJsonTwo = JsonUtils.writeValueAsString(mockDataTwo);
      ServerDataBox serverDataBoxTwo = new ServerDataBox(mockDataJsonTwo);
      ProvideData provideDataTwo =
          new ProvideData(serverDataBoxTwo, ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID, 2L);
      mockFetchSystemPropertyAnswer.setProvideData(provideDataTwo);

      // 等 300ms，让它处理完
      this.sleep(300L, TimeUnit.MILLISECONDS);

      // 获取一下数据，检查是否已经拉取到了
      DataInfoIDBlacklistStorage storageTwo = fetchDataInfoIDBlackListService.getStorage().get();
      Assert.assertNotNull(storageTwo);
      Set<String> setInStorageTwo = storageTwo.getDataInfoIds();
      Assert.assertEquals(2, setInStorageTwo.size());
      Assert.assertTrue(setInStorageTwo.contains(this.dataInfoIdOne));
      Assert.assertTrue(setInStorageTwo.contains(this.dataInfoIdTwo));
      Assert.assertEquals(2L, storageTwo.getVersion());
      Assert.assertTrue(fetchDataInfoIDBlackListService.isInBlackList(this.dataInfoIdOne));
      Assert.assertTrue(fetchDataInfoIDBlackListService.isInBlackList(this.dataInfoIdTwo));

      // 拉黑了 DataIDTwo，预期第二个和第三个 Publisher 都已经不存在了
      Assert.assertNull(
          sessionDataStore.queryById(publisherTwo.getRegisterId(), publisherTwo.getDataInfoId()));
      Assert.assertNull(
          sessionDataStore.queryById(
              publisherThree.getRegisterId(), publisherThree.getDataInfoId()));
    } finally {
      if (null != fetchDataInfoIDBlackListService) {
        fetchDataInfoIDBlackListService.shutdownWatchDog();
      }

      if (null != sessionDataStore) {
        sessionDataStore.shutdownWatchDog();
      }
    }
  }

  private void sleep(Long time, TimeUnit timeUnit) {
    try {
      Thread.sleep(timeUnit.toMillis(time));
    } catch (InterruptedException interruptedException) {
      // 忽略
    }
  }
}

class MockFetchSystemPropertyAnswer implements Answer<FetchSystemPropertyResult> {

  private volatile ProvideData provideData;

  @Override
  public FetchSystemPropertyResult answer(InvocationOnMock invocation) throws Throwable {
    ProvideData provideData = this.provideData;
    Long versionL = provideData.getVersion();
    long version = versionL == null ? 0 : versionL;

    Long versionInParamL = invocation.getArgumentAt(1, Long.class);
    long versionInParam = versionInParamL == null ? 0 : versionInParamL;

    if (version > versionInParam) {
      return new FetchSystemPropertyResult(true, provideData);
    } else {
      return new FetchSystemPropertyResult(false, provideData);
    }
  }

  public void setProvideData(ProvideData provideData) {
    this.provideData = provideData;
  }
}
