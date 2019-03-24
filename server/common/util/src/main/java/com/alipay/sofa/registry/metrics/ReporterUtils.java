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
package com.alipay.sofa.registry.metrics;

import com.alipay.sofa.registry.log.Logger;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author shangyu.wh
 * @version $Id: ReporterUtils.java, v 0.1 2018-08-23 13:25 shangyu.wh Exp $
 */
public class ReporterUtils {

    /**
     * start slf4j reporter
     * @param period
     * @param registry
     * @param loggerMetrics
     */
    public static void startSlf4jReporter(long period, MetricRegistry registry, Logger loggerMetrics) {
        Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
            .outputTo((org.slf4j.Logger) loggerMetrics.getLogger())
            .convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
        if (period > 0) {
            reporter.start(period, TimeUnit.SECONDS);
        } else {
            reporter.start(30, TimeUnit.SECONDS);
        }

    }
}