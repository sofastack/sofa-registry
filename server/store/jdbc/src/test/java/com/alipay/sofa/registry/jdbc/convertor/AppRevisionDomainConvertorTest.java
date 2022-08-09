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
package com.alipay.sofa.registry.jdbc.convertor;

import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

/** @Author dzdx @Date 2022/8/8 11:13 @Version 1.0 */
public class AppRevisionDomainConvertorTest {
  @Test
  public void testEnableConfig() {
    AppRevision rev = new AppRevision();
    AppRevisionInterface appRevisionInterface = new AppRevisionInterface();
    appRevisionInterface.setId("2");
    appRevisionInterface.getServiceParams().put("serviceKey1", Lists.newArrayList("serviceKey1"));
    rev.getInterfaceMap().put("service", appRevisionInterface);
    AppRevisionDomain domain1 = AppRevisionDomainConvertor.convert2Domain("dc2", rev);
    Assert.assertNotNull(domain1.getServiceParams());
    Assert.assertNull(domain1.getServiceParamsLarge());

    AppRevisionDomainConvertor.setEnableConfig(
        new AppRevisionDomainConvertor.EnableConfig(true, true));
    AppRevisionDomain domain2 = AppRevisionDomainConvertor.convert2Domain("dc2", rev);
    Assert.assertNotNull(domain2.getServiceParams());
    Assert.assertNotNull(domain2.getServiceParamsLarge());

    AppRevisionDomainConvertor.setEnableConfig(
        new AppRevisionDomainConvertor.EnableConfig(false, true));
    AppRevisionDomain domain3 = AppRevisionDomainConvertor.convert2Domain("dc2", rev);
    Assert.assertNull(domain3.getServiceParams());
    Assert.assertNotNull(domain3.getServiceParamsLarge());

    AppRevision rev1 = AppRevisionDomainConvertor.convert2Revision(domain1);
    AppRevision rev2 = AppRevisionDomainConvertor.convert2Revision(domain1);
    AppRevision rev3 = AppRevisionDomainConvertor.convert2Revision(domain1);
    Assert.assertEquals(rev1.getInterfaceMap().size(), rev2.getInterfaceMap().size());
    Assert.assertEquals(rev1.getInterfaceMap().size(), rev3.getInterfaceMap().size());
    AppRevisionDomainConvertor.setEnableConfig(new AppRevisionDomainConvertor.EnableConfig());
  }
}
