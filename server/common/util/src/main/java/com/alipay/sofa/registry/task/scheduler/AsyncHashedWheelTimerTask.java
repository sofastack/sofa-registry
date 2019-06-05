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
package com.alipay.sofa.registry.task.scheduler;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 *
 * @author shangyu.wh
 * @version 123: AsyncHashedWheelTimerTask.java, v 0.1 2019-06-03 16:11 shangyu.wh Exp $
 */
public class AsyncHashedWheelTimerTask extends HashedWheelTimer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncHashedWheelTimerTask.class);

    private final String        name;

    private final long          tickDuration;

    private final int           ticksPerWheel;

    public AsyncHashedWheelTimerTask(String name, long tickDuration, TimeUnit tickUnit,
                                     int ticksPerWheel) {
        super((new ThreadFactoryBuilder()).setNameFormat(name).build(), tickDuration, tickUnit,
            ticksPerWheel);
        this.name = name;
        this.tickDuration = tickDuration;
        this.ticksPerWheel = ticksPerWheel;
    }

    public Timeout newTimeout(String taskName, TimerTask task, long delay, TimeUnit unit,
                              BooleanSupplier checkCondition) {

        return super.newTimeout(new OngoingTimerTask(taskName, task, delay, unit, checkCondition),
            delay, unit);
    }

    public class OngoingTimerTask implements TimerTask {

        private final String          taskName;

        private final TimerTask       task;

        private final long            delay;

        private final TimeUnit        delayUnit;

        private final BooleanSupplier checkCondition;

        public OngoingTimerTask(String taskName, TimerTask task, long delay, TimeUnit unit,
                                BooleanSupplier checkCondition) {
            this.taskName = taskName;
            this.task = task;
            this.delay = delay;
            this.delayUnit = unit;
            this.checkCondition = checkCondition;
        }

        @Override
        public void run(Timeout timeout) throws Exception {

            try {
                task.run(timeout);
            } catch (Exception e) {
                LOGGER.error(taskName + "executionFailed: " + e.getMessage(), e);
            } finally {
                if (checkCondition.getAsBoolean()) {
                    AsyncHashedWheelTimerTask.this.newTimeout(taskName, task, delay, delayUnit,
                        checkCondition);
                }
            }
        }

        /**
         * Getter method for property <tt>delay</tt>.
         *
         * @return property value of delay
         */
        public long getDelay() {
            return delay;
        }

        /**
         * Getter method for property <tt>delayUnit</tt>.
         *
         * @return property value of delayUnit
         */
        public TimeUnit getDelayUnit() {
            return delayUnit;
        }
    }
}