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
package com.alipay.sofa.registry.server.meta.bootstrap.config;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.util.OsUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

/**
 * @author shangyu.wh
 * @version $Id: MetaServerConfigBean.java, v 0.1 2018-01-16 11:01 shangyu.wh Exp $
 */
@ConfigurationProperties(prefix = MetaServerConfigBean.PREFIX)
public class MetaServerConfigBean implements MetaServerConfig {

    public static final String PREFIX                                          = "meta.server";

    private int                sessionServerPort                               = 9610;

    private int                dataServerPort                                  = 9611;

    private int                metaServerPort                                  = 9612;

    private int                httpServerPort                                  = 9615;

    private int                schedulerCheckNodeListChangePushTimeout         = 3;

    private int                schedulerCheckNodeListChangePushFirstDelay      = 1;

    private int                schedulerCheckNodeListChangePushExpBackOffBound = 10;

    private int                dataNodeExchangeTimeout                         = 3000;

    private int                sessionNodeExchangeTimeout                      = 3000;

    private int                metaNodeExchangeTimeout                         = 3000;

    private int                raftElectionTimeout                             = 1000;

    /**
     * Whether to enable metrics for node.
     */
    private boolean            enableMetrics                                   = true;

    private String             raftDataPath                                    = System
                                                                                   .getProperty("user.home")
                                                                                 + File.separator
                                                                                 + "raftData";

    private int                rockDBCacheSize                                 = 64;                   //64M

    private int                connectMetaServerExecutorMinSize                = 3;
    private int                connectMetaServerExecutorMaxSize                = 10;
    private int                connectMetaServerExecutorQueueSize              = 1024;

    private int                raftClientRefreshExecutorMinSize                = 3;
    private int                raftClientRefreshExecutorMaxSize                = 10;
    private int                raftClientRefreshExecutorQueueSize              = 1024;

    private int                defaultRequestExecutorMinSize                   = 20;
    private int                defaultRequestExecutorMaxSize                   = 600;
    private int                defaultRequestExecutorQueueSize                 = 500;

    private int                appRevisionRegisterExecutorMinSize              = OsUtils
                                                                                   .getCpuCount() * 5;
    private int                appRevisionRegisterExecutorMaxSize              = OsUtils
                                                                                   .getCpuCount() * 20;
    private int                appRevisionRegisterExecutorQueueSize            = 500;

    private int                raftExecutorMinSize                             = 20;
    private int                raftExecutorMaxSize                             = 400;
    private int                raftExecutorQueueSize                           = 100;

    private int                raftServerExecutorMinSize                       = 20;
    private int                raftServerExecutorMaxSize                       = 100;
    private int                raftServerExecutorQueueSize                     = 100;

    private int                raftFsmExecutorMinSize                          = 3;
    private int                raftFsmExecutorMaxSize                          = 10;
    private int                raftFsmExecutorQueueSize                        = 100;

    private int                metaSchedulerPoolSize                           = 6;
    private double             sessionLoadbalanceThresholdRatio                = 1.1;

    private int                expireCheckIntervalMilli                        = 1000;

    /**
     * Gets get session server port.
     *
     * @return the get session server port
     */
    @Override
    public int getSessionServerPort() {
        return sessionServerPort;
    }

    /**
     * Setter method for property <tt>sessionServerPort</tt>.
     *
     * @param sessionServerPort value to be assigned to property sessionServerPort
     */
    public void setSessionServerPort(int sessionServerPort) {
        this.sessionServerPort = sessionServerPort;
    }

    /**
     * Gets get data server port.
     *
     * @return the get data server port
     */
    @Override
    public int getDataServerPort() {
        return dataServerPort;
    }

    /**
     * Setter method for property <tt>dataServerPort</tt>.
     *
     * @param dataServerPort value to be assigned to property dataServerPort
     */
    public void setDataServerPort(int dataServerPort) {
        this.dataServerPort = dataServerPort;
    }

    /**
     * Gets get http server port.
     *
     * @return the get http server port
     */
    @Override
    public int getHttpServerPort() {
        return httpServerPort;
    }

    /**
     * Setter method for property <tt>httpServerPort</tt>.
     *
     * @param httpServerPort value to be assigned to property httpServerPort
     */
    public void setHttpServerPort(int httpServerPort) {
        this.httpServerPort = httpServerPort;
    }

    /**
     * Getter method for property <tt>schedulerHeartbeatExpBackOffBound</tt>.
     *
     * @return property value of schedulerHeartbeatExpBackOffBound
     */
    @Override
    public int getExpireCheckIntervalMilli() {
        return expireCheckIntervalMilli;
    }

    /**
     * Sets set expire check interval milli.
     *
     * @param expireCheckIntervalMilli the expire check interval milli
     * @return the set expire check interval milli
     */
    public MetaServerConfigBean setExpireCheckIntervalMilli(int expireCheckIntervalMilli) {
        this.expireCheckIntervalMilli = expireCheckIntervalMilli;
        return this;
    }

    /**
     * Getter method for property <tt>dataNodeExchangeTimeout</tt>.
     *
     * @return property value of dataNodeExchangeTimeout
     */
    @Override
    public int getDataNodeExchangeTimeout() {
        return dataNodeExchangeTimeout;
    }

    /**
     * Setter method for property <tt>dataNodeExchangeTimeout</tt>.
     *
     * @param dataNodeExchangeTimeout value to be assigned to property dataNodeExchangeTimeout
     */
    public void setDataNodeExchangeTimeout(int dataNodeExchangeTimeout) {
        this.dataNodeExchangeTimeout = dataNodeExchangeTimeout;
    }

    /**
     * Getter method for property <tt>sessionNodeExchangeTimeout</tt>.
     *
     * @return property value of sessionNodeExchangeTimeout
     */
    @Override
    public int getSessionNodeExchangeTimeout() {
        return sessionNodeExchangeTimeout;
    }

    /**
     * Setter method for property <tt>sessionNodeExchangeTimeout</tt>.
     *
     * @param sessionNodeExchangeTimeout value to be assigned to property sessionNodeExchangeTimeout
     */
    public void setSessionNodeExchangeTimeout(int sessionNodeExchangeTimeout) {
        this.sessionNodeExchangeTimeout = sessionNodeExchangeTimeout;
    }

    /**
     * Getter method for property <tt>metaServerPort</tt>.
     *
     * @return property value of metaServerPort
     */
    @Override
    public int getMetaServerPort() {
        return metaServerPort;
    }

    /**
     * Setter method for property <tt>metaServerPort</tt>.
     *
     * @param metaServerPort value to be assigned to property metaServerPort
     */
    public void setMetaServerPort(int metaServerPort) {
        this.metaServerPort = metaServerPort;
    }

    /**
     * Getter method for property <tt>metaNodeExchangeTimeout</tt>.
     *
     * @return property value of metaNodeExchangeTimeout
     */
    @Override
    public int getMetaNodeExchangeTimeout() {
        return metaNodeExchangeTimeout;
    }

    /**
     * Setter method for property <tt>metaNodeExchangeTimeout</tt>.
     *
     * @param metaNodeExchangeTimeout value to be assigned to property metaNodeExchangeTimeout
     */
    public void setMetaNodeExchangeTimeout(int metaNodeExchangeTimeout) {
        this.metaNodeExchangeTimeout = metaNodeExchangeTimeout;
    }

    /**
     * Getter method for property <tt>schedulerCheckNodeListChangePushTimeout</tt>.
     *
     * @return property value of schedulerCheckNodeListChangePushTimeout
     */
    @Override
    public int getSchedulerCheckNodeListChangePushTimeout() {
        return schedulerCheckNodeListChangePushTimeout;
    }

    /**
     * Setter method for property <tt>schedulerCheckNodeListChangePushTimeout</tt>.
     *
     * @param schedulerCheckNodeListChangePushTimeout value to be assigned to property schedulerCheckNodeListChangePushTimeout
     */
    public void setSchedulerCheckNodeListChangePushTimeout(int schedulerCheckNodeListChangePushTimeout) {
        this.schedulerCheckNodeListChangePushTimeout = schedulerCheckNodeListChangePushTimeout;
    }

    /**
     * Getter method for property <tt>schedulerCheckNodeListChangePushFirstDelay</tt>.
     *
     * @return property value of schedulerCheckNodeListChangePushFirstDelay
     */
    @Override
    public int getSchedulerCheckNodeListChangePushFirstDelay() {
        return schedulerCheckNodeListChangePushFirstDelay;
    }

    /**
     * Setter method for property <tt>schedulerCheckNodeListChangePushFirstDelay</tt>.
     *
     * @param schedulerCheckNodeListChangePushFirstDelay value to be assigned to property schedulerCheckNodeListChangePushFirstDelay
     */
    public void setSchedulerCheckNodeListChangePushFirstDelay(int schedulerCheckNodeListChangePushFirstDelay) {
        this.schedulerCheckNodeListChangePushFirstDelay = schedulerCheckNodeListChangePushFirstDelay;
    }

    /**
     * Getter method for property <tt>schedulerCheckNodeListChangePushExpBackOffBound</tt>.
     *
     * @return property value of schedulerCheckNodeListChangePushExpBackOffBound
     */
    @Override
    public int getSchedulerCheckNodeListChangePushExpBackOffBound() {
        return schedulerCheckNodeListChangePushExpBackOffBound;
    }

    /**
     * Setter method for property <tt>schedulerCheckNodeListChangePushExpBackOffBound</tt>.
     *
     * @param schedulerCheckNodeListChangePushExpBackOffBound value to be assigned to property
     *                                                        schedulerCheckNodeListChangePushExpBackOffBound
     */
    public void setSchedulerCheckNodeListChangePushExpBackOffBound(int schedulerCheckNodeListChangePushExpBackOffBound) {
        this.schedulerCheckNodeListChangePushExpBackOffBound = schedulerCheckNodeListChangePushExpBackOffBound;
    }

    /**
     * Getter method for property <tt>raftGroup</tt>.
     *
     * @return property value of raftGroup
     */
    @Override
    public String getRaftGroup() {
        return ValueConstants.RAFT_SERVER_GROUP;
    }

    /**
     * Getter method for property <tt>raftServerPort</tt>.
     *
     * @return property value of raftServerPort
     */
    @Override
    public int getRaftServerPort() {
        return ValueConstants.RAFT_SERVER_PORT;
    }

    /**
     * Gets get cross dc meta sync interval milli.
     *
     * @return the get cross dc meta sync interval milli
     */
    @Override
    public int getCrossDcMetaSyncIntervalMilli() {
        return ValueConstants.CROSS_DC_META_SYNC_INTERVAL_MILLI;
    }

    /**
     * Getter method for property <tt>raftDataPath</tt>.
     *
     * @return property value of raftDataPath
     */
    @Override
    public String getRaftDataPath() {
        return raftDataPath;
    }

    /**
     * Setter method for property <tt>raftDataPath</tt>.
     *
     * @param raftDataPath value to be assigned to property raftDataPath
     */
    public void setRaftDataPath(String raftDataPath) {
        this.raftDataPath = raftDataPath;
    }

    /**
     * Is enable metrics boolean.
     *
     * @return the boolean
     */
    @Override
    public boolean isEnableMetrics() {
        return enableMetrics;
    }

    /**
     * Setter method for property <tt>enableMetrics</tt>.
     *
     * @param enableMetrics value to be assigned to property enableMetrics
     */
    public void setEnableMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
    }

    /**
     * Getter method for property <tt>RockDBCacheSize</tt>.
     *
     * @return property value of RockDBCacheSize
     */
    public int getRockDBCacheSize() {
        return rockDBCacheSize;
    }

    /**
     * Setter method for property <tt>RockDBCacheSize</tt>.
     *
     * @param rockDBCacheSize value to be assigned to property RockDBCacheSize
     */
    public void setRockDBCacheSize(int rockDBCacheSize) {
        this.rockDBCacheSize = rockDBCacheSize;
    }

    /**
     * Getter method for property <tt>connectMetaServerExecutorMinSize</tt>.
     *
     * @return property value of connectMetaServerExecutorMinSize
     */
    @Override
    public int getConnectMetaServerExecutorMinSize() {
        return connectMetaServerExecutorMinSize;
    }

    /**
     * Getter method for property <tt>connectMetaServerExecutorMaxSize</tt>.
     *
     * @return property value of connectMetaServerExecutorMaxSize
     */
    @Override
    public int getConnectMetaServerExecutorMaxSize() {
        return connectMetaServerExecutorMaxSize;
    }

    /**
     * Getter method for property <tt>connectMetaServerExecutorQueueSize</tt>.
     *
     * @return property value of connectMetaServerExecutorQueueSize
     */
    @Override
    public int getConnectMetaServerExecutorQueueSize() {
        return connectMetaServerExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>raftClientRefreshExecutorMinSize</tt>.
     *
     * @return property value of raftClientRefreshExecutorMinSize
     */
    @Override
    public int getRaftClientRefreshExecutorMinSize() {
        return raftClientRefreshExecutorMinSize;
    }

    /**
     * Getter method for property <tt>raftClientRefreshExecutorMaxSize</tt>.
     *
     * @return property value of raftClientRefreshExecutorMaxSize
     */
    @Override
    public int getRaftClientRefreshExecutorMaxSize() {
        return raftClientRefreshExecutorMaxSize;
    }

    /**
     * Getter method for property <tt>raftClientRefreshExecutorQueueSize</tt>.
     *
     * @return property value of raftClientRefreshExecutorQueueSize
     */
    @Override
    public int getRaftClientRefreshExecutorQueueSize() {
        return raftClientRefreshExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>metaSchedulerPoolSize </tt>.
     *
     * @param metaSchedulerPoolSize value to be assigned to property metaSchedulerPoolSize
     */
    public void setMetaSchedulerPoolSize(int metaSchedulerPoolSize) {
        this.metaSchedulerPoolSize = metaSchedulerPoolSize;
    }

    /**
     * Gets get meta scheduler pool size.
     *
     * @return the get meta scheduler pool size
     */
    @Override
    public int getMetaSchedulerPoolSize() {
        return metaSchedulerPoolSize;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     * Gets get default request executor min size.
     *
     * @return the get default request executor min size
     */
    public int getDefaultRequestExecutorMinSize() {
        return defaultRequestExecutorMinSize;
    }

    /**
     * Sets set default request executor min size.
     *
     * @param defaultRequestExecutorMinSize the default request executor min size
     */
    public void setDefaultRequestExecutorMinSize(int defaultRequestExecutorMinSize) {
        this.defaultRequestExecutorMinSize = defaultRequestExecutorMinSize;
    }

    /**
     * Gets get default request executor max size.
     *
     * @return the get default request executor max size
     */
    public int getDefaultRequestExecutorMaxSize() {
        return defaultRequestExecutorMaxSize;
    }

    /**
     * Sets set default request executor max size.
     *
     * @param defaultRequestExecutorMaxSize the default request executor max size
     */
    public void setDefaultRequestExecutorMaxSize(int defaultRequestExecutorMaxSize) {
        this.defaultRequestExecutorMaxSize = defaultRequestExecutorMaxSize;
    }

    /**
     * Gets get default request executor queue size.
     *
     * @return the get default request executor queue size
     */
    public int getDefaultRequestExecutorQueueSize() {
        return defaultRequestExecutorQueueSize;
    }

    /**
     * Sets set default request executor queue size.
     *
     * @param defaultRequestExecutorQueueSize the default request executor queue size
     */
    public void setDefaultRequestExecutorQueueSize(int defaultRequestExecutorQueueSize) {
        this.defaultRequestExecutorQueueSize = defaultRequestExecutorQueueSize;
    }

    /**
     * Gets get raft executor min size.
     *
     * @return the get raft executor min size
     */
    public int getRaftExecutorMinSize() {
        return raftExecutorMinSize;
    }

    /**
     * Sets set raft executor min size.
     *
     * @param raftExecutorMinSize the raft executor min size
     */
    public void setRaftExecutorMinSize(int raftExecutorMinSize) {
        this.raftExecutorMinSize = raftExecutorMinSize;
    }

    /**
     * Gets get raft executor max size.
     *
     * @return the get raft executor max size
     */
    public int getRaftExecutorMaxSize() {
        return raftExecutorMaxSize;
    }

    /**
     * Sets set raft executor max size.
     *
     * @param raftExecutorMaxSize the raft executor max size
     */
    public void setRaftExecutorMaxSize(int raftExecutorMaxSize) {
        this.raftExecutorMaxSize = raftExecutorMaxSize;
    }

    /**
     * Gets get raft executor queue size.
     *
     * @return the get raft executor queue size
     */
    public int getRaftExecutorQueueSize() {
        return raftExecutorQueueSize;
    }

    /**
     * Sets set raft executor queue size.
     *
     * @param raftExecutorQueueSize the raft executor queue size
     */
    public void setRaftExecutorQueueSize(int raftExecutorQueueSize) {
        this.raftExecutorQueueSize = raftExecutorQueueSize;
    }

    /**
     * Gets get raft server executor min size.
     *
     * @return the get raft server executor min size
     */
    public int getRaftServerExecutorMinSize() {
        return raftServerExecutorMinSize;
    }

    /**
     * Sets set raft server executor min size.
     *
     * @param raftServerExecutorMinSize the raft server executor min size
     */
    public void setRaftServerExecutorMinSize(int raftServerExecutorMinSize) {
        this.raftServerExecutorMinSize = raftServerExecutorMinSize;
    }

    /**
     * Gets get raft server executor max size.
     *
     * @return the get raft server executor max size
     */
    public int getRaftServerExecutorMaxSize() {
        return raftServerExecutorMaxSize;
    }

    /**
     * Sets set raft server executor max size.
     *
     * @param raftServerExecutorMaxSize the raft server executor max size
     */
    public void setRaftServerExecutorMaxSize(int raftServerExecutorMaxSize) {
        this.raftServerExecutorMaxSize = raftServerExecutorMaxSize;
    }

    /**
     * Gets get raft server executor queue size.
     *
     * @return the get raft server executor queue size
     */
    public int getRaftServerExecutorQueueSize() {
        return raftServerExecutorQueueSize;
    }

    /**
     * Sets set raft server executor queue size.
     *
     * @param raftServerExecutorQueueSize the raft server executor queue size
     */
    public void setRaftServerExecutorQueueSize(int raftServerExecutorQueueSize) {
        this.raftServerExecutorQueueSize = raftServerExecutorQueueSize;
    }

    /**
     * Gets get raft fsm executor min size.
     *
     * @return the get raft fsm executor min size
     */
    @Override
    public int getRaftFsmExecutorMinSize() {
        return raftFsmExecutorMinSize;
    }

    /**
     * Sets set raft fsm executor min size.
     *
     * @param raftFsmExecutorMinSize the raft fsm executor min size
     */
    public void setRaftFsmExecutorMinSize(int raftFsmExecutorMinSize) {
        this.raftFsmExecutorMinSize = raftFsmExecutorMinSize;
    }

    /**
     * Gets get raft fsm executor max size.
     *
     * @return the get raft fsm executor max size
     */
    @Override
    public int getRaftFsmExecutorMaxSize() {
        return raftFsmExecutorMaxSize;
    }

    /**
     * Sets set raft fsm executor max size.
     *
     * @param raftFsmExecutorMaxSize the raft fsm executor max size
     */
    public void setRaftFsmExecutorMaxSize(int raftFsmExecutorMaxSize) {
        this.raftFsmExecutorMaxSize = raftFsmExecutorMaxSize;
    }

    /**
     * Gets get raft fsm executor queue size.
     *
     * @return the get raft fsm executor queue size
     */
    @Override
    public int getRaftFsmExecutorQueueSize() {
        return raftFsmExecutorQueueSize;
    }

    /**
     * Sets set raft fsm executor queue size.
     *
     * @param raftFsmExecutorQueueSize the raft fsm executor queue size
     */
    public void setRaftFsmExecutorQueueSize(int raftFsmExecutorQueueSize) {
        this.raftFsmExecutorQueueSize = raftFsmExecutorQueueSize;
    }

    /**
     * Gets get raft election timeout.
     *
     * @return the get raft election timeout
     */
    public int getRaftElectionTimeout() {
        return raftElectionTimeout;
    }

    /**
     * Sets set raft election timeout.
     *
     * @param raftElectionTimeout the raft election timeout
     */
    public void setRaftElectionTimeout(int raftElectionTimeout) {
        this.raftElectionTimeout = raftElectionTimeout;
    }

    /**
     * Gets get session loadbalance threshold ratio.
     *
     * @return the get session loadbalance threshold ratio
     */
    @Override
    public double getSessionLoadbalanceThresholdRatio() {
        return sessionLoadbalanceThresholdRatio;
    }

    /**
     * Sets set session loadbalance threshold ratio.
     *
     * @param sessionLoadbalanceThresholdRatio the session loadbalance threshold ratio
     */
    public void setSessionLoadbalanceThresholdRatio(double sessionLoadbalanceThresholdRatio) {
        this.sessionLoadbalanceThresholdRatio = sessionLoadbalanceThresholdRatio;
    }

    /**
     * Sets set connect meta server executor min size.
     *
     * @param connectMetaServerExecutorMinSize the connect meta server executor min size
     * @return the set connect meta server executor min size
     */
    public MetaServerConfigBean setConnectMetaServerExecutorMinSize(int connectMetaServerExecutorMinSize) {
        this.connectMetaServerExecutorMinSize = connectMetaServerExecutorMinSize;
        return this;
    }

    /**
     * Sets set connect meta server executor max size.
     *
     * @param connectMetaServerExecutorMaxSize the connect meta server executor max size
     * @return the set connect meta server executor max size
     */
    public MetaServerConfigBean setConnectMetaServerExecutorMaxSize(int connectMetaServerExecutorMaxSize) {
        this.connectMetaServerExecutorMaxSize = connectMetaServerExecutorMaxSize;
        return this;
    }

    /**
     * Sets set connect meta server executor queue size.
     *
     * @param connectMetaServerExecutorQueueSize the connect meta server executor queue size
     * @return the set connect meta server executor queue size
     */
    public MetaServerConfigBean setConnectMetaServerExecutorQueueSize(int connectMetaServerExecutorQueueSize) {
        this.connectMetaServerExecutorQueueSize = connectMetaServerExecutorQueueSize;
        return this;
    }

    /**
     * Sets set raft client refresh executor min size.
     *
     * @param raftClientRefreshExecutorMinSize the raft client refresh executor min size
     * @return the set raft client refresh executor min size
     */
    public MetaServerConfigBean setRaftClientRefreshExecutorMinSize(int raftClientRefreshExecutorMinSize) {
        this.raftClientRefreshExecutorMinSize = raftClientRefreshExecutorMinSize;
        return this;
    }

    /**
     * Sets set raft client refresh executor max size.
     *
     * @param raftClientRefreshExecutorMaxSize the raft client refresh executor max size
     * @return the set raft client refresh executor max size
     */
    public MetaServerConfigBean setRaftClientRefreshExecutorMaxSize(int raftClientRefreshExecutorMaxSize) {
        this.raftClientRefreshExecutorMaxSize = raftClientRefreshExecutorMaxSize;
        return this;
    }

    /**
     * Sets set raft client refresh executor queue size.
     *
     * @param raftClientRefreshExecutorQueueSize the raft client refresh executor queue size
     * @return the set raft client refresh executor queue size
     */
    public MetaServerConfigBean setRaftClientRefreshExecutorQueueSize(int raftClientRefreshExecutorQueueSize) {
        this.raftClientRefreshExecutorQueueSize = raftClientRefreshExecutorQueueSize;
        return this;
    }

    public int getAppRevisionRegisterExecutorMinSize() {
        return appRevisionRegisterExecutorMinSize;
    }

    public void setAppRevisionRegisterExecutorMinSize(int appRevisionRegisterExecutorMinSize) {
        this.appRevisionRegisterExecutorMinSize = appRevisionRegisterExecutorMinSize;
    }

    public int getAppRevisionRegisterExecutorMaxSize() {
        return appRevisionRegisterExecutorMaxSize;
    }

    public void setAppRevisionRegisterExecutorMaxSize(int appRevisionRegisterExecutorMaxSize) {
        this.appRevisionRegisterExecutorMaxSize = appRevisionRegisterExecutorMaxSize;
    }

    public int getAppRevisionRegisterExecutorQueueSize() {
        return appRevisionRegisterExecutorQueueSize;
    }

    public void setAppRevisionRegisterExecutorQueueSize(int appRevisionRegisterExecutorQueueSize) {
        this.appRevisionRegisterExecutorQueueSize = appRevisionRegisterExecutorQueueSize;
    }
}