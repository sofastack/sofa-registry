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

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author xiaojian.xj
 * @version $Id: SchedulerCornUtilTest.java, v 0.1 2020年08月03日 19:58 xiaojian.xj Exp $
 */
public class SchedulerCornUtilTest {

    /**
     * execute at 05:10, 15:10, 25:10, 35:10, 45:10, 55:10
     */
    public final static String CACHE_PRINTER_CRON = "10 5,15,25,35,45,55 * * * ?";

    public static final String DATE_PATTERN       = "yyyy-MM-dd HH:mm:ss";

    SimpleDateFormat           formatter          = new SimpleDateFormat(DATE_PATTERN, Locale.CHINA);

    @Test
    public void testCorn() {
        Date trigger1 = null;
        try {
            trigger1 = SchedulerCornUtil.nextTrigger(formatter.parse("2020-08-03 20:15:00"),
                CACHE_PRINTER_CRON);
            Assert.assertEquals("calculate next trigger error.", "2020-08-03 20:15:10",
                formatter.format(trigger1));

            trigger1 = SchedulerCornUtil.nextTrigger(formatter.parse("2020-08-03 20:15:20"),
                CACHE_PRINTER_CRON);
            Assert.assertEquals("calculate next trigger error.", "2020-08-03 20:25:10",
                formatter.format(trigger1));

            trigger1 = SchedulerCornUtil.nextTrigger(formatter.parse("2020-08-03 20:30:00"),
                CACHE_PRINTER_CRON);
            Assert.assertEquals("calculate next trigger error.", "2020-08-03 20:35:10",
                formatter.format(trigger1));

            SchedulerCornUtil.nextTrigger(CACHE_PRINTER_CRON);

            long initialDelay = SchedulerCornUtil.calculateInitialDelay(CACHE_PRINTER_CRON);
            System.out.println(initialDelay / 1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}