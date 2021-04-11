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

import com.alipay.sofa.registry.util.BatchCallableRunnable.InvokeFuture;
import com.alipay.sofa.registry.util.BatchCallableRunnable.TaskEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;

/**
 * @author xiaojian.xj
 * @version $Id: BatchCallableRunnableTest.java, v 0.1 2021年01月23日 16:11 xiaojian.xj Exp $
 */
public class BatchCallableRunnableTest {

  @org.junit.Test
  public void testBatchRunnable() throws InterruptedException {
    Test test = new Test();
    test.init();
    Map<Integer, InvokeFuture> futures = new HashMap<>();
    for (int i = 0; i <= 255; i++) {
      TaskEvent taskEvent = test.new TaskEvent(i);
      InvokeFuture future = test.commit(taskEvent);
      futures.putIfAbsent(i, future);
    }

    for (Entry<Integer, InvokeFuture> entry : futures.entrySet()) {

      InvokeFuture future = entry.getValue();
      String result = (String) future.getResponse();
      Assert.assertEquals("hello " + entry.getKey().intValue(), result);
    }
  }

  @org.junit.Test
  public void testBatchRunnable_False() throws InterruptedException {
    Test test = new Test();
    test.retFalse = true;
    test.init();
    InvokeFuture future = test.commit(test.new TaskEvent(10));
    Assert.assertFalse(future.isSuccess());
  }

  @org.junit.Test
  public void testBatchRunnable_Exception() throws InterruptedException {
    Test test = new Test();
    test.exception = true;
    test.init();
    InvokeFuture future = test.commit(test.new TaskEvent(10));
    Assert.assertFalse(future.isSuccess());
    Assert.assertTrue("@@@" + future.getMessage(), future.getMessage() != null);
  }

  @org.junit.Test
  public void testFuture() throws Exception {
    InvokeFuture future = new InvokeFuture();
    future.fail();
    Assert.assertFalse(future.isSuccess());

    future = new InvokeFuture();
    future.error("xxx");
    Assert.assertFalse(future.isSuccess());
    Assert.assertEquals(future.getMessage(), "xxx");

    future = new InvokeFuture();
    future.finish();
    Assert.assertFalse(future.isSuccess());

    future = new InvokeFuture();
    Object obj = new Object();
    future.putResponse(obj);
    future.finish();
    Assert.assertTrue(future.isSuccess());
    Assert.assertEquals(future.getResponse(), obj);
  }

  public class Test extends BatchCallableRunnable<Integer, String> {
    boolean exception;
    boolean retFalse;

    public Test() {
      super(1, TimeUnit.SECONDS, 100);
    }

    @Override
    public boolean batchProcess(List<TaskEvent> taskEvents) {
      if (taskEvents == null || taskEvents.size() == 0) {
        return true;
      }

      if (retFalse) {
        return false;
      }

      if (exception) {
        throw new RuntimeException();
      }

      for (TaskEvent taskEvent : taskEvents) {
        Integer data = taskEvent.getData();
        taskEvent.getFuture().putResponse("hello " + data);
      }
      return true;
    }
  }
}
