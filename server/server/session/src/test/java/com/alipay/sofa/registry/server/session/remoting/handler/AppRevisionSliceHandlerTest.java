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
package com.alipay.sofa.registry.server.session.remoting.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.cleaner.AppRevisionSlice;
import com.alipay.sofa.registry.common.model.metaserver.cleaner.AppRevisionSliceRequest;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

public class AppRevisionSliceHandlerTest {
  private AppRevisionSliceHandler newHandler() {
    AppRevisionSliceHandler handler = new AppRevisionSliceHandler();
    Assert.assertNull(handler.getExecutor());
    Assert.assertNotNull(handler.interest());
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.META);
    return handler;
  }

  @Test
  public void testHandle() {
    AppRevisionSliceHandler handler = newHandler();
    handler.appRevisionJdbcRepository = mock(AppRevisionRepository.class);
    when(handler.appRevisionJdbcRepository.availableRevisions())
        .thenReturn(
            Lists.newArrayList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"));
    AppRevisionSliceRequest req = new AppRevisionSliceRequest(10, 0);
    AppRevisionSlice slice = (AppRevisionSlice) handler.doHandle(null, req);
    Assert.assertEquals(slice.getRevisions().size(), 3);
  }
}
