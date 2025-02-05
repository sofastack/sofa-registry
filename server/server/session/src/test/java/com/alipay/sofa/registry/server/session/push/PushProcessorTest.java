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

import static com.alipay.sofa.registry.server.session.push.PushMetrics.Push.BUFFER_REPLACE_COUNTER;
import static com.alipay.sofa.registry.server.session.push.PushMetrics.Push.BUFFER_SKIP_COUNTER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.alipay.remoting.rpc.exception.InvokeTimeoutException;
import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.MultiSubDatum;
import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.ChannelOverflowException;
import com.alipay.sofa.registry.remoting.exchange.RequestChannelClosedException;
import com.alipay.sofa.registry.server.session.AbstractSessionServerTestBase.InMemoryCircuitBreakerService;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.circuit.breaker.CircuitBreakerService;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import com.alipay.sofa.registry.task.RejectedDiscardHandler;
import com.alipay.sofa.registry.util.BackOffTimes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

public class PushProcessorTest {
  private String zone = "testZone";
  private String dataId = "testDataId";

  private String dataCenter = "testDc";
  private long version = -1L;

  @BeforeEach
  public void beforeEach() {}

  @Test
  public void testFire() throws Exception {
    final double skip = BUFFER_SKIP_COUNTER.get();
    final double replace = BUFFER_REPLACE_COUNTER.get();
    PushProcessor processor = newProcessor();
    final PushTaskBuffer.BufferWorker worker = processor.taskBuffer.workers[0];

    Assert.assertEquals(processor.taskBuffer.watchBuffer(worker), 0);
    TriggerPushContext ctx =
        new TriggerPushContext(dataCenter, 100, null, System.currentTimeMillis());
    PushCause pushCause =
        new PushCause(
            ctx, PushType.Sub, Collections.singletonMap(dataCenter, System.currentTimeMillis()));
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    SubDatum datum =
        TestUtils.newSubDatum(dataCenter, subscriber.getDataId(), 100, Collections.emptyList());
    Assert.assertTrue(worker.bufferMap.isEmpty());

    long now1 = System.currentTimeMillis();
    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        MultiSubDatum.of(datum));
    long now2 = System.currentTimeMillis();

    Assert.assertEquals(worker.bufferMap.size(), 1);
    PushTaskBuffer.BufferTaskKey taskKey = worker.bufferMap.keySet().iterator().next();
    Assert.assertTrue(taskKey.toString(), taskKey.toString().contains(dataId));

    PushTask task = worker.bufferMap.values().iterator().next();
    TestUtils.assertBetween(
        task.expireTimestamp,
        now1 + processor.sessionServerConfig.getPushDataTaskDebouncingMillis(),
        now2 + processor.sessionServerConfig.getPushDataTaskDebouncingMillis());

    Assert.assertEquals(BUFFER_SKIP_COUNTER.get(), skip, 0);
    // make sure the expire ts is diff
    Thread.sleep(1);
    // fire again, skip
    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        MultiSubDatum.of(datum));
    Assert.assertEquals(BUFFER_SKIP_COUNTER.get(), skip + 1, 0);
    Assert.assertEquals(worker.bufferMap.size(), 1, 0);

    // fire after
    Assert.assertEquals(BUFFER_REPLACE_COUNTER.get(), replace, 0);
    datum = TestUtils.newSubDatum(dataCenter, subscriber.getDataId(), 200, Collections.emptyList());
    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        MultiSubDatum.of(datum));
    Assert.assertEquals(BUFFER_REPLACE_COUNTER.get(), replace + 1, 0);
    Assert.assertEquals(worker.bufferMap.size(), 1, 0);
    PushTask replaceTask = worker.bufferMap.get(taskKey);
    Assert.assertNotEquals(replaceTask, task);
    // replace.task expire set to prev.expire
    Assert.assertEquals(replaceTask.expireTimestamp, task.expireTimestamp);
    Assert.assertTrue(replaceTask.toString(), replaceTask.toString().contains(dataId));

    // now there is one pending task with delay
    processor
        .pushSwitchService
        .getFetchStopPushService()
        .setStopPushSwitch(System.currentTimeMillis(), true);
    Assert.assertEquals(processor.taskBuffer.watchBuffer(worker), 0);

    processor
        .pushSwitchService
        .getFetchStopPushService()
        .setStopPushSwitch(System.currentTimeMillis(), false);
    // task clean
    worker.bufferMap.clear();
    // first suspend, avoid run watchdog
    processor.taskBuffer.suspend();
    // pushExecutor init
    processor.init();
    // push again
    // Reg.noDelay=false
    processor.firePush(
        new PushCause(
            ctx, PushType.Empty, Collections.singletonMap(dataCenter, System.currentTimeMillis())),
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        MultiSubDatum.of(datum));

    // noDelay=false
    processor.firePush(
        new PushCause(
            ctx, PushType.Sub, Collections.singletonMap(dataCenter, System.currentTimeMillis())),
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId() + "-test", subscriber),
        MultiSubDatum.of(datum));
    Assert.assertEquals(worker.bufferMap.size(), 2);
    // only one, sub is not expire
    Assert.assertEquals(1, processor.taskBuffer.watchBuffer(worker));
    Assert.assertEquals(processor.taskBuffer.watchBuffer(worker), 0);
    Assert.assertEquals(worker.bufferMap.size(), 1);

    task = worker.bufferMap.values().iterator().next();
    Assert.assertEquals(task.trace.pushCause.pushType, PushType.Sub);
    // make task expire
    task.expireTimestamp = System.currentTimeMillis();

    Assert.assertEquals(processor.taskBuffer.watchBuffer(worker), 1);
    Assert.assertEquals(processor.taskBuffer.watchBuffer(worker), 0);
    Assert.assertEquals(worker.bufferMap.size(), 0);

    processor.taskBuffer.resume();
  }

  @Test
  public void testReg() {
    PushProcessor processor = newProcessor();
    final PushTaskBuffer.BufferWorker worker = processor.taskBuffer.workers[0];

    TriggerPushContext ctx =
        new TriggerPushContext("testDc", 100, null, System.currentTimeMillis());
    PushCause pushCause =
        new PushCause(
            ctx, PushType.Reg, Collections.singletonMap(dataCenter, System.currentTimeMillis()));
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    SubDatum datum =
        TestUtils.newSubDatum(dataCenter, subscriber.getDataId(), 100, Collections.emptyList());

    PushTask task1 =
        processor
            .createPushTask(
                pushCause,
                NetUtil.getLocalSocketAddress(),
                Collections.singletonMap(subscriber.getRegisterId(), subscriber),
                MultiSubDatum.of(datum))
            .get(0);

    Assert.assertTrue(processor.taskBuffer.buffer(task1));

    Subscriber subscriber2 = TestUtils.newZoneSubscriber(dataId, zone);
    subscriber2.setVersion(subscriber.getVersion() - 1);
    subscriber2.setRegisterId(subscriber.getRegisterId());

    PushTask task2 =
        processor
            .createPushTask(
                pushCause,
                NetUtil.getLocalSocketAddress(),
                Collections.singletonMap(subscriber.getRegisterId(), subscriber2),
                MultiSubDatum.of(datum))
            .get(0);

    Assert.assertFalse(processor.taskBuffer.buffer(task2));

    subscriber2.setVersion(subscriber.getVersion() + 10);
    task2 =
        processor
            .createPushTask(
                pushCause,
                NetUtil.getLocalSocketAddress(),
                Collections.singletonMap(subscriber.getRegisterId(), subscriber2),
                MultiSubDatum.of(datum))
            .get(0);
    Assert.assertTrue(processor.taskBuffer.buffer(task2));
  }

  @Test
  public void testPush() throws Exception {
    PushProcessor processor = newProcessor();
    final PushTaskBuffer.BufferWorker worker = processor.taskBuffer.workers[0];

    TriggerPushContext ctx =
        new TriggerPushContext("testDc", 100, null, System.currentTimeMillis());
    PushCause pushCause =
        new PushCause(
            ctx, PushType.Reg, Collections.singletonMap(dataCenter, System.currentTimeMillis()));
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    SubDatum datum =
        TestUtils.newSubDatum(dataCenter, subscriber.getDataId(), 100, Collections.emptyList());

    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        MultiSubDatum.of(datum));

    PushTask task = worker.bufferMap.values().iterator().next();
    worker.bufferMap.clear();

    processor
        .pushSwitchService
        .getFetchStopPushService()
        .setStopPushSwitch(System.currentTimeMillis(), true);
    Assert.assertFalse(processor.doPush(task));

    processor
        .pushSwitchService
        .getFetchStopPushService()
        .setStopPushSwitch(System.currentTimeMillis(), false);
    // clientNodeService is null
    processor.clientNodeService = null;
    Assert.assertFalse(processor.doPush(task));
    Assert.assertEquals(processor.pushingRecords.size(), 0);

    processor.clientNodeService = mock(ClientNodeService.class);
    // push success
    Assert.assertTrue(processor.doPush(task));

    Mockito.verify(processor.clientNodeService, Mockito.times(1))
        .pushWithCallback(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject());
    Assert.assertEquals(processor.pushingRecords.size(), 1);

    PushTask.PushingTaskKey pushingKey = processor.pushingRecords.keySet().iterator().next();
    Assert.assertTrue(pushingKey.toString(), pushingKey.toString().contains(dataId));
    PushTask.PushingTaskKey otherPushingKey =
        new PushTask.PushingTaskKey(
            pushingKey.dataInfoId,
            pushingKey.addr,
            pushingKey.scopeEnum,
            BaseInfo.ClientVersion.MProtocolpackage);

    Assert.assertNotEquals(pushingKey, otherPushingKey);
    SessionServerConfigBean config = (SessionServerConfigBean) processor.sessionServerConfig;
    // make sure check pushing success
    config.setClientNodeExchangeTimeoutMillis(1000 * 30);
    // no retry
    config.setPushTaskRetryTimes(0);
    Assert.assertFalse(processor.doPush(task));
    Assert.assertEquals(worker.bufferMap.size(), 0);
    Assert.assertEquals(task.retryCount, 1);

    // support retry
    config.setPushTaskRetryTimes(1);
    task.retryCount = 0;
    long expire = task.expireTimestamp;
    long now1 = System.currentTimeMillis();
    Assert.assertFalse(processor.doPush(task));
    // has add pending
    Assert.assertEquals(worker.bufferMap.size(), 1);
    TestUtils.assertBetween(
        task.expireTimestamp,
        now1 + config.getPushDataTaskRetryFirstDelayMillis(),
        System.currentTimeMillis()
            + processor.getRetryBackoffTime(1)
            + BackOffTimes.maxBackOffRandoms());

    // expire has update
    Assert.assertNotEquals(task.expireTimestamp, expire);
    Assert.assertEquals(processor.pushingRecords.size(), 1);
    // push too long, trigger force push
    config.setClientNodeExchangeTimeoutMillis(0);
    Thread.sleep(1);
    Assert.assertTrue(processor.checkPushRunning(task));
    Assert.assertEquals(processor.pushingRecords.size(), 0);

    Assert.assertTrue(processor.doPush(task));
    Assert.assertEquals(processor.pushingRecords.size(), 1);
    // task has clean
    processor.handleDoPushException(task, new RequestChannelClosedException("test"));
    Assert.assertEquals(processor.pushingRecords.size(), 0);

    Assert.assertTrue(processor.doPush(task));
    Assert.assertEquals(processor.pushingRecords.size(), 1);
    // task has clean
    processor.handleDoPushException(task, new Throwable("test"));
    Assert.assertEquals(processor.pushingRecords.size(), 0);

    Assert.assertTrue(processor.doPush(task));
    Assert.assertEquals(processor.pushingRecords.size(), 1);
    // task has clean
    processor.handleDoPushException(
        task, new ChannelOverflowException("test", new RuntimeException()));
    Assert.assertEquals(processor.pushingRecords.size(), 0);
  }

  @Test
  public void testGetRetry() {
    PushProcessor processor = new PushProcessor();
    SessionServerConfigBean config = TestUtils.newSessionConfig("testDc");
    processor.sessionServerConfig = config;
    Assert.assertTrue(
        processor.getRetryBackoffTime(0) > config.getPushDataTaskRetryFirstDelayMillis());
    Assert.assertTrue(
        processor.getRetryBackoffTime(0)
            < config.getPushDataTaskRetryFirstDelayMillis()
                + config.getPushDataTaskRetryIncrementDelayMillis());

    Assert.assertTrue(
        processor.getRetryBackoffTime(1) > config.getPushDataTaskRetryFirstDelayMillis());
    Assert.assertTrue(
        processor.getRetryBackoffTime(1)
            < config.getPushDataTaskRetryFirstDelayMillis()
                + config.getPushDataTaskRetryIncrementDelayMillis());

    Assert.assertTrue(
        processor.getRetryBackoffTime(2)
            > config.getPushDataTaskRetryFirstDelayMillis()
                + config.getPushDataTaskRetryIncrementDelayMillis());
    Assert.assertTrue(
        processor.getRetryBackoffTime(2)
            < config.getPushDataTaskRetryFirstDelayMillis()
                + config.getPushDataTaskRetryIncrementDelayMillis() * 2);
  }

  @Test
  public void testRun() {
    RejectedDiscardHandler discardRunHandler = new RejectedDiscardHandler();
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
    final PushTaskBuffer.BufferWorker worker = processor.taskBuffer.workers[0];

    TriggerPushContext ctx =
        new TriggerPushContext("testDc", 100, null, System.currentTimeMillis());
    PushCause pushCause =
        new PushCause(
            ctx, PushType.Reg, Collections.singletonMap(dataCenter, System.currentTimeMillis()));
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    SubDatum datum =
        TestUtils.newSubDatum(dataCenter, subscriber.getDataId(), 100, Collections.emptyList());

    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        MultiSubDatum.of(datum));

    PushTask task = worker.bufferMap.values().iterator().next();
    processor.doPush(task);
    Thread.sleep(1);
    Assert.assertEquals(processor.pushingRecords.size(), 1);
    PushProcessor.PushClientCallback callback = processor.new PushClientCallback(task);
    Assert.assertNotNull(callback.getExecutor());
    Assert.assertEquals(0, subscriber.getPushedVersion(datum.getDataCenter()));
    callback.onCallback(null, null);
    // pushing task cleaned
    Assert.assertEquals(processor.pushingRecords.size(), 0);
    Assert.assertEquals(100, subscriber.getPushedVersion(datum.getDataCenter()));
  }

  private PushProcessor newProcessor() {
    PushProcessor processor = new PushProcessor();
    SessionServerConfigBean config = TestUtils.newSessionConfig(dataCenter);
    config.setPushTaskBufferBucketSize(1);
    processor.sessionServerConfig = config;
    processor.clientNodeService = mock(ClientNodeService.class);
    processor.pushSwitchService = TestUtils.newPushSwitchService(config);

    processor
        .pushSwitchService
        .getFetchStopPushService()
        .setStopPushSwitch(System.currentTimeMillis(), false);
    CircuitBreakerService circuitBreakerService = spy(InMemoryCircuitBreakerService.class);
    processor.circuitBreakerService = circuitBreakerService;
    processor.pushDataGenerator = new PushDataGenerator();
    processor.pushDataGenerator.sessionServerConfig = config;
    processor.intTaskBuffer();
    processor.taskBuffer.suspend();
    Assert.assertEquals(1, processor.taskBuffer.workers.length);
    return processor;
  }

  @Test
  public void testOnException() throws Exception {
    PushProcessor processor = newProcessor();
    final PushTaskBuffer.BufferWorker worker = processor.taskBuffer.workers[0];
    TriggerPushContext ctx =
        new TriggerPushContext("testDc", 100, null, System.currentTimeMillis());
    PushCause pushCause =
        new PushCause(
            ctx, PushType.Reg, Collections.singletonMap(dataCenter, System.currentTimeMillis()));
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    SubDatum datum =
        TestUtils.newSubDatum(dataCenter, subscriber.getDataId(), 100, Collections.emptyList());

    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        MultiSubDatum.of(datum));

    PushTask task = worker.bufferMap.values().iterator().next();
    processor.doPush(task);
    Thread.sleep(1);
    Assert.assertEquals(processor.pushingRecords.size(), 1);
    PushProcessor.PushClientCallback callback = processor.new PushClientCallback(task);
    Assert.assertNotNull(callback.getExecutor());
    Assert.assertEquals(0, subscriber.getPushedVersion(datum.getDataCenter()));
    TestUtils.MockBlotChannel channel = TestUtils.newChannel(9600, "192.168.1.1", 1234);
    callback.onException(channel, new InvokeTimeoutException());
    Assert.assertEquals(processor.pushingRecords.size(), 0);
    Assert.assertEquals(0, subscriber.getPushedVersion(datum.getDataCenter()));

    callback.onException(channel, new Exception());
    Assert.assertEquals(processor.pushingRecords.size(), 0);
    Assert.assertEquals(0, subscriber.getPushedVersion(datum.getDataCenter()));
  }

  @Test
  public void testClean() {
    PushProcessor processor = newProcessor();
    final PushTaskBuffer.BufferWorker worker = processor.taskBuffer.workers[0];

    Assert.assertEquals(0, processor.cleanPushingTaskRunTooLong());
    TriggerPushContext ctx =
        new TriggerPushContext(dataCenter, 100, null, System.currentTimeMillis());
    PushCause pushCause =
        new PushCause(
            ctx, PushType.Reg, Collections.singletonMap(dataCenter, System.currentTimeMillis()));
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    SubDatum datum =
        TestUtils.newSubDatum(dataCenter, subscriber.getDataId(), 100, Collections.emptyList());

    processor.firePush(
        pushCause,
        NetUtil.getLocalSocketAddress(),
        Collections.singletonMap(subscriber.getRegisterId(), subscriber),
        MultiSubDatum.of(datum));
    PushTask task = worker.bufferMap.values().iterator().next();
    processor.doPush(task);
    // no run too long
    Assert.assertEquals(0, processor.cleanPushingTaskRunTooLong());
    // make expire
    ((SessionServerConfigBean) processor.sessionServerConfig).setClientNodeExchangeTimeoutMillis(0);
    Assert.assertEquals(1, processor.cleanPushingTaskRunTooLong());
  }

  @Test
  public void testInterestOfDatum() throws Exception {
    PushProcessor processor = new PushProcessor();
    Subscriber subscriber = TestUtils.newZoneSubscriber(dataId, zone);
    SubDatum datum =
        TestUtils.newSubDatum(dataCenter, subscriber.getDataId(), 100, Collections.emptyList());
    PushTask task =
        processor
        .new PushTaskImpl(
            null,
            null,
            Collections.singletonMap(subscriber.getRegisterId(), subscriber),
            MultiSubDatum.of(datum));

    Assert.assertTrue(processor.interestOfDatum(task));
    subscriber.checkAndUpdateCtx(
        Collections.singletonMap(datum.getDataCenter(), 90L),
        Collections.singletonMap(datum.getDataCenter(), 100));
    Assert.assertTrue(processor.interestOfDatum(task));
    subscriber.checkAndUpdateCtx(
        Collections.singletonMap(datum.getDataCenter(), 100L),
        Collections.singletonMap(datum.getDataCenter(), 100));
    Assert.assertFalse(processor.interestOfDatum(task));
    subscriber.checkAndUpdateCtx(
        Collections.singletonMap(datum.getDataCenter(), 110L),
        Collections.singletonMap(datum.getDataCenter(), 100));
    Assert.assertFalse(processor.interestOfDatum(task));
  }

  @Test
  public void testSetPushDelay() {
    PushProcessor processor = newProcessor();

    PushEfficiencyImproveConfig pushEfficiencyImproveConfig = new PushEfficiencyImproveConfig();
    pushEfficiencyImproveConfig.setPushTaskWaitingMillis(10);
    Set<String> zoneSet = new HashSet<>();
    zoneSet.add("ALL_ZONE");
    pushEfficiencyImproveConfig.setZoneSet(zoneSet);
    processor.setPushTaskDelayTime(pushEfficiencyImproveConfig);

    Assert.assertTrue(processor.taskBuffer.workers[0].getWaitingMillis() == 10);
  }
}
