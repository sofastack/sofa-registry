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

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CompressPushServiceTest extends CompressPushService {
  @Before
  public void before() {}

  @Test
  public void test() {
    Assert.assertFalse(getCompressSwitch().isEnabled());
    doProcess(
        storage.get(),
        new ProvideData(
            new ServerDataBox("{\"enabled\": true, \"forbidEncodes\": [\"zstd\"]}"),
            ValueConstants.COMPRESS_PUSH_SWITCH_DATA_ID,
            2L));
    Assert.assertTrue(getCompressSwitch().isEnabled());
    Assert.assertEquals(1, getCompressSwitch().getForbidEncodes().size());

    Assert.assertTrue(getCompressSwitch().isEnabled());
    doProcess(
        storage.get(),
        new ProvideData(
            new ServerDataBox("{\"enabled\": false}"),
            ValueConstants.COMPRESS_PUSH_SWITCH_DATA_ID,
            2L));
    Assert.assertFalse(getCompressSwitch().isEnabled());
    Assert.assertEquals(0, getCompressSwitch().getForbidEncodes().size());

    doProcess(
        storage.get(),
        new ProvideData(
            new ServerDataBox("{\"enabled\": true}"),
            ValueConstants.COMPRESS_PUSH_SWITCH_DATA_ID,
            3L));
    doProcess(
        storage.get(),
        new ProvideData(new ServerDataBox(""), ValueConstants.COMPRESS_PUSH_SWITCH_DATA_ID, 4L));
    Assert.assertTrue(getCompressSwitch().isEnabled());
  }
}
