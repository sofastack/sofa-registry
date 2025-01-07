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
package com.alipay.sofa.registry.server.session.wrapper;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.common.model.wrapper.Wrapper;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInvocation;
import com.alipay.sofa.registry.server.session.providedata.FetchDataInfoIDBlackListService;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author huicha
 * @date 2024/12/16
 */
public class DataInfoIDBlacklistWrapperInterceptorTest {

  private final String dataInfoIdOne;

  private final String dataInfoIdTwo;

  private final String dataIdOne;

  private final String dataIdTwo;

  private final String instanceId;

  private final String group;

  public DataInfoIDBlacklistWrapperInterceptorTest() {
    dataIdOne = "dataid.black.list.test";
    dataIdTwo = "dataid.black.list.test2";
    instanceId = "DEFAULT_INSTANCE_ID";
    group = "dataid-black-list-test-group";

    dataInfoIdOne = String.format("%s#@#%s#@#%s", dataIdOne, instanceId, group);
    dataInfoIdTwo = String.format("%s#@#%s#@#%s", dataIdTwo, instanceId, group);
  }

  @Test
  public void test() throws Exception {
    MockIsInBlackListAnswer mockIsInBlackListAnswer = new MockIsInBlackListAnswer();
    FetchDataInfoIDBlackListService fetchDataInfoIDBlackListService =
        mock(FetchDataInfoIDBlackListService.class);
    when(fetchDataInfoIDBlackListService.isInBlackList(anyString())).then(mockIsInBlackListAnswer);

    DataInfoIDBlacklistWrapperInterceptor interceptor = new DataInfoIDBlacklistWrapperInterceptor();
    interceptor.setFetchDataInfoIDBlackListService(fetchDataInfoIDBlackListService);

    // 黑名单只设置 dataInfoOne 一个
    Set<String> dataInfos = new HashSet<>();
    dataInfos.add(this.dataInfoIdOne);
    mockIsInBlackListAnswer.setDataInfoIds(dataInfos);

    // 先测试 Sub
    // 创建一个 dataIdOne 的订阅
    Subscriber subscriber = new Subscriber();
    subscriber.setDataInfoId(this.dataInfoIdOne);
    subscriber.setDataId(this.dataIdOne);
    subscriber.setGroup(this.group);
    subscriber.setInstanceId(this.instanceId);
    RegisterInvokeData registerInvokeDataSub = new RegisterInvokeData(subscriber, null);
    TouchChecker touchCheckerSub = new TouchChecker(registerInvokeDataSub);

    // 模拟执行 Sub 流程
    WrapperInvocation<RegisterInvokeData, Boolean> invocationSub =
        new WrapperInvocation<>(touchCheckerSub, Collections.singletonList(interceptor));
    invocationSub.proceed();
    // 因为我们预期不处理 Sub，TouchChecker 应该是会被执行的，因此这里检查应该为 True
    Assert.assertTrue(touchCheckerSub.hasBeenChecked());

    // 再测试 Pub
    // 创建一个 dataIdTwo 的发布
    Publisher publisherTwo = new Publisher();
    publisherTwo.setDataInfoId(this.dataInfoIdTwo);
    publisherTwo.setDataId(this.dataIdTwo);
    publisherTwo.setGroup(this.group);
    publisherTwo.setInstanceId(this.instanceId);
    RegisterInvokeData registerInvokeDataPubTwo = new RegisterInvokeData(publisherTwo, null);
    TouchChecker touchCheckerPubTwo = new TouchChecker(registerInvokeDataPubTwo);

    // 模拟执行 Pub 流程
    WrapperInvocation<RegisterInvokeData, Boolean> invocationPubTwo =
        new WrapperInvocation<>(touchCheckerPubTwo, Collections.singletonList(interceptor));
    invocationPubTwo.proceed();
    // 因为黑名单中只有 dataIdOne，因此这里预期会执行 TouchChecker，因此这里检查应该为 True
    Assert.assertTrue(touchCheckerPubTwo.hasBeenChecked());

    // 测试 Pub
    // 创建一个 dataIdOne 的发布
    Publisher publisherOne = new Publisher();
    publisherOne.setDataInfoId(this.dataInfoIdOne);
    publisherOne.setDataId(this.dataIdOne);
    publisherOne.setGroup(this.group);
    publisherOne.setInstanceId(this.instanceId);
    RegisterInvokeData registerInvokeDataPubOne = new RegisterInvokeData(publisherOne, null);
    TouchChecker touchCheckerPubOne = new TouchChecker(registerInvokeDataPubOne);

    // 模拟执行 Pub 流程
    WrapperInvocation<RegisterInvokeData, Boolean> invocationPubOne =
        new WrapperInvocation<>(touchCheckerPubOne, Collections.singletonList(interceptor));
    invocationPubOne.proceed();
    // 因为黑名单中有 dataIdOne，因此这里预期会跳过执行 TouchChecker，因此这里检查应该为 False
    Assert.assertFalse(touchCheckerPubOne.hasBeenChecked());
  }
}

class MockIsInBlackListAnswer implements Answer<Boolean> {

  private volatile Set<String> dataInfoIds = new HashSet<>();

  @Override
  public Boolean answer(InvocationOnMock invocation) throws Throwable {
    String dataInfoId = invocation.getArgumentAt(0, String.class);
    return this.dataInfoIds.contains(dataInfoId);
  }

  public void setDataInfoIds(Set<String> dataInfoIds) {
    this.dataInfoIds = dataInfoIds;
  }
}

class TouchChecker implements Wrapper<RegisterInvokeData, Boolean> {

  private boolean touched;

  private RegisterInvokeData registerInvokeData;

  public TouchChecker(RegisterInvokeData registerInvokeData) {
    this.touched = false;
    this.registerInvokeData = registerInvokeData;
  }

  @Override
  public Boolean call() {
    this.touched = true;
    return true;
  }

  @Override
  public Supplier<RegisterInvokeData> getParameterSupplier() {
    return this::getRegisterInvokeData;
  }

  public RegisterInvokeData getRegisterInvokeData() {
    return registerInvokeData;
  }

  public void setRegisterInvokeData(RegisterInvokeData registerInvokeData) {
    this.registerInvokeData = registerInvokeData;
  }

  public boolean hasBeenChecked() {
    return this.touched;
  }
}
