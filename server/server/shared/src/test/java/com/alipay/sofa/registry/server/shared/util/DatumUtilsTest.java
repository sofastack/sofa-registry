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
package com.alipay.sofa.registry.server.shared.util;

import com.alipay.sofa.registry.common.model.PublishSource;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.DatumVersion;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import java.util.Collections;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xuanbei
 * @since 2019/2/12
 */
public class DatumUtilsTest {
  @Test
  public void testNewEmptySubDatum() {
    Subscriber subscriber = new Subscriber();
    subscriber.setDataId("subscriber-dataId");
    subscriber.setGroup("DEFAULT_GROUP");
    subscriber.setInstanceId("InstanceId");
    subscriber.setDataInfoId("dataInfoId");
    SubDatum subDatum =
        DatumUtils.newEmptySubDatum(subscriber, "testDc", ValueConstants.DEFAULT_NO_DATUM_VERSION);
    Assert.assertEquals(subDatum.getDataCenter(), "testDc");
    Assert.assertEquals(subDatum.getDataInfoId(), subscriber.getDataInfoId());
    Assert.assertEquals(subDatum.getDataId(), subscriber.getDataId());
    Assert.assertEquals(subDatum.getInstanceId(), subscriber.getInstanceId());
    Assert.assertEquals(subDatum.getGroup(), subscriber.getGroup());
    Assert.assertEquals(subDatum.getVersion(), ValueConstants.DEFAULT_NO_DATUM_VERSION);
    Assert.assertTrue(subDatum.getPublishers().isEmpty());
  }

  @Test
  public void testIntern() {
    Map<String, DatumVersion> m = Collections.singletonMap("test", DatumVersion.of(100));
    Assert.assertEquals(DatumUtils.intern(m), m);
  }

  @Test
  public void testDatum() {
    Datum datum = new Datum();
    Publisher publisher = new Publisher();
    publisher.setRegisterId("testRegisterId");
    publisher.setCell("testCell");
    publisher.setDataList(Collections.emptyList());
    publisher.setVersion(100);
    publisher.setRegisterTimestamp(System.currentTimeMillis());
    publisher.setPublishSource(PublishSource.CLIENT);
    datum.addPublisher(publisher);
    datum.setDataCenter("testDc");
    datum.setDataId("testDataId");
    datum.setVersion(200);
    datum.setInstanceId("testInstanceId");
    datum.setGroup("testGroup");
    datum.setDataInfoId("testDataInfoId");
    SubDatum subDatum = DatumUtils.of(datum);

    Assert.assertEquals(subDatum.getDataCenter(), datum.getDataCenter());
    Assert.assertEquals(subDatum.getDataInfoId(), datum.getDataInfoId());
    Assert.assertEquals(subDatum.getDataId(), datum.getDataId());
    Assert.assertEquals(subDatum.getInstanceId(), datum.getInstanceId());
    Assert.assertEquals(subDatum.getGroup(), datum.getGroup());
    Assert.assertEquals(subDatum.getVersion(), datum.getVersion());

    Publisher p = datum.getPubMap().get(publisher.getRegisterId());

    Assert.assertEquals(p.getRegisterId(), publisher.getRegisterId());
    Assert.assertEquals(p.getCell(), publisher.getCell());
    Assert.assertEquals(p.getDataList(), publisher.getDataList());
    Assert.assertEquals(p.getVersion(), publisher.getVersion());
    Assert.assertEquals(p.getRegisterTimestamp(), publisher.getRegisterTimestamp());
    Assert.assertEquals(p.getPublishSource(), publisher.getPublishSource());
  }
}
