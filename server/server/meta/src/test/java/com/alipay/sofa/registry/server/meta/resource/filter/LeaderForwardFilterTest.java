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
package com.alipay.sofa.registry.server.meta.resource.filter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.meta.AbstractH2DbTestBase;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Set;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huicha
 * @date 2024/12/24
 */
public class LeaderForwardFilterTest extends AbstractH2DbTestBase {

  @Autowired private LeaderForwardFilter leaderForwardFilter;

  @Test
  public void test() {
    AmILeaderAnswer amILeaderAnswer = new AmILeaderAnswer();
    MetaLeaderService metaLeaderService = mock(MetaLeaderService.class);
    when(metaLeaderService.amILeader()).then(amILeaderAnswer);
    when(metaLeaderService.getLeader()).thenReturn("127.0.0.1");
    this.leaderForwardFilter.setMetaLeaderService(metaLeaderService);

    // 1. 首先请求一次，不触发转发，拿到正常的响应结果
    amILeaderAnswer.setFirstTime(false);
    Result firstResult = null;
    try (Response firstResponse = this.sendAddBlackListRequest()) {
      Assert.assertEquals(HttpStatus.OK_200, firstResponse.getStatus());
      firstResult = firstResponse.readEntity(Result.class);
    }

    // 2. 然后再请求一次，触发转发，对比两次请求的结果，这两次请求结果必须一致
    amILeaderAnswer.setFirstTime(true);

    try (Response secondResponse = this.sendAddBlackListRequest()) {
      Assert.assertEquals(HttpStatus.OK_200, secondResponse.getStatus());
      Result secondResult = secondResponse.readEntity(Result.class);

      Assert.assertEquals(firstResult.isSuccess(), secondResult.isSuccess());
      Assert.assertEquals(firstResult.getMessage(), secondResult.getMessage());
    }

    // 3. 再测试一个 Get 的例子，由于前面成功添加了一个黑名单，因此后面这里一定能查询到这个添加的数据
    amILeaderAnswer.setFirstTime(true);
    try (Response getResponse = this.sendQueryBlackListRequest()) {
      Assert.assertEquals(HttpStatus.OK_200, getResponse.getStatus());
      Result getResult = getResponse.readEntity(Result.class);

      Assert.assertTrue(getResult.isSuccess());

      String blackListJson = getResult.getMessage();
      Set<String> blackList = JsonUtils.read(blackListJson, new TypeReference<Set<String>>() {});
      Assert.assertEquals(1, blackList.size());

      DataInfo dataInfo = new DataInfo("test-instance-id", "test-data-id", "test-group");
      Assert.assertTrue(blackList.contains(dataInfo.getDataInfoId()));
    }
  }

  private Response sendQueryBlackListRequest() {
    return JerseyClient.getInstance()
        .connect(new URL("127.0.0.1", 9615))
        .getWebTarget()
        .path("datainfoid/blacklist/query")
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get();
  }

  private Response sendAddBlackListRequest() {
    Form form = new Form();
    form.param("dataId", "test-data-id");
    form.param("group", "test-group");
    form.param("instanceId", "test-instance-id");

    return JerseyClient.getInstance()
        .connect(new URL("127.0.0.1", 9615))
        .getWebTarget()
        .path("datainfoid/blacklist/add")
        .request(MediaType.APPLICATION_JSON_TYPE)
        .buildPost(Entity.form(form))
        .invoke();
  }
}

class AmILeaderAnswer implements Answer<Boolean> {

  // 单测，这里不考虑并发问题
  private boolean firstTime = true;

  @Override
  public Boolean answer(InvocationOnMock invocation) throws Throwable {
    if (!firstTime) {
      // 不是第一次请求就返回是 Leader，请求不做代理
      return true;
    } else {
      // 第一次请求，返回不是 Leader，触发请求代理
      this.firstTime = false;
      return false;
    }
  }

  public void setFirstTime(boolean firstTime) {
    this.firstTime = firstTime;
  }
}
