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
package com.alipay.sofa.registry.client.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.alipay.sofa.registry.client.api.model.UserData;
import com.alipay.sofa.registry.client.model.SegmentData;
import com.alipay.sofa.registry.core.model.DataBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * To test DefaultSubscriber.
 *
 * @author hui.shih
 * @version $Id: DefaultSubscriberTest.java, v 0.1 2018-03-26 16:11 hui.shih Exp $$
 */
public class DefaultSubscriberTest {

  public final String ZHEJIANG = "Zhejiang";
  public final String HANGZHOU = "Hangzhou";
  public final String NINGBO = "Ningbo";
  public final String WENZHOU = "Wenzhou";

  public final String JIANGSU = "Jiangsu";
  public final String NANJING = "Nanjing";

  public final String FUJIAN = "Fujian";
  public final String FUZHOU = "Fuzhou";
  public final String XIAMEN = "Xiamen";

  public final String segmentA = "a";

  public final String segmentB = "b";

  @Test
  public void testPutReceivedData() {

    String localZone = ZHEJIANG;

    DefaultRegistryClientConfig config = DefaultRegistryClientConfigBuilder.start().build();
    DefaultSubscriber defaultSubscriber = new DefaultSubscriber(null, null, config);

    UserData initUserData = defaultSubscriber.peekData();

    assertNull(initUserData.getLocalZone());
    assertTrue(initUserData.getZoneData().isEmpty());

    long versionA = 0;
    long versionB = 0;

    // 1. receive data normally
    Map<String, List<DataBox>> map1 = new HashMap<String, List<DataBox>>();
    addToDataBoxMap(map1, ZHEJIANG, HANGZHOU);
    SegmentData sd1 = new SegmentData();
    sd1.setSegment(segmentA);
    sd1.setVersion(++versionA);
    sd1.setData(map1);

    defaultSubscriber.putReceivedData(sd1, localZone);
    UserData userData1 = defaultSubscriber.peekData();

    Map<String, List<String>> expectedMap1 = new HashMap<String, List<String>>();
    addToStringMap(expectedMap1, ZHEJIANG, HANGZHOU);

    assertEquals(localZone, userData1.getLocalZone());
    assertZoneDataEquals(expectedMap1, userData1.getZoneData());

    // 2. receive data normally again
    Map<String, List<DataBox>> map2 = new HashMap<String, List<DataBox>>();
    addToDataBoxMap(map2, ZHEJIANG, NINGBO, WENZHOU);
    SegmentData sd2 = new SegmentData();
    sd2.setSegment(segmentA);
    sd2.setVersion(++versionA);
    sd2.setData(map2);

    defaultSubscriber.putReceivedData(sd2, localZone);
    UserData userData2 = defaultSubscriber.peekData();

    Map<String, List<String>> expectedMap2 = new HashMap<String, List<String>>();
    addToStringMap(expectedMap2, ZHEJIANG, NINGBO, WENZHOU);

    assertEquals(localZone, userData2.getLocalZone());
    assertZoneDataEquals(expectedMap2, userData2.getZoneData());

    // 3. illegal version
    Map<String, List<DataBox>> map3 = new HashMap<String, List<DataBox>>();
    addToDataBoxMap(map3, ZHEJIANG, NINGBO);
    SegmentData sd3 = new SegmentData();
    sd3.setSegment(segmentA);
    sd3.setVersion(versionA);
    sd3.setData(map3);

    defaultSubscriber.putReceivedData(sd3, localZone);
    UserData userData3 = defaultSubscriber.peekData();

    assertZoneDataEquals(expectedMap2, userData3.getZoneData());

    // 4. from another segment
    Map<String, List<DataBox>> map4 = new HashMap<String, List<DataBox>>();
    addToDataBoxMap(map4, ZHEJIANG, HANGZHOU);
    SegmentData sd4 = new SegmentData();
    sd4.setSegment(segmentB);
    sd4.setVersion(++versionB);
    sd4.setData(map4);

    defaultSubscriber.putReceivedData(sd4, localZone);
    UserData userData4 = defaultSubscriber.peekData();

    Map<String, List<String>> expectedMap4 = new HashMap<String, List<String>>();
    addToStringMap(expectedMap4, ZHEJIANG, HANGZHOU, NINGBO, WENZHOU);

    assertZoneDataEquals(expectedMap4, userData4.getZoneData());

    // 5. another segment, another zone
    Map<String, List<DataBox>> map5 = new HashMap<String, List<DataBox>>();
    addToDataBoxMap(map5, JIANGSU, NANJING);
    SegmentData sd5 = new SegmentData();
    sd5.setSegment(segmentB);
    sd5.setVersion(++versionB);
    sd5.setData(map5);

    defaultSubscriber.putReceivedData(sd5, localZone);
    UserData userData5 = defaultSubscriber.peekData();

    Map<String, List<String>> expectedMap5 = new HashMap<String, List<String>>();
    addToStringMap(expectedMap5, ZHEJIANG, NINGBO, WENZHOU);
    addToStringMap(expectedMap5, JIANGSU, NANJING);

    assertZoneDataEquals(expectedMap5, userData5.getZoneData());

    // 6. push empty
    Map<String, List<DataBox>> map6 = new HashMap<String, List<DataBox>>();
    SegmentData sd6 = new SegmentData();
    sd6.setSegment(segmentA);
    sd6.setVersion(++versionA);
    sd6.setData(map6);

    defaultSubscriber.putReceivedData(sd6, localZone);
    UserData userData6 = defaultSubscriber.peekData();

    Map<String, List<String>> expectedMap6 = new HashMap<String, List<String>>();
    addToStringMap(expectedMap6, JIANGSU, NANJING);

    assertZoneDataEquals(expectedMap6, userData6.getZoneData());

    // 7. different segments without intersection
    Map<String, List<DataBox>> map7 = new HashMap<String, List<DataBox>>();
    addToDataBoxMap(map7, ZHEJIANG, NINGBO, WENZHOU);
    addToDataBoxMap(map7, FUJIAN, XIAMEN);

    SegmentData sd7 = new SegmentData();
    sd7.setSegment(segmentA);
    sd7.setVersion(++versionA);
    sd7.setData(map7);

    defaultSubscriber.putReceivedData(sd7, localZone);
    UserData userData7 = defaultSubscriber.peekData();

    Map<String, List<String>> expectedMap7 = new HashMap<String, List<String>>();
    addToStringMap(expectedMap7, ZHEJIANG, NINGBO, WENZHOU);
    addToStringMap(expectedMap7, JIANGSU, NANJING);
    addToStringMap(expectedMap7, FUJIAN, XIAMEN);

    assertZoneDataEquals(expectedMap7, userData7.getZoneData());

    // 8. different segments with intersection
    Map<String, List<DataBox>> map8 = new HashMap<String, List<DataBox>>();
    addToDataBoxMap(map8, ZHEJIANG, HANGZHOU);
    addToDataBoxMap(map8, JIANGSU, NANJING);
    addToDataBoxMap(map8, FUJIAN, FUZHOU);

    SegmentData sd8 = new SegmentData();
    sd8.setSegment(segmentB);
    sd8.setVersion(++versionB);
    sd8.setData(map8);

    defaultSubscriber.putReceivedData(sd8, localZone);
    UserData userData8 = defaultSubscriber.peekData();

    Map<String, List<String>> expectedMap8 = new HashMap<String, List<String>>();
    addToStringMap(expectedMap8, ZHEJIANG, HANGZHOU, NINGBO, WENZHOU);
    addToStringMap(expectedMap8, JIANGSU, NANJING);
    addToStringMap(expectedMap8, FUJIAN, FUZHOU, XIAMEN);

    assertZoneDataEquals(expectedMap8, userData8.getZoneData());

    // 9. set available segments

    List<String> availableSegments9 = new ArrayList<String>();
    availableSegments9.add(segmentB);
    defaultSubscriber.setAvailableSegments(availableSegments9);

    UserData userData9 = defaultSubscriber.peekData();

    Map<String, List<String>> expectedMap9 = new HashMap<String, List<String>>();
    addToStringMap(expectedMap9, ZHEJIANG, HANGZHOU);
    addToStringMap(expectedMap9, JIANGSU, NANJING);
    addToStringMap(expectedMap9, FUJIAN, FUZHOU);

    assertZoneDataEquals(expectedMap9, userData9.getZoneData());

    // 10. add available segments

    List<String> availableSegments10 = new ArrayList<String>();
    availableSegments10.add(segmentA);
    availableSegments10.add(segmentB);
    defaultSubscriber.setAvailableSegments(availableSegments10);

    UserData userData10 = defaultSubscriber.peekData();

    assertZoneDataEquals(expectedMap8, userData10.getZoneData());
  }

  private void addToDataBoxMap(Map<String, List<DataBox>> map, String key, String... values) {
    List<DataBox> list = map.get(key);
    if (list == null) {
      list = new ArrayList<DataBox>();
      map.put(key, list);
    }
    for (String value : values) {
      DataBox dataBox = new DataBox();
      dataBox.setData(value);
      list.add(dataBox);
    }
  }

  private void addToStringMap(Map<String, List<String>> map, String key, String... values) {
    List<String> list = map.get(key);
    if (list == null) {
      list = new ArrayList<String>();
      map.put(key, list);
    }
    for (String value : values) {
      list.add(value);
    }
  }

  private void assertZoneDataEquals(Map<String, List<String>> a, Map<String, List<String>> b) {
    assertEquals(sortAllList(a), sortAllList(b));
  }

  private Map<String, List<String>> sortAllList(Map<String, List<String>> map) {
    if (map == null) {
      return null;
    }
    Map<String, List<String>> copyMap = new HashMap<String, List<String>>();
    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
      if (entry.getValue() == null) {
        copyMap.put(entry.getKey(), null);
      } else {
        List<String> copyList = new ArrayList<String>(entry.getValue());
        Collections.sort(copyList);
        copyMap.put(entry.getKey(), copyList);
      }
    }
    return copyMap;
  }
}
