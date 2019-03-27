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
package com.alipay.sofa.registry.timer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.util.Timer;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author xuanbei
 * @since 2019/3/27
 */
public class AsyncHashedWheelTimerTest {
    private long      currentTime;
    private long      executeTime;
    private Throwable executionRejectedThrowable;
    private Throwable executionFailedThrowable;

    @Test
    public void doTest() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setDaemon(true);
        final Timer timer = new AsyncHashedWheelTimer(threadFactoryBuilder.setNameFormat(
                "AsyncHashedWheelTimerTest").build(), 50, TimeUnit.MILLISECONDS, 10,
                threadFactoryBuilder.setNameFormat("Registry-DataNodeServiceImpl-WheelExecutor-%d")
                        .build(), new AsyncHashedWheelTimer.TaskFailedCallback() {
            @Override
            public void executionRejected(Throwable t) {
                executionRejectedThrowable = t;
            }

            @Override
            public void executionFailed(Throwable t) {
                executionFailedThrowable = t;
            }
        });

        currentTime = System.currentTimeMillis();
        executeTime = currentTime;
        timer.newTimeout((timeout)-> {
            executeTime = System.currentTimeMillis();
            countDownLatch.countDown();
            throw new Exception("execution failed.");
        },1000, TimeUnit.MILLISECONDS);

        countDownLatch.await();
        Assert.assertTrue(executeTime >= currentTime + 1000);
        Assert.assertNull(executionRejectedThrowable);
        Assert.assertNotNull(executionFailedThrowable);
    }
}
