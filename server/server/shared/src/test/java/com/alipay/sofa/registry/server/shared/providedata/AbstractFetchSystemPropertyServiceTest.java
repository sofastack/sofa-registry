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
package com.alipay.sofa.registry.server.shared.providedata;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.FetchSystemPropertyResult;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyServiceTest.MyTestStorage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author xiaojian.xj
 * @version $Id: AbstractFetchSystemPropertyServiceTest.java, v 0.1 2021年06月04日 17:41 xiaojian.xj
 *     Exp $
 */
public class AbstractFetchSystemPropertyServiceTest
    extends AbstractFetchSystemPropertyService<MyTestStorage> {

  @Mock MetaServerService metaNodeService;

  @Before
  public void beforeFetchSystemPropertyService() {
    MockitoAnnotations.initMocks(this);
    this.setMetaNodeService(metaNodeService);
  }

  public AbstractFetchSystemPropertyServiceTest() {
    super(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID, new MyTestStorage(INIT_VERSION, false));
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return 1000;
  }

  @Override
  protected boolean doProcess(MyTestStorage expect, ProvideData data) {
    final Boolean stop = ProvideData.toBool(data);
    MyTestStorage update = new MyTestStorage(data.getVersion(), stop);
    if (!compareAndSet(expect, update)) {
      return false;
    }
    return true;
  }

  protected static class MyTestStorage extends SystemDataStorage {
    protected final boolean stopPushSwitch;

    public MyTestStorage(long version, boolean stopPushSwitch) {
      super(version);
      this.stopPushSwitch = stopPushSwitch;
    }
  }

  @Test
  public void test() {
    when(metaNodeService.fetchSystemProperty(anyString(), anyLong()))
        .thenReturn(
            new FetchSystemPropertyResult(
                true,
                new ProvideData(
                    new ServerDataBox("true"),
                    ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID,
                    INIT_VERSION - 1)));

    watchDog.runUnthrowable();

    when(metaNodeService.fetchSystemProperty(anyString(), anyLong()))
        .thenReturn(
            new FetchSystemPropertyResult(
                true,
                new ProvideData(
                    new ServerDataBox("true"),
                    ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID,
                    INIT_VERSION + 1)));

    Assert.assertTrue(doFetchData());
  }
}
