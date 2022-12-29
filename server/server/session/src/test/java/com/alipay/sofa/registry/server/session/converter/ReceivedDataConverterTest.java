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
import com.alipay.sofa.registry.common.model.store.MultiSubDatum;
import com.alipay.sofa.registry.common.model.store.PushData;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.SubPublisher;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.MultiReceivedData;
import com.alipay.sofa.registry.core.model.MultiSegmentData;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.predicate.ZonePredicate;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.junit.Test;

public class ReceivedDataConverterTest {
  private final String dataId = "testDataId";
  private final String localDataCenter = "testLocalDataCenter";
  private final String localZone = "testLocalZone";
  private final SubPublisher subPublisher =
      TestUtils.newSubPublisher(1, System.currentTimeMillis());

  @Test
  public void testGetReceivedDataMulti() throws Exception {
    Assert.assertNull(
        ReceivedDataConverter.getReceivedData(
                null, ScopeEnum.zone, Collections.emptyList(), null, localDataCenter, null)
            .getPayload());
    SubDatum subDatum =
        TestUtils.newSubDatum(localDataCenter, dataId, 100, Collections.emptyList());
    List<String> subIds = Collections.singletonList("testSubId");
    ReceivedData data =
        ReceivedDataConverter.getReceivedData(
                MultiSubDatum.of(subDatum),
                ScopeEnum.dataCenter,
                subIds,
                localZone,
                localDataCenter,
                null)
            .getPayload();

    assertReceivedData(data, subDatum, subIds, localZone);
    Assert.assertEquals(data.getData().size(), 0);

    SessionServerConfigBean configBean = TestUtils.newSessionConfig("testDc");
    subDatum =
        TestUtils.newSubDatum(
            localDataCenter, dataId, 100, Collections.singletonList(subPublisher));
    data =
        ReceivedDataConverter.getReceivedData(
                MultiSubDatum.of(subDatum),
                ScopeEnum.dataCenter,
                subIds,
                localZone,
                localDataCenter,
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
    SubDatum subDatum = TestUtils.newSubDatum(localDataCenter, dataId, 10, Collections.emptyList());
    DataInfo dataInfo = DataInfo.valueOf(subDatum.getDataInfoId());
    ReceivedConfigData data = ReceivedDataConverter.getReceivedConfigData(dataBox, dataInfo, 100L);
    Assert.assertEquals(data.getDataId(), dataInfo.getDataId());
    Assert.assertEquals(data.getGroup(), dataInfo.getGroup());
    Assert.assertEquals(data.getInstanceId(), dataInfo.getInstanceId());
    Assert.assertEquals(data.getVersion().longValue(), 100);
    Assert.assertEquals(data.getDataBox().getData(), dataBox.extract());
  }

  @Test
  public void testGetMultiReceivedData() {
    MultiSubDatum multiSubDatum = TestUtils.newMultiSubDatum("testGetMultiReceivedData", 3, 3);
    Entry<String, SubDatum> first =
        multiSubDatum.getDatumMap().entrySet().stream().findFirst().get();
    String localDataCenter = first.getKey();
    String localZone = "localZone";

    SessionServerConfigBean sessionServerConfig =
        TestUtils.newSessionConfig(localDataCenter, localZone);

    Predicate<String> pushDataPredicate =
        ZonePredicate.pushDataPredicate(
            multiSubDatum.getDataId(), localZone, ScopeEnum.global, sessionServerConfig);
    Map<String, Set<String>> segmentZones = com.google.common.collect.Maps.newHashMap();
    Map<String, String> zone2DataCenter = com.google.common.collect.Maps.newHashMap();

    for (Entry<String, SubDatum> entry : multiSubDatum.getDatumMap().entrySet()) {
      Set<String> cells = Sets.newHashSet();
      for (SubPublisher pub : entry.getValue().mustGetPublishers()) {
        cells.add(pub.getCell());
        zone2DataCenter.put(pub.getCell(), entry.getKey());
      }
      segmentZones.put(entry.getKey(), cells);
    }

    PushData<MultiReceivedData> pushData =
        ReceivedDataConverter.getMultiReceivedData(
            multiSubDatum,
            ScopeEnum.global,
            com.google.common.collect.Lists.newArrayList("aaa"),
            localZone,
            localDataCenter,
            pushDataPredicate,
            segmentZones);

    Assert.assertEquals(7, pushData.getPayload().getMultiData().size());
    for (Entry<String, MultiSegmentData> entry : pushData.getPayload().getMultiData().entrySet()) {
      if (StringUtils.equals(entry.getKey(), localDataCenter)) {
        ParaCheckUtil.checkEquals(entry.getValue().getDataCount().size(), 3, "zoneCount");

        ParaCheckUtil.checkEquals(
            entry.getValue().getVersion(),
            multiSubDatum.getDatumMap().get(entry.getKey()).getVersion(),
            "datum.version");
        entry
            .getValue()
            .getDataCount()
            .values()
            .forEach(count -> ParaCheckUtil.checkEquals(count, 1, "pubCount"));
      } else {
        ParaCheckUtil.checkEquals(entry.getValue().getDataCount().size(), 1, "zoneCount");
        ParaCheckUtil.checkEquals(
            entry.getValue().getDataCount().values().stream().findFirst().get(), 1, "pubCount");
        String dataCenter = zone2DataCenter.get(entry.getKey());
        ParaCheckUtil.checkEquals(
            entry.getValue().getVersion(),
            multiSubDatum.getDatumMap().get(dataCenter).getVersion(),
            "datum.version");
      }
    }
  }

  @Test
  public void testGetMultiReceivedDataWithInvalidForeverZone() {

    PushData<MultiReceivedData> pushData =
        TestUtils.createPushData("testGetMultiReceivedDataWithInvalidForeverZone", 3, 3);
    String localDataCenter = pushData.getPayload().getLocalSegment();

    Assert.assertEquals(8, pushData.getPayload().getMultiData().size());
    for (Entry<String, MultiSegmentData> entry : pushData.getPayload().getMultiData().entrySet()) {
      if (StringUtils.equals(entry.getKey(), localDataCenter)) {
        ParaCheckUtil.checkEquals(entry.getValue().getDataCount().size(), 2, "zoneCount");
        entry
            .getValue()
            .getDataCount()
            .values()
            .forEach(count -> ParaCheckUtil.checkEquals(count, 1, "pubCount"));
      } else {
        ParaCheckUtil.checkEquals(entry.getValue().getDataCount().size(), 1, "zoneCount");
        ParaCheckUtil.checkEquals(
            entry.getValue().getDataCount().values().stream().findFirst().get(), 1, "pubCount");
      }
    }
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
