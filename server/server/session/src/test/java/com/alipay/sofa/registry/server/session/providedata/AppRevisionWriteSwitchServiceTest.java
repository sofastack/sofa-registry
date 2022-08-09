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
import com.alipay.sofa.registry.jdbc.convertor.AppRevisionDomainConvertor;
import org.junit.Assert;
import org.junit.Test;

/** @Author dzdx @Date 2022/8/8 17:25 @Version 1.0 */
public class AppRevisionWriteSwitchServiceTest extends AppRevisionWriteSwitchService {

  @Test
  public void test() {
    Assert.assertTrue(
        doProcess(
            storage.get(),
            new ProvideData(
                new ServerDataBox("{\"serviceParams\": true, \"serviceParamsLarge\": true}"),
                ValueConstants.APP_REVISION_WRITE_SWITCH_DATA_ID,
                2L)));
    AppRevisionDomainConvertor.EnableConfig enableConfig =
        AppRevisionDomainConvertor.getEnableConfig();
    Assert.assertTrue(enableConfig.isServiceParams());
    Assert.assertTrue(enableConfig.isServiceParamsLarge());
    Assert.assertTrue(
        doProcess(
            storage.get(),
            new ProvideData(
                new ServerDataBox("{\"serviceParams\": true, \"serviceParamsLarge\": false}"),
                ValueConstants.APP_REVISION_WRITE_SWITCH_DATA_ID,
                3L)));
    enableConfig = AppRevisionDomainConvertor.getEnableConfig();
    Assert.assertTrue(enableConfig.isServiceParams());
    Assert.assertFalse(enableConfig.isServiceParamsLarge());
  }
}
