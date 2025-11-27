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

import com.alipay.sofa.registry.common.model.TraceTimes;
import com.alipay.sofa.registry.util.StringFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author huicha
 * @date 2025/10/28
 */
public class LargeChangeAdaptiveDelayWorkerTest {

  private final int changeDebouncingMillis = 100;

  private final int changeDebouncingMaxMillis = 1000;

  private final int changeTaskWaitingMillis = 100;

  private final long baseDelay = 1000;

  private final long delayPerUnit = 10;

  private final long publisherThreshold = 1000;

  private final long maxPublisherCount = 4000;

  private LargeChangeAdaptiveDelayWorker createWorker() {
    return new LargeChangeAdaptiveDelayWorker(
        this.changeDebouncingMillis,
        this.changeDebouncingMaxMillis,
        changeTaskWaitingMillis,
        this.baseDelay,
        this.delayPerUnit,
        this.publisherThreshold,
        this.maxPublisherCount);
  }

  @Test
  public void testLessChange_getExpireTasks_emptyQueue() throws InterruptedException {
    LargeChangeAdaptiveDelayWorker largeChangeAdaptiveDelayWorker = this.createWorker();

    ChangeKey changeKey = new ChangeKey(Collections.singleton("DataCenter"), "DataInfoId");
    ChangeHandler changeHandler = Mockito.mock(ChangeHandler.class);
    TraceTimes traceTimes = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtx =
        new TriggerPushContext("DataCenter", 1, "DataNode", 0, traceTimes, 100);

    boolean result =
        largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtx);
    Assert.assertTrue(result);

    List<ChangeTaskImpl> timeoutTasks = largeChangeAdaptiveDelayWorker.getExpireTasks();
    Assert.assertNotNull(timeoutTasks);
    Assert.assertTrue(timeoutTasks.isEmpty());

    Thread.sleep(100);

    timeoutTasks = largeChangeAdaptiveDelayWorker.getExpireTasks();

    Assert.assertNotNull(timeoutTasks);
    Assert.assertEquals(1, timeoutTasks.size());

    ChangeTaskImpl timeoutTask = timeoutTasks.get(0);
    Assert.assertEquals(changeKey, timeoutTask.key());

    Map<String, Long> expectDatumVersion = timeoutTask.changeCtx.getExpectDatumVersion();
    Assert.assertNotNull(expectDatumVersion);
    Assert.assertEquals(1, expectDatumVersion.size());
    Assert.assertEquals(Long.valueOf(1), expectDatumVersion.get("DataCenter"));
  }

  @Test
  public void testLessChange_getExpireTasks_notEmptyQueue() throws InterruptedException {
    LargeChangeAdaptiveDelayWorker largeChangeAdaptiveDelayWorker = this.createWorker();

    ChangeKey changeKey = new ChangeKey(Collections.singleton("DataCenter"), "DataInfoId");
    ChangeHandler changeHandler = Mockito.mock(ChangeHandler.class);
    TraceTimes traceTimesOne = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxOne =
        new TriggerPushContext("DataCenter", 1, "DataNode", 0, traceTimesOne, 100);

    TraceTimes traceTimesTwo = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxTwo =
        new TriggerPushContext("DataCenter", 2, "DataNode", 0, traceTimesTwo, 100);

    boolean result =
        largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxOne);
    Assert.assertTrue(result);

    Thread.sleep(70);

    result = largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxTwo);
    Assert.assertTrue(result);

    Thread.sleep(30);

    List<ChangeTaskImpl> timeoutTasks = largeChangeAdaptiveDelayWorker.getExpireTasks();
    Assert.assertNotNull(timeoutTasks);
    Assert.assertTrue(timeoutTasks.isEmpty());

    Thread.sleep(100);
    timeoutTasks = largeChangeAdaptiveDelayWorker.getExpireTasks();
    Assert.assertNotNull(timeoutTasks);
    Assert.assertEquals(1, timeoutTasks.size());

    ChangeTaskImpl timeoutTask = timeoutTasks.get(0);
    Assert.assertEquals(changeKey, timeoutTask.key());

    Map<String, Long> expectDatumVersion = timeoutTask.changeCtx.getExpectDatumVersion();
    Assert.assertNotNull(expectDatumVersion);
    Assert.assertEquals(1, expectDatumVersion.size());
    Assert.assertEquals(Long.valueOf(2), expectDatumVersion.get("DataCenter"));
  }

  @Test
  public void testLessChange_getExpireTasks_notEmptyQueue_sameDeadline() {
    //    totalTaskInList:
    // ChangeTask{com.alipay.registry.chaos-9-43#@#DEFAULT_INSTANCE_ID#@#SOFA@[REGISTRY-TEST-CZ00A],ver={REGISTRY-TEST-CZ00A=1762244497039},expire=1762244497481,deadline=1762244498381},
    // ChangeTask{test.812dac49-e77e-48da-81b7-5eb1914bb1d5#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP@[REGISTRY-TEST-CZ00A],ver={REGISTRY-TEST-CZ00A=1762244497317},expire=1762244497518,deadline=1762244498418}

    //    totalTaskInMap:
    // ChangeTask{test.812dac49-e77e-48da-81b7-5eb1914bb1d5#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP@[REGISTRY-TEST-CZ00A],ver={REGISTRY-TEST-CZ00A=1762244497317},expire=1762244497518,deadline=1762244498418},
    // ChangeTask{com.alipay.registry.chaos-9-43#@#DEFAULT_INSTANCE_ID#@#SOFA@[REGISTRY-TEST-CZ00A],ver={REGISTRY-TEST-CZ00A=1762244497039},expire=1762244497481,deadline=1762244498381},
    // ChangeTask{test.db8471bd-f62f-4454-b2cf-61b5f5d70877#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP@[REGISTRY-TEST-CZ00A],ver={REGISTRY-TEST-CZ00A=1762244497320},expire=1762244497518,deadline=1762244498418}

    ChangeTaskQueue<ChangeKey, ChangeTaskImpl> changeTaskQueue = new ChangeTaskQueue<>();
    ChangeHandler changeHandler = Mockito.mock(ChangeHandler.class);

    ChangeKey keyOne =
        new ChangeKey(
            Collections.singleton("REGISTRY-TEST-CZ00A"),
            "test.812dac49-e77e-48da-81b7-5eb1914bb1d5#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP");
    TraceTimes traceTimesOne = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxOne =
        new TriggerPushContext(
            "REGISTRY-TEST-CZ00A", 1762244497317L, "data_node", 0, traceTimesOne, 100);
    ChangeTaskImpl taskOne =
        new ChangeTaskImpl(keyOne, changeCtxOne, changeHandler, 1762244497518L);
    taskOne.expireDeadlineTimestamp = 1762244498418L;

    ChangeKey keyTwo =
        new ChangeKey(
            Collections.singleton("REGISTRY-TEST-CZ00A"),
            "com.alipay.registry.chaos-9-43#@#DEFAULT_INSTANCE_ID#@#SOFA");
    TraceTimes traceTimesTwo = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxTwo =
        new TriggerPushContext(
            "REGISTRY-TEST-CZ00A", 1762244497039L, "data_node", 0, traceTimesTwo, 100);
    ChangeTaskImpl taskTwo =
        new ChangeTaskImpl(keyTwo, changeCtxTwo, changeHandler, 1762244497481L);
    taskTwo.expireDeadlineTimestamp = 1762244498381L;

    ChangeKey keyThree =
        new ChangeKey(
            Collections.singleton("REGISTRY-TEST-CZ00A"),
            "test.db8471bd-f62f-4454-b2cf-61b5f5d70877#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP");
    TraceTimes traceTimesThree = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxThree =
        new TriggerPushContext(
            "REGISTRY-TEST-CZ00A", 1762244497320L, "data_node", 0, traceTimesThree, 100);
    ChangeTaskImpl taskThree =
        new ChangeTaskImpl(keyThree, changeCtxThree, changeHandler, 1762244497518L);
    taskThree.expireDeadlineTimestamp = 1762244498418L;

    Assert.assertTrue(changeTaskQueue.pushTask(taskOne));
    Assert.assertTrue(changeTaskQueue.pushTask(taskTwo));
    Assert.assertTrue(changeTaskQueue.pushTask(taskThree));
  }

  @Test
  public void testLessChange_deadline() throws InterruptedException {
    LargeChangeAdaptiveDelayWorker largeChangeAdaptiveDelayWorker = this.createWorker();

    ChangeKey changeKey = new ChangeKey(Collections.singleton("DataCenter"), "DataInfoId");
    ChangeHandler changeHandler = Mockito.mock(ChangeHandler.class);
    TraceTimes traceTimesOne = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxOne =
        new TriggerPushContext("DataCenter", 1, "DataNode", 0, traceTimesOne, 100);

    TraceTimes traceTimesTwo = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxTwo =
        new TriggerPushContext("DataCenter", 2, "DataNode", 0, traceTimesTwo, 100);

    long firstTimeNow1 = System.currentTimeMillis();
    boolean result =
        largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxOne);
    long firstTimeNow2 = System.currentTimeMillis();
    Assert.assertTrue(result);

    ChangeTaskImpl changeTaskOne = largeChangeAdaptiveDelayWorker.findTask(changeKey);
    Assert.assertNotNull(changeTaskOne);
    this.assertBetween(
        changeTaskOne.deadline(),
        firstTimeNow1 + this.changeDebouncingMillis,
        firstTimeNow2 + this.changeDebouncingMillis);
    this.assertBetween(
        changeTaskOne.expireTimestamp,
        firstTimeNow1 + this.changeDebouncingMillis,
        firstTimeNow2 + this.changeDebouncingMillis);
    this.assertBetween(
        changeTaskOne.expireDeadlineTimestamp,
        firstTimeNow1 + this.changeDebouncingMaxMillis,
        firstTimeNow2 + this.changeDebouncingMaxMillis);

    long expireDeadlineTimestamp = changeTaskOne.expireDeadlineTimestamp;

    long secondTimeNow1 = System.currentTimeMillis();
    result = largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxTwo);
    long secondTimeNow2 = System.currentTimeMillis();
    Assert.assertTrue(result);

    ChangeTaskImpl changeTaskTwo = largeChangeAdaptiveDelayWorker.findTask(changeKey);
    Assert.assertNotNull(changeTaskTwo);
    this.assertBetween(
        changeTaskTwo.deadline(),
        secondTimeNow1 + this.changeDebouncingMillis,
        secondTimeNow2 + this.changeDebouncingMillis);
    this.assertBetween(
        changeTaskTwo.expireTimestamp,
        secondTimeNow1 + this.changeDebouncingMillis,
        secondTimeNow2 + this.changeDebouncingMillis);
    // expireDeadlineTimestamp 肯定一直保持原样的
    Assert.assertEquals(expireDeadlineTimestamp, changeTaskTwo.expireDeadlineTimestamp);

    // 模拟中间持续有推送，但是因为这个测试不会拿出超时的任务做推送，因此直接 sleep 模拟就行了
    Thread.sleep(this.changeDebouncingMaxMillis);

    // 然后再推送一次，这次预期会 merge，不调整 debouncing time
    TraceTimes traceTimesThree = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxThree =
        new TriggerPushContext("DataCenter", 3, "DataNode", 0, traceTimesThree, 100);

    result = largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxThree);
    Assert.assertTrue(result);
    ChangeTaskImpl changeTaskThree = largeChangeAdaptiveDelayWorker.findTask(changeKey);
    Assert.assertNotNull(changeTaskThree);

    // 这里预期任务会自动 merge，因此 deadline 还是上一次的 deadline
    this.assertBetween(
        changeTaskThree.deadline(),
        secondTimeNow1 + this.changeDebouncingMillis,
        secondTimeNow2 + this.changeDebouncingMillis);
    this.assertBetween(
        changeTaskThree.expireTimestamp,
        secondTimeNow1 + this.changeDebouncingMillis,
        secondTimeNow2 + this.changeDebouncingMillis);
    // expireDeadlineTimestamp 肯定一直保持原样的
    Assert.assertEquals(expireDeadlineTimestamp, changeTaskThree.expireDeadlineTimestamp);

    // 但是数据版本是最新一次推送的
    Map<String, Long> expectDatumVersion = changeTaskThree.changeCtx.getExpectDatumVersion();
    Assert.assertNotNull(expectDatumVersion);
    Assert.assertEquals(1, expectDatumVersion.size());
    Assert.assertEquals(Long.valueOf(3), expectDatumVersion.get("DataCenter"));
  }

  @Test
  public void testLargeChange_getExpireTasks_emptyQueue() throws InterruptedException {
    LargeChangeAdaptiveDelayWorker largeChangeAdaptiveDelayWorker = this.createWorker();

    ChangeKey changeKey = new ChangeKey(Collections.singleton("DataCenter"), "DataInfoId");
    ChangeHandler changeHandler = Mockito.mock(ChangeHandler.class);
    TraceTimes traceTimes = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtx =
        new TriggerPushContext("DataCenter", 1, "DataNode", 0, traceTimes, 1001);

    long now1 = System.currentTimeMillis();
    boolean result =
        largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtx);
    long now2 = System.currentTimeMillis();
    Assert.assertTrue(result);

    ChangeTaskImpl changeTask = largeChangeAdaptiveDelayWorker.findTask(changeKey);
    Assert.assertNotNull(changeTask);

    long expectDelay =
        this.computeDelay(this.baseDelay, this.delayPerUnit, this.publisherThreshold, 1001);
    this.assertBetween(changeTask.deadline(), now1 + expectDelay, now2 + expectDelay);

    List<ChangeTaskImpl> timeoutTasks = largeChangeAdaptiveDelayWorker.getExpireTasks();
    Assert.assertNotNull(timeoutTasks);
    Assert.assertTrue(timeoutTasks.isEmpty());

    Thread.sleep(expectDelay);

    timeoutTasks = largeChangeAdaptiveDelayWorker.getExpireTasks();
    Assert.assertNotNull(timeoutTasks);
    Assert.assertEquals(1, timeoutTasks.size());

    ChangeTaskImpl timeoutTask = timeoutTasks.get(0);
    Assert.assertEquals(changeKey, timeoutTask.key());

    Map<String, Long> expectDatumVersion = timeoutTask.changeCtx.getExpectDatumVersion();
    Assert.assertNotNull(expectDatumVersion);
    Assert.assertEquals(1, expectDatumVersion.size());
    Assert.assertEquals(Long.valueOf(1), expectDatumVersion.get("DataCenter"));
  }

  @Test
  public void testLargeChange_getExpireTasks_notEmptyQueue() throws InterruptedException {
    LargeChangeAdaptiveDelayWorker largeChangeAdaptiveDelayWorker = this.createWorker();

    ChangeKey changeKey = new ChangeKey(Collections.singleton("DataCenter"), "DataInfoId");
    ChangeHandler changeHandler = Mockito.mock(ChangeHandler.class);
    TraceTimes traceTimesOne = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxOne =
        new TriggerPushContext("DataCenter", 1, "DataNode", 0, traceTimesOne, 1001);

    TraceTimes traceTimesTwo = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxTwo =
        new TriggerPushContext("DataCenter", 2, "DataNode", 0, traceTimesTwo, 1001);

    boolean result =
        largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxOne);
    Assert.assertTrue(result);

    Thread.sleep(100);

    result = largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxTwo);
    Assert.assertTrue(result);

    Thread.sleep(100);

    List<ChangeTaskImpl> timeoutTasks = largeChangeAdaptiveDelayWorker.getExpireTasks();
    Assert.assertNotNull(timeoutTasks);
    Assert.assertTrue(timeoutTasks.isEmpty());

    long expectDelay =
        this.computeDelay(this.baseDelay, this.delayPerUnit, this.publisherThreshold, 1001);
    Thread.sleep(expectDelay);

    timeoutTasks = largeChangeAdaptiveDelayWorker.getExpireTasks();
    Assert.assertNotNull(timeoutTasks);
    Assert.assertEquals(1, timeoutTasks.size());

    ChangeTaskImpl timeoutTask = timeoutTasks.get(0);
    Assert.assertEquals(changeKey, timeoutTask.key());

    Map<String, Long> expectDatumVersion = timeoutTask.changeCtx.getExpectDatumVersion();
    Assert.assertNotNull(expectDatumVersion);
    Assert.assertEquals(1, expectDatumVersion.size());
    Assert.assertEquals(Long.valueOf(2), expectDatumVersion.get("DataCenter"));
  }

  @Test
  public void testLargeChange_deadline() throws InterruptedException {
    LargeChangeAdaptiveDelayWorker largeChangeAdaptiveDelayWorker = this.createWorker();

    ChangeKey changeKey = new ChangeKey(Collections.singleton("DataCenter"), "DataInfoId");
    ChangeHandler changeHandler = Mockito.mock(ChangeHandler.class);
    TraceTimes traceTimesOne = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxOne =
        new TriggerPushContext("DataCenter", 1, "DataNode", 0, traceTimesOne, 1001);

    TraceTimes traceTimesTwo = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxTwo =
        new TriggerPushContext("DataCenter", 2, "DataNode", 0, traceTimesTwo, 1001);

    long expectDelay =
        this.computeDelay(this.baseDelay, this.delayPerUnit, this.publisherThreshold, 1001);

    long now1 = System.currentTimeMillis();
    boolean result =
        largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxOne);
    long now2 = System.currentTimeMillis();
    Assert.assertTrue(result);

    ChangeTaskImpl changeTaskOne = largeChangeAdaptiveDelayWorker.findTask(changeKey);
    Assert.assertNotNull(changeTaskOne);
    this.assertBetween(changeTaskOne.deadline(), now1 + expectDelay, now2 + expectDelay);
    this.assertBetween(changeTaskOne.expireTimestamp, now1 + expectDelay, now2 + expectDelay);

    long expireTimestamp = changeTaskOne.expireTimestamp;

    result = largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxTwo);
    Assert.assertTrue(result);

    ChangeTaskImpl changeTaskTwo = largeChangeAdaptiveDelayWorker.findTask(changeKey);
    Assert.assertNotNull(changeTaskTwo);
    // 大推送任务是不调整 deadline 的
    Assert.assertEquals(expireTimestamp, changeTaskTwo.deadline());
    Assert.assertEquals(expireTimestamp, changeTaskTwo.expireTimestamp);

    // 模拟中间持续有推送，但是因为这个测试不会拿出超时的任务做推送，因此直接 sleep 模拟就行了
    Thread.sleep(expectDelay);

    // 然后再推送一次
    TraceTimes traceTimesThree = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxThree =
        new TriggerPushContext("DataCenter", 3, "DataNode", 0, traceTimesThree, 1001);

    result = largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxThree);
    Assert.assertTrue(result);
    ChangeTaskImpl changeTaskThree = largeChangeAdaptiveDelayWorker.findTask(changeKey);
    Assert.assertNotNull(changeTaskThree);

    Assert.assertEquals(expireTimestamp, changeTaskTwo.deadline());
    Assert.assertEquals(expireTimestamp, changeTaskTwo.expireTimestamp);

    Map<String, Long> expectDatumVersion = changeTaskThree.changeCtx.getExpectDatumVersion();
    Assert.assertNotNull(expectDatumVersion);
    Assert.assertEquals(1, expectDatumVersion.size());
    Assert.assertEquals(Long.valueOf(3), expectDatumVersion.get("DataCenter"));
  }

  @Test
  public void testLessFirstLargeSecond() throws InterruptedException {
    LargeChangeAdaptiveDelayWorker largeChangeAdaptiveDelayWorker = this.createWorker();

    // 第一次推送为小推送
    ChangeKey changeKey = new ChangeKey(Collections.singleton("DataCenter"), "DataInfoId");
    ChangeHandler changeHandler = Mockito.mock(ChangeHandler.class);
    TraceTimes traceTimesOne = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxOne =
        new TriggerPushContext("DataCenter", 1, "DataNode", 0, traceTimesOne, 100);

    long smallNow1 = System.currentTimeMillis();
    boolean result =
        largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxOne);
    long smallNow2 = System.currentTimeMillis();
    Assert.assertTrue(result);

    // 第二次推送为大推送
    TraceTimes traceTimesTwo = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxTwo =
        new TriggerPushContext("DataCenter", 2, "DataNode", 0, traceTimesTwo, 1001);
    long expectDelay =
        this.computeDelay(this.baseDelay, this.delayPerUnit, this.publisherThreshold, 1001);

    result = largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxTwo);
    Assert.assertTrue(result);

    ChangeTaskImpl changeTask = largeChangeAdaptiveDelayWorker.findTask(changeKey);
    Assert.assertNotNull(changeTask);

    // 首先推送时间仍然是第一次推送的时间以及 deadline
    this.assertBetween(
        changeTask.deadline(),
        smallNow1 + this.changeDebouncingMillis,
        smallNow2 + this.changeDebouncingMillis);
    this.assertBetween(
        changeTask.expireTimestamp,
        smallNow1 + this.changeDebouncingMillis,
        smallNow2 + this.changeDebouncingMillis);
    this.assertBetween(
        changeTask.expireDeadlineTimestamp,
        smallNow1 + this.changeDebouncingMaxMillis,
        smallNow2 + this.changeDebouncingMaxMillis);

    // 其次版本号应当是第二次大推送的版本号
    Map<String, Long> expectDatumVersion = changeTask.changeCtx.getExpectDatumVersion();
    Assert.assertNotNull(expectDatumVersion);
    Assert.assertEquals(1, expectDatumVersion.size());
    Assert.assertEquals(Long.valueOf(2), expectDatumVersion.get("DataCenter"));

    // 此时应当没有任何超时任务
    List<ChangeTaskImpl> timeoutTasks = largeChangeAdaptiveDelayWorker.getExpireTasks();
    Assert.assertTrue(timeoutTasks.isEmpty());

    Thread.sleep(this.changeDebouncingMillis);

    // 此时应当有超时任务了，对应大推送
    timeoutTasks = largeChangeAdaptiveDelayWorker.getExpireTasks();
    Assert.assertEquals(1, timeoutTasks.size());

    ChangeTaskImpl timeoutTask = timeoutTasks.get(0);
    Assert.assertNotNull(timeoutTask);
    this.assertBetween(
        timeoutTask.deadline(),
        smallNow1 + this.changeDebouncingMillis,
        smallNow2 + this.changeDebouncingMillis);
    this.assertBetween(
        timeoutTask.expireTimestamp,
        smallNow1 + this.changeDebouncingMillis,
        smallNow2 + this.changeDebouncingMillis);
    this.assertBetween(
        timeoutTask.expireDeadlineTimestamp,
        smallNow1 + this.changeDebouncingMaxMillis,
        smallNow2 + this.changeDebouncingMaxMillis);

    Map<String, Long> timeoutExpectDatumVersion = timeoutTask.changeCtx.getExpectDatumVersion();
    Assert.assertNotNull(timeoutExpectDatumVersion);
    Assert.assertEquals(1, timeoutExpectDatumVersion.size());
    Assert.assertEquals(Long.valueOf(2), timeoutExpectDatumVersion.get("DataCenter"));
  }

  @Test
  public void testLargeFirstSmallSecond() throws InterruptedException {
    LargeChangeAdaptiveDelayWorker largeChangeAdaptiveDelayWorker = this.createWorker();

    // 第一次推送为大推送
    ChangeKey changeKey = new ChangeKey(Collections.singleton("DataCenter"), "DataInfoId");
    ChangeHandler changeHandler = Mockito.mock(ChangeHandler.class);
    TraceTimes traceTimesOne = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxOne =
        new TriggerPushContext("DataCenter", 1, "DataNode", 0, traceTimesOne, 1001);
    long expectDelay =
        this.computeDelay(this.baseDelay, this.delayPerUnit, this.publisherThreshold, 1001);

    long largeNow1 = System.currentTimeMillis();
    boolean result =
        largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxOne);
    long largeNow2 = System.currentTimeMillis();
    Assert.assertTrue(result);

    // 第二次推送为小推送
    TraceTimes traceTimesTwo = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxTwo =
        new TriggerPushContext("DataCenter", 2, "DataNode", 0, traceTimesTwo, 100);

    long smallNow1 = System.currentTimeMillis();
    result = largeChangeAdaptiveDelayWorker.commitChange(changeKey, changeHandler, changeCtxTwo);
    long smallNow2 = System.currentTimeMillis();

    Assert.assertTrue(result);

    ChangeTaskImpl changeTask = largeChangeAdaptiveDelayWorker.findTask(changeKey);
    Assert.assertNotNull(changeTask);

    // 首先推送时间是第二次小推送的推送时间，但是 deadline 是第一次推送的 deadline
    this.assertBetween(
        changeTask.deadline(),
        smallNow1 + this.changeDebouncingMillis,
        smallNow2 + this.changeDebouncingMillis);
    this.assertBetween(
        changeTask.expireTimestamp,
        smallNow1 + this.changeDebouncingMillis,
        smallNow2 + this.changeDebouncingMillis);
    this.assertBetween(
        changeTask.expireDeadlineTimestamp, largeNow1 + expectDelay, largeNow2 + expectDelay);

    // 其次版本号应当是第二次小推送的版本号
    Map<String, Long> expectDatumVersion = changeTask.changeCtx.getExpectDatumVersion();
    Assert.assertNotNull(expectDatumVersion);
    Assert.assertEquals(1, expectDatumVersion.size());
    Assert.assertEquals(Long.valueOf(2), expectDatumVersion.get("DataCenter"));

    // 此时应当没有任何超时任务
    List<ChangeTaskImpl> timeoutTasks = largeChangeAdaptiveDelayWorker.getExpireTasks();
    Assert.assertTrue(timeoutTasks.isEmpty());

    Thread.sleep(this.changeDebouncingMillis);

    // 此时应当有超时任务了，对应小推送
    timeoutTasks = largeChangeAdaptiveDelayWorker.getExpireTasks();
    Assert.assertEquals(1, timeoutTasks.size());

    ChangeTaskImpl timeoutTask = timeoutTasks.get(0);
    Assert.assertNotNull(timeoutTask);
    this.assertBetween(
        timeoutTask.deadline(),
        smallNow1 + this.changeDebouncingMillis,
        smallNow2 + this.changeDebouncingMillis);
    this.assertBetween(
        timeoutTask.expireTimestamp,
        smallNow1 + this.changeDebouncingMillis,
        smallNow2 + this.changeDebouncingMillis);
    this.assertBetween(
        timeoutTask.expireDeadlineTimestamp, largeNow1 + expectDelay, largeNow2 + expectDelay);

    Map<String, Long> timeoutExpectDatumVersion = timeoutTask.changeCtx.getExpectDatumVersion();
    Assert.assertNotNull(timeoutExpectDatumVersion);
    Assert.assertEquals(1, timeoutExpectDatumVersion.size());
    Assert.assertEquals(Long.valueOf(2), timeoutExpectDatumVersion.get("DataCenter"));
  }

  private void assertBetween(long v, long low, long high) {
    Assert.assertTrue(StringFormatter.format("v={}, low={}", v, low), v >= low);
    Assert.assertTrue(StringFormatter.format("v={}, high={}", v, high), v <= high);
  }

  private long computeDelay(
      long baseDelay, long delayPerUnit, long publisherThreshold, long publisherCount) {
    return baseDelay + (delayPerUnit * (publisherCount - publisherThreshold));
  }

  @Test
  public void testTimeoutOrder() {
    LargeChangeAdaptiveDelayWorker largeChangeAdaptiveDelayWorker = this.createWorker();

    ChangeKey changeKeyOne = new ChangeKey(Collections.singleton("DataCenter"), "DataInfoIdOne");
    ChangeHandler changeHandler = Mockito.mock(ChangeHandler.class);
    TraceTimes traceTimesOne = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxOne =
        new TriggerPushContext("DataCenter", 1, "DataNode", 0, traceTimesOne, 1001);

    largeChangeAdaptiveDelayWorker.commitChange(changeKeyOne, changeHandler, changeCtxOne);

    ChangeKey changeKeyTwo = new ChangeKey(Collections.singleton("DataCenter"), "DataInfoIdTwo");
    TraceTimes traceTimesTwo = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxTwo =
        new TriggerPushContext("DataCenter", 2, "DataNode", 0, traceTimesTwo, 1003);

    largeChangeAdaptiveDelayWorker.commitChange(changeKeyTwo, changeHandler, changeCtxTwo);

    ChangeKey changeKeyThree =
        new ChangeKey(Collections.singleton("DataCenter"), "DataInfoIdThree");
    TraceTimes traceTimesThree = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxThree =
        new TriggerPushContext("DataCenter", 3, "DataNode", 0, traceTimesThree, 1002);

    largeChangeAdaptiveDelayWorker.commitChange(changeKeyThree, changeHandler, changeCtxThree);

    ChangeKey changeKeyFour = new ChangeKey(Collections.singleton("DataCenter"), "DataInfoIdFour");
    TraceTimes traceTimesFour = Mockito.mock(TraceTimes.class);
    TriggerPushContext changeCtxFour =
        new TriggerPushContext("DataCenter", 4, "DataNode", 0, traceTimesFour, 2000);

    largeChangeAdaptiveDelayWorker.commitChange(changeKeyFour, changeHandler, changeCtxFour);

    ChangeTaskQueue<ChangeKey, ChangeTaskImpl> taskQueue =
        largeChangeAdaptiveDelayWorker.getTaskQueue();
    List<ChangeTaskImpl> tasks = new ArrayList<>();
    taskQueue.visitTasks(tasks::add);

    // 检查结果，必须是按照 deadline 从小到大排序的，由于大推送的 deadline 是随着 publisher
    // 数量线性增长的，因此这里预期是按照 publisher count 升序排序的
    Assert.assertEquals(4, tasks.size());
    Assert.assertEquals(changeKeyOne, tasks.get(0).key());
    Assert.assertEquals(changeKeyThree, tasks.get(1).key());
    Assert.assertEquals(changeKeyTwo, tasks.get(2).key());
    Assert.assertEquals(changeKeyFour, tasks.get(3).key());
  }
}
