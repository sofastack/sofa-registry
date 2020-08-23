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

import org.springframework.scheduling.support.CronSequenceGenerator;

import java.util.Date;

/**
 *
 * @author xiaojian.xj
 * @version $Id: SchedulerCornUtil.java, v 0.1 2020年08月03日 17:39 xiaojian.xj Exp $
 */
public class SchedulerCornUtil {

    public static long calculateInitialDelay(String corn) {
        Date current = new Date();
        Date nextTrigger = nextTrigger(current, corn);
        return nextTrigger.getTime() - current.getTime();
    }

    /**
     * calculate next trigger time
     * @param corn
     * @return
     */
    public static Date nextTrigger(String corn) {
        CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(corn);
        Date nextTriggerTime = cronSequenceGenerator.next(new Date());

        return nextTriggerTime;
    }

    public static Date nextTrigger(Date date, String corn) {
        CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(corn);
        Date nextTriggerTime = cronSequenceGenerator.next(date);

        return nextTriggerTime;
    }
}