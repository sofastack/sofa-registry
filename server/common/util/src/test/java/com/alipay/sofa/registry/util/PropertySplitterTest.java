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
package com.alipay.sofa.registry.util;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xuanbei
 * @since 2018/12/28
 */
public class PropertySplitterTest {
  private PropertySplitter propertySplitter = new PropertySplitter();

  @Test
  public void testMap() {
    Assert.assertEquals(0, propertySplitter.map(null).size());
    Assert.assertEquals(0, propertySplitter.map("").size());
    Assert.assertEquals(2, propertySplitter.map("KEY1:VALUE1,KEY2:VALUE2").size());
    Assert.assertEquals("VALUE1", propertySplitter.map("KEY1:VALUE1,KEY2:VALUE2").get("KEY1"));
    Assert.assertEquals("VALUE2", propertySplitter.map("KEY1:VALUE1,KEY2:VALUE2").get("KEY2"));
  }

  @Test
  public void testMapOfList() {
    Assert.assertEquals(0, propertySplitter.mapOfList(null).size());
    Assert.assertEquals(0, propertySplitter.mapOfList("|").size());
    Assert.assertEquals(
        2, propertySplitter.mapOfList("KEY1:VALUE1.1,VALUE1.2|KEY2:VALUE2.1,VALUE2.2").size());
    Assert.assertTrue(
        propertySplitter.map("KEY1:VALUE1,KEY2:VALUE2").get("KEY1").contains("VALUE1"));
    Assert.assertTrue(
        propertySplitter.map("KEY1:VALUE1,KEY2:VALUE2").get("KEY2").contains("VALUE2"));
  }

  @Test
  public void testList() {
    Assert.assertEquals(0, propertySplitter.list(null).size());
    Assert.assertEquals(0, propertySplitter.list("").size());
    Assert.assertEquals(4, propertySplitter.list("VALUE1,VALUE2,VALUE3,VALUE4").size());
    Assert.assertTrue(propertySplitter.list("VALUE1,VALUE2,VALUE3,VALUE4").contains("VALUE1"));
    Assert.assertTrue(propertySplitter.list("VALUE1,VALUE2,VALUE3,VALUE4").contains("VALUE2"));
    Assert.assertTrue(propertySplitter.list("VALUE1,VALUE2,VALUE3,VALUE4").contains("VALUE3"));
    Assert.assertTrue(propertySplitter.list("VALUE1,VALUE2,VALUE3,VALUE4").contains("VALUE4"));
  }

  @Test
  public void testGroupedList() {
    Assert.assertEquals(0, propertySplitter.groupedList(null).size());
    Assert.assertEquals(0, propertySplitter.groupedList("").size());
    Assert.assertEquals(
        2, propertySplitter.groupedList("VALUE1.1,VALUE1.2|VALUE2.1,VALUE2.2").size());
    Assert.assertTrue(
        ((List)
                (((List) propertySplitter.groupedList("VALUE1.1,VALUE1.2|VALUE2.1,VALUE2.2"))
                    .get(0)))
            .contains("VALUE1.1"));
    Assert.assertTrue(
        ((List)
                (((List) propertySplitter.groupedList("VALUE1.1,VALUE1.2|VALUE2.1,VALUE2.2"))
                    .get(0)))
            .contains("VALUE1.1"));
    Assert.assertTrue(
        ((List)
                (((List) propertySplitter.groupedList("VALUE1.1,VALUE1.2|VALUE2.1,VALUE2.2"))
                    .get(1)))
            .contains("VALUE2.1"));
    Assert.assertTrue(
        ((List)
                (((List) propertySplitter.groupedList("VALUE1.1,VALUE1.2|VALUE2.1,VALUE2.2"))
                    .get(1)))
            .contains("VALUE2.2"));
  }

  @Test
  public void testMapOfKeyList() {
    Assert.assertEquals(propertySplitter.mapOfKeyList("aaa", null).size(), 0);
    Assert.assertEquals(
        propertySplitter.mapOfList("K1:V1,V2"), propertySplitter.mapOfKeyList("K1", "V1,V2"));
    Assert.assertEquals(
        propertySplitter.mapOfList("K1:V1,V2"), propertySplitter.mapOfKeyList("K1", "K2:V1,V2"));
    Assert.assertEquals(
        propertySplitter.mapOfList("K1:V1,V2|K3:V3,V4"),
        propertySplitter.mapOfKeyList("K2", "K1:V1,V2,|K3:V3,V4"));
  }
}
