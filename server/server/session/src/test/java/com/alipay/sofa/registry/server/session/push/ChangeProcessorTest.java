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
package com.alipay.sofa.registry.server.session.push;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import org.junit.Assert;
import org.junit.Test;

public class ChangeProcessorTest {

  private String dataCenter = "testDc";
  private String dataInfoId = "testDataInfoId";
  private int changeDebouncingMillis = 100;
  private int changeDebouncingMaxMillis = 500;

  @Test
  public void testWorker() throws Exception {
    ChangeProcessor.Worker worker =
        new ChangeProcessor.Worker(changeDebouncingMillis, changeDebouncingMaxMillis);

    Assert.assertTrue(worker.getWaitingMillis() <= 200);

    Assert.assertEquals(worker.changeDebouncingMillis, changeDebouncingMillis);
    Assert.assertEquals(worker.changeDebouncingMaxMillis, changeDebouncingMaxMillis);
    Assert.assertNull(worker.getExpire());
    worker.runUnthrowable();

    ChangeProcessor.ChangeHandler handler = mock(ChangeProcessor.ChangeHandler.class);
    ChangeProcessor.ChangeKey key = new ChangeProcessor.ChangeKey(dataCenter, dataInfoId);
    Assert.assertTrue(key.toString(), key.toString().contains(dataInfoId));

    long now1 = System.currentTimeMillis();
    TriggerPushContext ctx =
        new TriggerPushContext(dataCenter, 1000, null, System.currentTimeMillis());
    // first commit
    Assert.assertTrue(worker.commitChange(key, handler, ctx));
    long now2 = System.currentTimeMillis();
    ChangeProcessor.ChangeTask existingTask = worker.get(key);
    Assert.assertNotNull(existingTask);
    Assert.assertTrue(existingTask.toString().contains(dataInfoId));
    TestUtils.assertBetween(
        existingTask.expireTimestamp, now1 + changeDebouncingMillis, now2 + changeDebouncingMillis);
    TestUtils.assertBetween(
        existingTask.expireDeadlineTimestamp,
        now1 + changeDebouncingMaxMillis,
        now2 + changeDebouncingMaxMillis);

    Assert.assertNull(worker.getExpire());
    // expectDatumVersion is less than exist
    ctx = new TriggerPushContext(dataCenter, 900, null, System.currentTimeMillis());
    Assert.assertFalse(worker.commitChange(key, handler, ctx));
    ctx = new TriggerPushContext(dataCenter, 1100, null, System.currentTimeMillis());
    Assert.assertTrue(worker.commitChange(key, handler, ctx));
    ChangeProcessor.ChangeTask replaceTask = worker.get(key);
    // has replace
    Assert.assertTrue(replaceTask != existingTask);
    Assert.assertEquals(replaceTask.expireDeadlineTimestamp, existingTask.expireDeadlineTimestamp);
    // wait max deadline
    Thread.sleep(changeDebouncingMaxMillis + 50);
    ctx = new TriggerPushContext(dataCenter, 1200, null, System.currentTimeMillis());
    Assert.assertTrue(worker.commitChange(key, handler, ctx));
    ChangeProcessor.ChangeTask overwriteTask = worker.get(key);
    Assert.assertTrue(replaceTask == overwriteTask);
    Assert.assertEquals(overwriteTask.changeCtx.getExpectDatumVersion(), 1200);
    worker.runUnthrowable();
    verify(handler, times(1)).onChange(anyString(), anyObject());
  }

  @Test
  public void testInit() throws Exception {
    ChangeProcessor processor = new ChangeProcessor();
    SessionServerConfigBean configBean = TestUtils.newSessionConfig("testDc");
    processor.sessionServerConfig = configBean;
    configBean.setDataChangeDebouncingMillis(100);
    configBean.setDataChangeMaxDebouncingMillis(300);
    processor.init();
    Assert.assertEquals(
        processor.workers.length, processor.sessionServerConfig.getDataChangeFetchTaskWorkerSize());
    ChangeProcessor.Worker worker = processor.workers[0];
    Assert.assertEquals(
        worker.changeDebouncingMillis,
        processor.sessionServerConfig.getDataChangeDebouncingMillis());
    Assert.assertEquals(
        worker.changeDebouncingMaxMillis,
        processor.sessionServerConfig.getDataChangeMaxDebouncingMillis());

    ChangeProcessor.ChangeHandler handler = mock(ChangeProcessor.ChangeHandler.class);
    ChangeProcessor.ChangeKey key = new ChangeProcessor.ChangeKey(dataCenter, dataInfoId);
    Assert.assertNotNull(processor.workerOf(key));
    TriggerPushContext ctx =
        new TriggerPushContext(dataCenter, 100, null, System.currentTimeMillis());
    processor.fireChange(dataInfoId, handler, ctx);
    Thread.sleep(configBean.getDataChangeDebouncingMillis() + 100);
    verify(handler, times(1)).onChange(anyString(), anyObject());
  }

  @Test
  public void testChangeKey() {
    ChangeProcessor.ChangeKey key1 = new ChangeProcessor.ChangeKey(dataCenter, dataInfoId);
    ChangeProcessor.ChangeKey key2 = new ChangeProcessor.ChangeKey(dataCenter, dataInfoId);
    Assert.assertEquals(key1, key2);
    Assert.assertEquals(key1.hashCode(), key2.hashCode());

    ChangeProcessor.ChangeKey key3 = new ChangeProcessor.ChangeKey(dataCenter + "1", dataInfoId);
    Assert.assertNotEquals(key1, key3);
  }
}
