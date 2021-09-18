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

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.providedata.FetchBlackListService;
import com.alipay.sofa.registry.server.shared.providedata.SystemPropertyProcessorManager;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class NotifyProvideDataChangeHandlerTest {

  private NotifyProvideDataChangeHandler newHandler() {
    NotifyProvideDataChangeHandler handler = new NotifyProvideDataChangeHandler();
    handler.metaNodeExecutor =
        new ThreadPoolExecutor(10, 10, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
    Assert.assertNotNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), ProvideDataChangeEvent.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.META);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    return handler;
  }

  private SystemPropertyProcessorManager newProvideDataProcessorManager() {
    SystemPropertyProcessorManager provideDataProcessorManager =
        new SystemPropertyProcessorManager();
    provideDataProcessorManager.addSystemDataProcessor(new FetchBlackListService());
    return provideDataProcessorManager;
  }

  @Test
  public void testHandle() {
    NotifyProvideDataChangeHandler handler = newHandler();
    handler.systemPropertyProcessorManager = newProvideDataProcessorManager();
    Assert.assertNull(handler.doHandle(null, request(ValueConstants.BLACK_LIST_DATA_ID)));
  }

  private static ProvideDataChangeEvent request(String dataInfoId) {
    return new ProvideDataChangeEvent(dataInfoId, 10);
  }
}
