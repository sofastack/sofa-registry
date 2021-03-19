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

  public class Test extends BatchCallableRunnable<Integer, String> {

    public Test() {
      init();
    }

    @Override
    public boolean batchProcess(List<TaskEvent> taskEvents) {
      if (taskEvents == null || taskEvents.size() == 0) {
        return true;
      }

      for (TaskEvent taskEvent : taskEvents) {
        Integer data = taskEvent.getData();
        taskEvent.getFuture().putResponse("hello " + data);
      }
      return true;
    }

    @Override
    protected void setSleep() {
      this.sleep = 1;
    }

    @Override
    protected void setTimeUnit() {
      this.timeUnit = TimeUnit.SECONDS;
    }

    @Override
    protected void setBatchSize() {
      this.batchSize = 100;
    }
  }
}
