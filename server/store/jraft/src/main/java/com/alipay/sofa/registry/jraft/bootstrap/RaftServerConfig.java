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
package com.alipay.sofa.registry.jraft.bootstrap;

import com.alipay.sofa.registry.log.Logger;

/**
 *
 * @author shangyu.wh
 * @version $Id: RaftServerConifg.java, v 0.1 2018-08-23 13:13 shangyu.wh Exp $
 */
public class RaftServerConfig {

    /**
     * Whether to enable metrics for node.
     */
    private boolean enableMetrics               = false;

    /**
     * Whether to enable metrics interval seconds
     */
    private int     enableMetricsReporterPeriod = 30;

    /**
     * A follower would become a candidate if it doesn't receive any message
     * from the leader in |election_timeout_ms| milliseconds
     * Default: 1000 (1s)
     */
    private int     electionTimeoutMs           = 1000;

    /**
     * A snapshot saving would be triggered every |snapshot_interval_s| seconds
     * if this was reset as a positive number
     * If |snapshot_interval_s| <= 0, the time based snapshot would be disabled.
     * Default: 3600 (1 hour)
     */
    private int     snapshotIntervalSecs        = 3600;

    private Logger  metricsLogger;

    /**
     * Getter method for property <tt>enableMetrics</tt>.
     *
     * @return property value of enableMetrics
     */
    public boolean isEnableMetrics() {
        return enableMetrics;
    }

    /**
     * Setter method for property <tt>enableMetrics</tt>.
     *
     * @param enableMetrics  value to be assigned to property enableMetrics
     */
    public void setEnableMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
    }

    /**
     * Getter method for property <tt>enableMetricsReporterPeriod</tt>.
     *
     * @return property value of enableMetricsReporterPeriod
     */
    public int getEnableMetricsReporterPeriod() {
        return enableMetricsReporterPeriod;
    }

    /**
     * Setter method for property <tt>enableMetricsReporterPeriod</tt>.
     *
     * @param enableMetricsReporterPeriod  value to be assigned to property enableMetricsReporterPeriod
     */
    public void setEnableMetricsReporterPeriod(int enableMetricsReporterPeriod) {
        this.enableMetricsReporterPeriod = enableMetricsReporterPeriod;
    }

    /**
     * Getter method for property <tt>electionTimeoutMs</tt>.
     *
     * @return property value of electionTimeoutMs
     */
    public int getElectionTimeoutMs() {
        return electionTimeoutMs;
    }

    /**
     * Setter method for property <tt>electionTimeoutMs</tt>.
     *
     * @param electionTimeoutMs  value to be assigned to property electionTimeoutMs
     */
    public void setElectionTimeoutMs(int electionTimeoutMs) {
        this.electionTimeoutMs = electionTimeoutMs;
    }

    /**
     * Getter method for property <tt>snapshotIntervalSecs</tt>.
     *
     * @return property value of snapshotIntervalSecs
     */
    public int getSnapshotIntervalSecs() {
        return snapshotIntervalSecs;
    }

    /**
     * Setter method for property <tt>snapshotIntervalSecs</tt>.
     *
     * @param snapshotIntervalSecs  value to be assigned to property snapshotIntervalSecs
     */
    public void setSnapshotIntervalSecs(int snapshotIntervalSecs) {
        this.snapshotIntervalSecs = snapshotIntervalSecs;
    }

    /**
     * Getter method for property <tt>metricsLogger</tt>.
     *
     * @return property value of metricsLogger
     */
    public Logger getMetricsLogger() {
        return metricsLogger;
    }

    /**
     * Setter method for property <tt>metricsLogger</tt>.
     *
     * @param metricsLogger  value to be assigned to property metricsLogger
     */
    public void setMetricsLogger(Logger metricsLogger) {
        this.metricsLogger = metricsLogger;
    }
}