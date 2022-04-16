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
package com.alipay.sofa.registry.server.session.converter;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.SubPublisher;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.predicate.ZonePredicate;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ReceivedDataConverterTest {
  private final String dataId = "testDataId";
  private final String localZone = "testLocalZone";
  private final SubPublisher subPublisher =
      TestUtils.newSubPublisher(1, System.currentTimeMillis());

  @Test
  public void testGetReceivedDataMulti() throws Exception {
    Assert.assertNull(
        ReceivedDataConverter.getReceivedDataMulti(
                null, ScopeEnum.zone, Collections.emptyList(), null, null)
            .getPayload());
    SubDatum subDatum = TestUtils.newSubDatum(dataId, 100, Collections.emptyList());
    List<String> subIds = Collections.singletonList("testSubId");
    ReceivedData data =
        ReceivedDataConverter.getReceivedDataMulti(
                subDatum, ScopeEnum.dataCenter, subIds, localZone, null)
            .getPayload();

    assertReceivedData(data, subDatum, subIds, localZone);
    Assert.assertEquals(data.getData().size(), 0);

    SessionServerConfigBean configBean = TestUtils.newSessionConfig("testDc");
    subDatum = TestUtils.newSubDatum(dataId, 100, Collections.singletonList(subPublisher));
    data =
        ReceivedDataConverter.getReceivedDataMulti(
                subDatum,
                ScopeEnum.dataCenter,
                subIds,
                localZone,
                ZonePredicate.pushDataPredicate(
                    subDatum.getDataId(), localZone, ScopeEnum.dataCenter, configBean))
            .getPayload();

    assertReceivedData(data, subDatum, subIds, localZone);
    Assert.assertEquals(data.getData().size(), 1);
    List<DataBox> dataBoxes = data.getData().get(subPublisher.getCell());
    Assert.assertEquals(dataBoxes.size(), 1);
    Assert.assertEquals(dataBoxes.get(0).getData(), subPublisher.getDataList().get(0).extract());
  }

  @Test
  public void testGetReceivedConfigData() throws Exception {
    ServerDataBox dataBox = new ServerDataBox("testDataBox");
    SubDatum subDatum = TestUtils.newSubDatum(dataId, 10, Collections.emptyList());
    DataInfo dataInfo = DataInfo.valueOf(subDatum.getDataInfoId());
    ReceivedConfigData data = ReceivedDataConverter.getReceivedConfigData(dataBox, dataInfo, 100L);
    Assert.assertEquals(data.getDataId(), dataInfo.getDataId());
    Assert.assertEquals(data.getGroup(), dataInfo.getGroup());
    Assert.assertEquals(data.getInstanceId(), dataInfo.getInstanceId());
    Assert.assertEquals(data.getVersion().longValue(), 100);
    Assert.assertEquals(data.getDataBox().getData(), dataBox.extract());
  }

  private void assertReceivedData(
      ReceivedData data, SubDatum subDatum, List<String> subIds, String localZone) {
    Assert.assertEquals(data.getDataId(), subDatum.getDataId());
    Assert.assertEquals(data.getGroup(), subDatum.getGroup());
    Assert.assertEquals(data.getInstanceId(), subDatum.getInstanceId());
    Assert.assertEquals(data.getSubscriberRegistIds(), subIds);
    Assert.assertEquals(data.getSegment(), subDatum.getDataCenter());
    Assert.assertEquals(data.getScope(), ScopeEnum.dataCenter.name());
    Assert.assertEquals(data.getVersion().longValue(), subDatum.getVersion());
    Assert.assertEquals(data.getLocalZone(), localZone);
  }
}
