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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.meta.AbstractH2DbTestBase;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class LeaderAwareFilterTest extends AbstractH2DbTestBase {

  @Autowired private LeaderAwareFilter leaderAwareFilter;

  private MetaLeaderService metaLeaderService;

  @Before
  public void beforeLeaderAwareFilterTest() {
    metaLeaderService = mock(MetaLeaderService.class);
    leaderAwareFilter.setMetaLeaderService(metaLeaderService);
  }

  @Test
  public void testFilter() throws IOException, URISyntaxException {
    when(metaLeaderService.amILeader()).thenReturn(false).thenReturn(true);
    when(metaLeaderService.getLeader()).thenReturn("127.0.0.1");
    when(metaLeaderService.getLeaderEpoch()).thenReturn(DatumVersionUtil.nextId());
    Response response =
        JerseyClient.getInstance()
            .connect(new URL("127.0.0.1", 9615))
            .getWebTarget()
            .path("openapi/v1/slot/table/reconcile/status")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
    Assert.assertEquals(200, response.getStatus());
  }
}
