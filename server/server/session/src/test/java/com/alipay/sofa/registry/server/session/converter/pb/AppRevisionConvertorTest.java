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
package com.alipay.sofa.registry.server.session.converter.pb;

import com.alipay.sofa.registry.common.model.client.pb.MetaRegister;
import com.alipay.sofa.registry.common.model.client.pb.MetaService;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

public class AppRevisionConvertorTest {
  @Test
  public void test() {
    AppRevision revision = new AppRevision();
    revision.setRevision("testRev");
    revision.setDataCenter("testDc");
    revision.setAppName("testApp");
    AppRevisionInterface inf1 = new AppRevisionInterface();
    inf1.setId("inf1");
    inf1.setServiceParams(Collections.singletonMap("inf11", Lists.newArrayList("1", "2")));
    revision.setInterfaceMap(Collections.singletonMap("svc1", inf1));

    MetaRegister register = AppRevisionConvertor.convert2Pb(revision);

    Assert.assertEquals(register.getApplication(), revision.getAppName());
    Assert.assertEquals(register.getRevision(), revision.getRevision());
    Assert.assertEquals(register.getBaseParamsCount(), revision.getBaseParams().size());
    Assert.assertEquals(register.getServicesCount(), revision.getInterfaceMap().size());
    MetaService metaService = register.getServicesMap().get("svc1");
    Assert.assertEquals(metaService.getId(), inf1.getId());
    Assert.assertEquals(metaService.getParamsCount(), 1);
    Assert.assertEquals(
        Sets.newHashSet(metaService.getParamsMap().get("inf11").getValuesList()),
        Sets.newHashSet("1", "2"));

    AppRevision appRevision = AppRevisionConvertor.convert2Java(register);
    Assert.assertEquals(appRevision.getAppName(), revision.getAppName());
    Assert.assertEquals(appRevision.getRevision(), revision.getRevision());
    Assert.assertEquals(appRevision.getBaseParams().size(), revision.getBaseParams().size());
    Assert.assertEquals(appRevision.getInterfaceMap().size(), revision.getInterfaceMap().size());
    Assert.assertTrue(revision.toString(), revision.toString().contains(revision.getRevision()));

    AppRevisionInterface f1 = appRevision.getInterfaceMap().get("svc1");
    Assert.assertEquals(f1.getId(), inf1.getId());
    Assert.assertEquals(f1.getServiceParams().size(), inf1.getServiceParams().size());
    Assert.assertEquals(f1.getServiceParams(), inf1.getServiceParams());
  }
}
