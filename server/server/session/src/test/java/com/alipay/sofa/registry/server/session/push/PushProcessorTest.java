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

import static com.alipay.sofa.registry.server.session.push.PushMetrics.Push.*;

import com.alipay.remoting.rpc.exception.InvokeTimeoutException;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class PushProcessorTest {
  private String zone = "testZone";
  private String dataId = "testDataId";

  @Test
  public void testFire() throws Exception {
    PushProcessor processor = new PushProcessor();
    processor.sessionServerConfig = TestUtils.newSessionConfig("testDc");
    Assert.assertTrue(processor.watchDog.getWaitingMillis() < 200);

    Assert.assertEquals(processor.watchCommit().size(), 0);
    TriggerPushContext ctx =
        new TriggerPushContext("testDc", 100, null, System.currentTimeMillis());
    PushCause pushCause = new PushCause(ctx, PushType.Reg, System.currentTimeMillis());
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    SubDatum datum = TestUtils.newSubDatum(subscriber.getDataId(), 100, Collections.emptyList());
    Assert.assertTrue(processor.pendingTasks.isEmpty());

    long now1 = System.currentTimeMillis();
    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        datum);
    long now2 = System.currentTimeMillis();

    Assert.assertEquals(processor.pendingTasks.size(), 1);
    PushProcessor.PendingTaskKey taskKey = processor.pendingTasks.keySet().iterator().next();
    Assert.assertTrue(taskKey.toString(), taskKey.toString().contains(dataId));

    PushProcessor.PushTask task = processor.pendingTasks.values().iterator().next();
    TestUtils.assertBetween(
        task.expireTimestamp,
        now1 + processor.sessionServerConfig.getPushDataTaskDebouncingMillis(),
        now2 + processor.sessionServerConfig.getPushDataTaskDebouncingMillis());

    Assert.assertEquals(PENDING_SKIP_COUNTER.get(), 0, 0);
    // make sure the expire ts is diff
    Thread.sleep(1);
    // fire again, skip
    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        datum);
    Assert.assertEquals(PENDING_SKIP_COUNTER.get(), 1, 0);
    Assert.assertEquals(processor.pendingTasks.size(), 1, 0);

    // fire after
    Assert.assertEquals(PENDING_REPLACE_COUNTER.get(), 0, 0);
    datum = TestUtils.newSubDatum(subscriber.getDataId(), 200, Collections.emptyList());
    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        datum);
    Assert.assertEquals(PENDING_REPLACE_COUNTER.get(), 1, 0);
    Assert.assertEquals(processor.pendingTasks.size(), 1, 0);
    PushProcessor.PushTask replaceTask = processor.pendingTasks.get(taskKey);
    Assert.assertNotEquals(replaceTask, task);
    // replace.task expire set to prev.expire
    Assert.assertEquals(replaceTask.expireTimestamp, task.expireTimestamp);
    Assert.assertTrue(replaceTask.toString(), replaceTask.toString().contains(dataId));

    // now there is one pending task with delay
    processor.sessionServerConfig.setStopPushSwitch(true);
    Assert.assertEquals(processor.watchCommit().size(), 0);

    processor.sessionServerConfig.setStopPushSwitch(false);
    // task has clean
    Assert.assertEquals(processor.watchCommit().size(), 0);
    // first suspend, avoid run watchdog
    processor.watchDog.suspend();
    // pushExecutor init
    processor.init();
    // push again
    // Reg.noDelay=false
    processor.firePush(
        new PushCause(ctx, PushType.Empty, System.currentTimeMillis()),
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        datum);

    // noDelay=false
    processor.firePush(
        new PushCause(ctx, PushType.Sub, System.currentTimeMillis()),
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId() + "-test", subscriber),
        datum);
    Assert.assertEquals(processor.pendingTasks.size(), 2);
    // only one, sub is not expire
    List<PushProcessor.PushTask> commits = processor.watchCommit();
    Assert.assertEquals(1, commits.size());
    Assert.assertEquals(processor.watchCommit().size(), 0);
    Assert.assertEquals(processor.pendingTasks.size(), 1);

    task = processor.pendingTasks.values().iterator().next();
    Assert.assertEquals(task.pushCause.pushType, PushType.Sub);
    // make task expire
    task.expireTimestamp = System.currentTimeMillis();

    commits = processor.watchCommit();
    Assert.assertEquals(commits.size(), 1);
    Assert.assertEquals(processor.watchCommit().size(), 0);
    Assert.assertEquals(processor.pendingTasks.size(), 0);

    processor.watchDog.resume();
  }

  @Test
  public void testPush() throws Exception {
    PushProcessor processor = new PushProcessor();
    SessionServerConfigBean config = TestUtils.newSessionConfig("testDc");
    processor.sessionServerConfig = config;
    processor.pushDataGenerator = new PushDataGenerator();
    processor.pushDataGenerator.sessionServerConfig = config;
    TriggerPushContext ctx =
        new TriggerPushContext("testDc", 100, null, System.currentTimeMillis());
    PushCause pushCause = new PushCause(ctx, PushType.Reg, System.currentTimeMillis());
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    SubDatum datum = TestUtils.newSubDatum(subscriber.getDataId(), 100, Collections.emptyList());

    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        datum);

    PushProcessor.PushTask task = processor.pendingTasks.values().iterator().next();
    processor.pendingTasks.clear();

    processor.sessionServerConfig.setStopPushSwitch(true);
    Assert.assertFalse(processor.doPush(task));

    processor.sessionServerConfig.setStopPushSwitch(false);
    // clientNodeService is null
    Assert.assertFalse(processor.doPush(task));
    Assert.assertEquals(processor.pushingTasks.size(), 0);

    processor.clientNodeService = Mockito.mock(ClientNodeService.class);
    // push success
    Assert.assertTrue(processor.doPush(task));

    Mockito.verify(processor.clientNodeService, Mockito.times(1))
        .pushWithCallback(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
    Assert.assertEquals(processor.pushingTasks.size(), 1);

    PushProcessor.PushTask pushing = processor.pushingTasks.values().iterator().next();
    Assert.assertEquals(pushing, task);
    PushProcessor.PushingTaskKey pushingKey = processor.pushingTasks.keySet().iterator().next();
    Assert.assertTrue(pushingKey.toString(), pushingKey.toString().contains(dataId));
    PushProcessor.PushingTaskKey otherPushingKey =
        new PushProcessor.PushingTaskKey(
            pushingKey.dataInfoId,
            pushingKey.addr,
            pushingKey.scopeEnum,
            BaseInfo.ClientVersion.MProtocolpackage);

    Assert.assertNotEquals(pushingKey, otherPushingKey);

    // make sure check pushing success
    config.setClientNodeExchangeTimeoutMillis(1000 * 30);
    // no retry
    config.setPushTaskRetryTimes(0);
    Assert.assertFalse(processor.doPush(task));
    Assert.assertEquals(processor.pendingTasks.size(), 0);
    Assert.assertEquals(task.retryCount, 1);

    // support retry
    config.setPushTaskRetryTimes(1);
    task.retryCount = 0;
    long expire = task.expireTimestamp;
    long now1 = System.currentTimeMillis();
    Assert.assertFalse(processor.doPush(task));
    // has add pending
    Assert.assertEquals(processor.pendingTasks.size(), 1);
    TestUtils.assertBetween(
        task.expireTimestamp,
        now1 + processor.getRetryBackoffTime(1),
        System.currentTimeMillis() + processor.getRetryBackoffTime(1));

    // expire has update
    Assert.assertNotEquals(task.expireTimestamp, expire);
    Assert.assertEquals(processor.pushingTasks.size(), 1);
    // push too long, trigger force push
    config.setClientNodeExchangeTimeoutMillis(0);
    Thread.sleep(1);
    Assert.assertTrue(processor.checkPushing(task));
    Assert.assertEquals(processor.pushingTasks.size(), 0);

    Assert.assertTrue(processor.doPush(task));
    Assert.assertEquals(processor.pushingTasks.size(), 1);
    // task has clean
    processor.handleDoPushException(task, new RequestChannelClosedException("test"));
    Assert.assertEquals(processor.pushingTasks.size(), 0);

    Assert.assertTrue(processor.doPush(task));
    Assert.assertEquals(processor.pushingTasks.size(), 1);
    // task has clean
    processor.handleDoPushException(task, new Throwable("test"));
    Assert.assertEquals(processor.pushingTasks.size(), 0);
  }

  @Test
  public void testGetRetry() {
    PushProcessor processor = new PushProcessor();
    SessionServerConfigBean config = TestUtils.newSessionConfig("testDc");
    processor.sessionServerConfig = config;
    Assert.assertEquals(
        processor.getRetryBackoffTime(0), config.getPushDataTaskRetryFirstDelayMillis());
    Assert.assertEquals(
        processor.getRetryBackoffTime(1), config.getPushDataTaskRetryFirstDelayMillis());
    Assert.assertEquals(
        processor.getRetryBackoffTime(2),
        config.getPushDataTaskRetryFirstDelayMillis()
            + config.getPushDataTaskRetryIncrementDelayMillis());
  }

  @Test
  public void testRun() {
    PushProcessor processor = new PushProcessor();
    PushProcessor.DiscardRunHandler discardRunHandler = processor.new DiscardRunHandler();
    Thread t = Thread.currentThread();
    final AtomicReference<Thread> runT = new AtomicReference<>();
    discardRunHandler.rejectedExecution(
        new Runnable() {
          @Override
          public void run() {
            runT.set(Thread.currentThread());
          }
        },
        (ThreadPoolExecutor) Executors.newCachedThreadPool());
    Assert.assertNull(runT.get());
  }

  @Test
  public void testOnCallback() throws Exception {
    PushProcessor processor = newProcessor();
    TriggerPushContext ctx =
        new TriggerPushContext("testDc", 100, null, System.currentTimeMillis());
    PushCause pushCause = new PushCause(ctx, PushType.Reg, System.currentTimeMillis());
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    SubDatum datum = TestUtils.newSubDatum(subscriber.getDataId(), 100, Collections.emptyList());

    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        datum);

    PushProcessor.PushTask task = processor.pendingTasks.values().iterator().next();
    processor.doPush(task);
    Thread.sleep(1);
    Assert.assertEquals(processor.pushingTasks.size(), 1);
    PushProcessor.PushClientCallback callback = processor.new PushClientCallback(task);
    Assert.assertNotNull(callback.getExecutor());
    Assert.assertEquals(0, subscriber.getPushVersion(datum.getDataCenter()));
    Assert.assertEquals(0, callback.finishedTimestamp);
    callback.onCallback(null, null);
    // pushing task cleaned
    Assert.assertEquals(processor.pushingTasks.size(), 0);
    Assert.assertNotEquals(0, callback.finishedTimestamp);
    Assert.assertEquals(100, subscriber.getPushVersion(datum.getDataCenter()));
    Assert.assertTrue(String.valueOf(callback.pushSpanMillis()), callback.pushSpanMillis() > 0);
    Assert.assertTrue(String.valueOf(callback.totalSpanMillis()), callback.totalSpanMillis() > 0);
  }

  private PushProcessor newProcessor() {
    PushProcessor processor = new PushProcessor();
    SessionServerConfigBean config = TestUtils.newSessionConfig("testDc");
    processor.sessionServerConfig = config;
    processor.clientNodeService = Mockito.mock(ClientNodeService.class);
    processor.pushDataGenerator = new PushDataGenerator();
    processor.pushDataGenerator.sessionServerConfig = config;
    return processor;
  }

  @Test
  public void testOnException() throws Exception {
    PushProcessor processor = newProcessor();
    TriggerPushContext ctx =
        new TriggerPushContext("testDc", 100, null, System.currentTimeMillis());
    PushCause pushCause = new PushCause(ctx, PushType.Reg, System.currentTimeMillis());
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    SubDatum datum = TestUtils.newSubDatum(subscriber.getDataId(), 100, Collections.emptyList());

    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        datum);

    PushProcessor.PushTask task = processor.pendingTasks.values().iterator().next();
    processor.doPush(task);
    Thread.sleep(1);
    Assert.assertEquals(processor.pushingTasks.size(), 1);
    PushProcessor.PushClientCallback callback = processor.new PushClientCallback(task);
    Assert.assertNotNull(callback.getExecutor());
    Assert.assertEquals(0, subscriber.getPushVersion(datum.getDataCenter()));
    Assert.assertEquals(0, callback.finishedTimestamp);

    callback.onException(null, new InvokeTimeoutException());
    Assert.assertEquals(PUSH_CLIENT_FAIL_COUNTER.get(), 1, 0);
    Assert.assertEquals(processor.pushingTasks.size(), 0);
    Assert.assertNotEquals(0, callback.finishedTimestamp);
    Assert.assertEquals(0, subscriber.getPushVersion(datum.getDataCenter()));
    Assert.assertTrue(callback.pushSpanMillis() > 0);
    Assert.assertTrue(callback.totalSpanMillis() > 0);

    callback.onException(null, new Exception());
    Assert.assertEquals(PUSH_CLIENT_FAIL_COUNTER.get(), 2, 0);
    Assert.assertEquals(processor.pushingTasks.size(), 0);
    Assert.assertEquals(0, subscriber.getPushVersion(datum.getDataCenter()));
  }

  @Test
  public void testClean() {
    PushProcessor processor = newProcessor();
    Assert.assertEquals(0, processor.cleanPushingTaskRunTooLong());
    TriggerPushContext ctx =
        new TriggerPushContext("testDc", 100, null, System.currentTimeMillis());
    PushCause pushCause = new PushCause(ctx, PushType.Reg, System.currentTimeMillis());
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    SubDatum datum = TestUtils.newSubDatum(subscriber.getDataId(), 100, Collections.emptyList());

    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        datum);
    PushProcessor.PushTask task = processor.pendingTasks.values().iterator().next();
    processor.doPush(task);
    // no run too long
    Assert.assertEquals(0, processor.cleanPushingTaskRunTooLong());
    // make expire
    ((SessionServerConfigBean) processor.sessionServerConfig).setClientNodeExchangeTimeoutMillis(0);
    Assert.assertEquals(1, processor.cleanPushingTaskRunTooLong());
  }
}
