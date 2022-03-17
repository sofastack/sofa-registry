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
package com.alipay.sofa.registry.common.model.metaserver;

import com.alipay.sofa.registry.util.ConcurrentUtils;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version : NodeServerOperateInfoTest.java, v 0.1 2022年02月15日 10:52 xiaojian.xj Exp $
 */
public class NodeServerOperateInfoTest {

  private static final String CELL = "TEST_CELL";

  @Test
  public void test() {
    NodeServerOperateInfo info = new NodeServerOperateInfo();

    // meta
    Assert.assertEquals(0, info.metasSize());
    Assert.assertEquals(0, info.metaLastOperateTs());
    Assert.assertTrue(info.addMetas(CELL, "1.1.1.1"));
    Assert.assertFalse(info.addMetas(CELL, "1.1.1.1"));
    info.addMetas(CELL, "1.1.1.2");
    long timeMillis = System.currentTimeMillis();
    ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
    Assert.assertTrue(info.addMetas(CELL, "1.1.1.3"));
    Assert.assertTrue(info.removeMetas(CELL, "1.1.1.2"));
    Assert.assertFalse(info.removeMetas(CELL, "1.1.1.2"));
    Assert.assertEquals(2, info.metasSize());
    Assert.assertTrue(info.metaLastOperateTs() > timeMillis);

    // data
    Assert.assertEquals(0, info.datasSize());
    Assert.assertEquals(0, info.dataLastOperateTs());
    Assert.assertTrue(info.addDatas(CELL, "1.1.1.1"));
    Assert.assertFalse(info.addDatas(CELL, "1.1.1.1"));
    Assert.assertTrue(info.addDatas(CELL, "1.1.1.2"));
    timeMillis = System.currentTimeMillis();
    ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
    Assert.assertTrue(info.addDatas(CELL, "1.1.1.3"));
    Assert.assertTrue(info.removeDatas(CELL, "1.1.1.2"));
    Assert.assertFalse(info.removeDatas(CELL, "1.1.1.2"));
    Assert.assertEquals(2, info.datasSize());
    Assert.assertTrue(info.dataLastOperateTs() > timeMillis);

    // session
    Assert.assertEquals(0, info.sessionSize());
    Assert.assertEquals(0, info.sessionLastOperateTs());
    Assert.assertTrue(info.addSessions(CELL, "1.1.1.1"));
    Assert.assertFalse(info.addSessions(CELL, "1.1.1.1"));
    Assert.assertTrue(info.addSessions(CELL, "1.1.1.2"));
    timeMillis = System.currentTimeMillis();
    ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.MILLISECONDS);
    Assert.assertTrue(info.addSessions(CELL, "1.1.1.3"));
    Assert.assertTrue(info.removeSessions(CELL, "1.1.1.2"));
    Assert.assertFalse(info.removeSessions(CELL, "1.1.1.2"));
    Assert.assertEquals(2, info.sessionSize());
    Assert.assertTrue(info.sessionLastOperateTs() > timeMillis);
  }
}
