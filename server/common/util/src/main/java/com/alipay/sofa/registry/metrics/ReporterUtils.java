/*
 * Copyright Notice: This software is developed by Ant Small and Micro Financial Services Group Co., Ltd. This software and
 *  all the relevant information, including but not limited to any signs, images, photographs, animations, text,
 *  interface design, audios and videos, and printed materials, are protected by copyright laws and other intellectual
 *  property laws and treaties.
 *
 * The use of this software shall abide by the laws and regulations as well as Software Installation License
 * Agreement/Software Use Agreement updated from time to time. Without authorization from Ant Small and Micro Financial
 *  Services Group Co., Ltd., no one may conduct the following actions:
 *
 *   1) reproduce, spread, present, set up a mirror of, upload, download this software;
 *
 *   2) reverse engineer, decompile the source code of this software or try to find the source code in any other ways;
 *
 *   3) modify, translate and adapt this software, or develop derivative products, works, and services based on this
 *    software;
 *
 *   4) distribute, lease, rent, sub-license, demise or transfer any rights in relation to this software, or authorize
 *    the reproduction of this software on other’s computers.
 */
package com.alipay.sofa.registry.metrics;

import java.util.concurrent.TimeUnit;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

/**
 *
 * @author shangyu.wh
 * @version $Id: ReporterUtils.java, v 0.1 2018-08-23 13:25 shangyu.wh Exp $
 */
public class ReporterUtils {

    private static final Logger METRIC_LOGGER = LoggerFactory.getLogger("REGISTRY-METRICS");

    /**
     * start slf4j reporter
     * @param period
     * @param registry
     * @param loggerMetrics
     */
    public static void startSlf4jReporter(long period, MetricRegistry registry, Logger loggerMetrics) {
        Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
                .outputTo((org.slf4j.Logger) loggerMetrics.getLogger())
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        if (period > 0) {
            reporter.start(period, TimeUnit.SECONDS);
        } else {
            reporter.start(30, TimeUnit.SECONDS);
        }

    }

    /**
     * start slf4j reporter
     * @param period
     * @param registry
     */
    public static void startSlf4jReporter(long period, MetricRegistry registry) {
        startSlf4jReporter(period, registry, METRIC_LOGGER);
    }
}