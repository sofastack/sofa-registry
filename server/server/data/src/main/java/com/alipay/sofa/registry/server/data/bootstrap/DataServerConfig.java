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
package com.alipay.sofa.registry.server.data.bootstrap;

import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.OsUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 *
 *
 * @author qian.lqlq
 * @version $Id: DataServerBootstrapConfig.java, v 0.1 2017-12-06 20:50 qian.lqlq Exp $
 */
@ConfigurationProperties(prefix = DataServerConfig.PRE_FIX)
public class DataServerConfig {

    public static final String   PRE_FIX                                  = "data.server";

    private int                  port;

    private int                  syncDataPort;

    private int                  syncSessionPort;

    private int                  metaServerPort;

    private int                  httpServerPort;

    private int                  notifyExecutorPoolSize                   = OsUtils.getCpuCount() * 3;

    private int                  notifyExecutorQueueSize                  = 10000;

    private int                  notifyRetryQueueSize                     = 10000;

    private int                  notifyMaxItems                           = 500;

    private int                  notifyIntervalMs                         = 500;

    private int                  notifyRetryTimes                         = 3;

    private int                  notifyTempExecutorPoolSize               = OsUtils.getCpuCount() * 3;

    private int                  notifyTempExecutorQueueSize              = 4000;
    private int                  notifyTempDataIntervalMs                 = 200;

    private int                  rpcTimeout                               = 3000;

    private CommonConfig         commonConfig;

    private volatile Set<String> metaIps                                  = null;

    private int                  getDataExecutorMinPoolSize               = OsUtils.getCpuCount() * 5;

    private int                  getDataExecutorMaxPoolSize               = OsUtils.getCpuCount() * 10;

    private int                  getDataExecutorQueueSize                 = 20000;

    private long                 getDataExecutorKeepAliveTime             = 60;

    private int                  publishExecutorMinPoolSize               = OsUtils.getCpuCount() * 5;

    private int                  publishExecutorMaxPoolSize               = OsUtils.getCpuCount() * 10;

    private int                  publishExecutorQueueSize                 = 10000;

    private volatile int         sessionLeaseSec                          = 30;

    private int                  datumCompactDelayMs                      = 1000 * 60 * 3;

    private int                  slotLeaderSyncSessionExecutorThreadSize  = OsUtils.getCpuCount() * 3;
    private int                  slotLeaderSyncSessionExecutorQueueSize   = 40000;
    private volatile int         slotLeaderSyncSessionIntervalSec         = 4;

    private int                  slotFollowerSyncLeaderExecutorThreadSize = OsUtils.getCpuCount();
    private int                  slotFollowerSyncLeaderExecutorQueueSize  = 10000;
    private volatile int         slotFollowerSyncLeaderIntervalMs         = 1000 * 30;

    // the publisher.digest if len(registerId/uuid+long+long), 50bytes
    private volatile int         slotSyncPublisherDigestMaxNum            = 8000;

    private volatile int         slotSyncPublisherMaxNum                  = 512;

    private int                  slotSyncRequestExecutorMinPoolSize       = OsUtils.getCpuCount() * 3;

    private int                  slotSyncRequestExecutorMaxPoolSize       = OsUtils.getCpuCount() * 3;

    private int                  slotSyncRequestExecutorQueueSize         = 1000;

    private int                  schedulerHeartbeatIntervalSec            = 3;

    private boolean              enableTestApi                            = false;

    /**
     * constructor
     * @param commonConfig
     */
    public DataServerConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    public boolean isLocalDataCenter(String dataCenter) {
        return commonConfig.getLocalDataCenter().equals(dataCenter);
    }

    public String getLocalDataCenter() {
        return commonConfig.getLocalDataCenter();
    }

    /**
     * Getter method for property <tt>port</tt>.
     *
     * @return property value of port
     */
    public int getPort() {
        return port;
    }

    /**
     * Setter method for property <tt>port</tt>.
     *
     * @param port  value to be assigned to property port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Getter method for property <tt>syncDataPort</tt>.
     *
     * @return property value of syncDataPort
     */
    public int getSyncDataPort() {
        return syncDataPort;
    }

    /**
     * Setter method for property <tt>syncDataPort</tt>.
     *
     * @param syncDataPort  value to be assigned to property syncDataPort
     */
    public void setSyncDataPort(int syncDataPort) {
        this.syncDataPort = syncDataPort;
    }

    /**
     * Getter method for property <tt>metaServerPort</tt>.
     *
     * @return property value of metaServerPort
     */
    public int getMetaServerPort() {
        return metaServerPort;
    }

    /**
     * Setter method for property <tt>metaServerPort</tt>.
     *
     * @param metaServerPort  value to be assigned to property metaServerPort
     */
    public void setMetaServerPort(int metaServerPort) {
        this.metaServerPort = metaServerPort;
    }

    /**
     * Getter method for property <tt>httpServerPort</tt>.
     *
     * @return property value of httpServerPort
     */
    public int getHttpServerPort() {
        return httpServerPort;
    }

    /**
     * Setter method for property <tt>httpServerPort</tt>.
     *
     * @param httpServerPort  value to be assigned to property httpServerPort
     */
    public void setHttpServerPort(int httpServerPort) {
        this.httpServerPort = httpServerPort;
    }

    /**
     * Getter method for property <tt>notifyIntervalMs</tt>.
     *
     * @return property value of notifyIntervalMs
     */
    public int getNotifyIntervalMs() {
        return notifyIntervalMs;
    }

    /**
     * Setter method for property <tt>notifyIntervalMs</tt>.
     *
     * @param notifyIntervalMs  value to be assigned to property notifyIntervalMs
     */
    public void setNotifyIntervalMs(int notifyIntervalMs) {
        this.notifyIntervalMs = notifyIntervalMs;
    }

    /**
     * Getter method for property <tt>notifyTempDataIntervalMs</tt>.
     *
     * @return property value of notifyTempDataIntervalMs
     */
    public int getNotifyTempDataIntervalMs() {
        return notifyTempDataIntervalMs;
    }

    /**
     * Setter method for property <tt>notifyTempDataIntervalMs</tt>.
     *
     * @param notifyTempDataIntervalMs  value to be assigned to property notifyTempDataIntervalMs
     */
    public void setNotifyTempDataIntervalMs(int notifyTempDataIntervalMs) {
        this.notifyTempDataIntervalMs = notifyTempDataIntervalMs;
    }

    /**
     * Getter method for property <tt>rpcTimeout</tt>.
     *
     * @return property value of rpcTimeout
     */
    public int getRpcTimeout() {
        return rpcTimeout;
    }

    /**
     * Setter method for property <tt>rpcTimeout</tt>.
     *
     * @param rpcTimeout  value to be assigned to property rpcTimeout
     */
    public void setRpcTimeout(int rpcTimeout) {
        this.rpcTimeout = rpcTimeout;
    }

    /**
     * Getter method for property <tt>getDataExecutorMinPoolSize</tt>.
     *
     * @return property value of getDataExecutorMinPoolSize
     */
    public int getGetDataExecutorMinPoolSize() {
        return getDataExecutorMinPoolSize;
    }

    /**
     * Getter method for property <tt>getDataExecutorMaxPoolSize</tt>.
     *
     * @return property value of getDataExecutorMaxPoolSize
     */
    public int getGetDataExecutorMaxPoolSize() {
        return getDataExecutorMaxPoolSize;
    }

    /**
     * Getter method for property <tt>getDataExecutorQueueSize</tt>.
     *
     * @return property value of getDataExecutorQueueSize
     */
    public int getGetDataExecutorQueueSize() {
        return getDataExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>getDataExecutorKeepAliveTime</tt>.
     *
     * @return property value of getDataExecutorKeepAliveTime
     */
    public long getGetDataExecutorKeepAliveTime() {
        return getDataExecutorKeepAliveTime;
    }

    /**
     * Setter method for property <tt>getDataExecutorMinPoolSize</tt>.
     *
     * @param getDataExecutorMinPoolSize  value to be assigned to property getDataExecutorMinPoolSize
     */
    public void setGetDataExecutorMinPoolSize(int getDataExecutorMinPoolSize) {
        this.getDataExecutorMinPoolSize = getDataExecutorMinPoolSize;
    }

    /**
     * Setter method for property <tt>getDataExecutorMaxPoolSize</tt>.
     *
     * @param getDataExecutorMaxPoolSize  value to be assigned to property getDataExecutorMaxPoolSize
     */
    public void setGetDataExecutorMaxPoolSize(int getDataExecutorMaxPoolSize) {
        this.getDataExecutorMaxPoolSize = getDataExecutorMaxPoolSize;
    }

    /**
     * Setter method for property <tt>getDataExecutorQueueSize</tt>.
     *
     * @param getDataExecutorQueueSize  value to be assigned to property getDataExecutorQueueSize
     */
    public void setGetDataExecutorQueueSize(int getDataExecutorQueueSize) {
        this.getDataExecutorQueueSize = getDataExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>getDataExecutorKeepAliveTime</tt>.
     *
     * @param getDataExecutorKeepAliveTime  value to be assigned to property getDataExecutorKeepAliveTime
     */
    public void setGetDataExecutorKeepAliveTime(long getDataExecutorKeepAliveTime) {
        this.getDataExecutorKeepAliveTime = getDataExecutorKeepAliveTime;
    }

    /**
     * Getter method for property <tt>publishExecutorMinPoolSize</tt>.
     *
     * @return property value of publishExecutorMinPoolSize
     */
    public int getPublishExecutorMinPoolSize() {
        return publishExecutorMinPoolSize;
    }

    /**
     * Setter method for property <tt>publishExecutorMinPoolSize</tt>.
     *
     * @param publishExecutorMinPoolSize  value to be assigned to property publishExecutorMinPoolSize
     */
    public void setPublishExecutorMinPoolSize(int publishExecutorMinPoolSize) {
        this.publishExecutorMinPoolSize = publishExecutorMinPoolSize;
    }

    /**
     * Getter method for property <tt>publishExecutorMaxPoolSize</tt>.
     *
     * @return property value of publishExecutorMaxPoolSize
     */
    public int getPublishExecutorMaxPoolSize() {
        return publishExecutorMaxPoolSize;
    }

    /**
     * Setter method for property <tt>publishExecutorMaxPoolSize</tt>.
     *
     * @param publishExecutorMaxPoolSize  value to be assigned to property publishExecutorMaxPoolSize
     */
    public void setPublishExecutorMaxPoolSize(int publishExecutorMaxPoolSize) {
        this.publishExecutorMaxPoolSize = publishExecutorMaxPoolSize;
    }

    /**
     * Getter method for property <tt>publishExecutorQueueSize</tt>.
     *
     * @return property value of publishExecutorQueueSize
     */
    public int getPublishExecutorQueueSize() {
        return publishExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>publishExecutorQueueSize</tt>.
     *
     * @param publishExecutorQueueSize  value to be assigned to property publishExecutorQueueSize
     */
    public void setPublishExecutorQueueSize(int publishExecutorQueueSize) {
        this.publishExecutorQueueSize = publishExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>metaServerIpAddress</tt>.
     *
     * @return property value of metaServerIpAddress
     */
    public Set<String> getMetaServerIpAddresses() {
        final Set<String> ips = metaIps;
        if (ips != null && !ips.isEmpty()) {
            return ips;
        }
        metaIps = ServerEnv.transferMetaIps(commonConfig.getMetaNode(),
            commonConfig.getLocalDataCenter());
        return metaIps;
    }

    /**
     * Getter method for property <tt>sessionLeaseSec</tt>.
     * @return property value of sessionLeaseSec
     */
    public int getSessionLeaseSec() {
        return sessionLeaseSec;
    }

    /**
     * Setter method for property <tt>sessionLeaseSec</tt>.
     * @param sessionLeaseSec value to be assigned to property sessionLeaseSec
     */
    public void setSessionLeaseSec(int sessionLeaseSec) {
        this.sessionLeaseSec = sessionLeaseSec;
    }

    /**
     * Getter method for property <tt>datumCompactDelayMs</tt>.
     * @return property value of datumCompactDelayMs
     */
    public int getDatumCompactDelayMs() {
        return datumCompactDelayMs;
    }

    /**
     * Setter method for property <tt>datumCompactDelayMs</tt>.
     * @param datumCompactDelayMs value to be assigned to property datumCompactDelayMs
     */
    public void setDatumCompactDelayMs(int datumCompactDelayMs) {
        this.datumCompactDelayMs = datumCompactDelayMs;
    }

    /**
     * Getter method for property <tt>slotSyncPublisherDigestMaxNum</tt>.
     * @return property value of slotSyncPublisherDigestMaxNum
     */
    public int getSlotSyncPublisherDigestMaxNum() {
        return slotSyncPublisherDigestMaxNum;
    }

    /**
     * Setter method for property <tt>slotSyncPublisherDigestMaxNum</tt>.
     * @param slotSyncPublisherDigestMaxNum value to be assigned to property slotSyncPublisherDigestMaxNum
     */
    public void setSlotSyncPublisherDigestMaxNum(int slotSyncPublisherDigestMaxNum) {
        this.slotSyncPublisherDigestMaxNum = slotSyncPublisherDigestMaxNum;
    }

    /**
     * Getter method for property <tt>slotSyncPublisherMaxNum</tt>.
     * @return property value of slotSyncPublisherMaxNum
     */
    public int getSlotSyncPublisherMaxNum() {
        return slotSyncPublisherMaxNum;
    }

    /**
     * Setter method for property <tt>slotSyncPublisherMaxNum</tt>.
     * @param slotSyncPublisherMaxNum value to be assigned to property slotSyncPublisherMaxNum
     */
    public void setSlotSyncPublisherMaxNum(int slotSyncPublisherMaxNum) {
        this.slotSyncPublisherMaxNum = slotSyncPublisherMaxNum;
    }

    /**
     * Getter method for property <tt>slotLeaderSyncSessionExecutorThreadSize</tt>.
     * @return property value of slotLeaderSyncSessionExecutorThreadSize
     */
    public int getSlotLeaderSyncSessionExecutorThreadSize() {
        return slotLeaderSyncSessionExecutorThreadSize;
    }

    /**
     * Setter method for property <tt>slotLeaderSyncSessionExecutorThreadSize</tt>.
     * @param slotLeaderSyncSessionExecutorThreadSize value to be assigned to property slotLeaderSyncSessionExecutorThreadSize
     */
    public void setSlotLeaderSyncSessionExecutorThreadSize(int slotLeaderSyncSessionExecutorThreadSize) {
        this.slotLeaderSyncSessionExecutorThreadSize = slotLeaderSyncSessionExecutorThreadSize;
    }

    /**
     * Getter method for property <tt>slotLeaderSyncSessionIntervalMs</tt>.
     * @return property value of slotLeaderSyncSessionIntervalMs
     */
    public int getSlotLeaderSyncSessionIntervalSec() {
        return slotLeaderSyncSessionIntervalSec;
    }

    /**
     * Setter method for property <tt>slotLeaderSyncSessionIntervalSec</tt>.
     * @param slotLeaderSyncSessionIntervalSec value to be assigned to property slotLeaderSyncSessionIntervalSec
     */
    public void setSlotLeaderSyncSessionIntervalSec(int slotLeaderSyncSessionIntervalSec) {
        this.slotLeaderSyncSessionIntervalSec = slotLeaderSyncSessionIntervalSec;
    }

    /**
     * Getter method for property <tt>slotFollowerSyncLeaderExecutorThreadSize</tt>.
     * @return property value of slotFollowerSyncLeaderExecutorThreadSize
     */
    public int getSlotFollowerSyncLeaderExecutorThreadSize() {
        return slotFollowerSyncLeaderExecutorThreadSize;
    }

    /**
     * Setter method for property <tt>slotFollowerSyncLeaderExecutorThreadSize</tt>.
     * @param slotFollowerSyncLeaderExecutorThreadSize value to be assigned to property slotFollowerSyncLeaderExecutorThreadSize
     */
    public void setSlotFollowerSyncLeaderExecutorThreadSize(int slotFollowerSyncLeaderExecutorThreadSize) {
        this.slotFollowerSyncLeaderExecutorThreadSize = slotFollowerSyncLeaderExecutorThreadSize;
    }

    /**
     * Getter method for property <tt>slotFollowerSyncLeaderIntervalMs</tt>.
     * @return property value of slotFollowerSyncLeaderIntervalMs
     */
    public int getSlotFollowerSyncLeaderIntervalMs() {
        return slotFollowerSyncLeaderIntervalMs;
    }

    /**
     * Setter method for property <tt>slotFollowerSyncLeaderIntervalMs</tt>.
     * @param slotFollowerSyncLeaderIntervalMs value to be assigned to property slotFollowerSyncLeaderIntervalMs
     */
    public void setSlotFollowerSyncLeaderIntervalMs(int slotFollowerSyncLeaderIntervalMs) {
        this.slotFollowerSyncLeaderIntervalMs = slotFollowerSyncLeaderIntervalMs;
    }

    /**
     * Getter method for property <tt>slotLeaderSyncSessionExecutorQueueSize</tt>.
     * @return property value of slotLeaderSyncSessionExecutorQueueSize
     */
    public int getSlotLeaderSyncSessionExecutorQueueSize() {
        return slotLeaderSyncSessionExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>slotLeaderSyncSessionExecutorQueueSize</tt>.
     * @param slotLeaderSyncSessionExecutorQueueSize value to be assigned to property slotLeaderSyncSessionExecutorQueueSize
     */
    public void setSlotLeaderSyncSessionExecutorQueueSize(int slotLeaderSyncSessionExecutorQueueSize) {
        this.slotLeaderSyncSessionExecutorQueueSize = slotLeaderSyncSessionExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>slotFollowerSyncLeaderExecutorQueueSize</tt>.
     * @return property value of slotFollowerSyncLeaderExecutorQueueSize
     */
    public int getSlotFollowerSyncLeaderExecutorQueueSize() {
        return slotFollowerSyncLeaderExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>slotFollowerSyncLeaderExecutorQueueSize</tt>.
     * @param slotFollowerSyncLeaderExecutorQueueSize value to be assigned to property slotFollowerSyncLeaderExecutorQueueSize
     */
    public void setSlotFollowerSyncLeaderExecutorQueueSize(int slotFollowerSyncLeaderExecutorQueueSize) {
        this.slotFollowerSyncLeaderExecutorQueueSize = slotFollowerSyncLeaderExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>slotSyncRequestExecutorMinPoolSize</tt>.
     * @return property value of slotSyncRequestExecutorMinPoolSize
     */
    public int getSlotSyncRequestExecutorMinPoolSize() {
        return slotSyncRequestExecutorMinPoolSize;
    }

    /**
     * Setter method for property <tt>slotSyncRequestExecutorMinPoolSize</tt>.
     * @param slotSyncRequestExecutorMinPoolSize value to be assigned to property slotSyncRequestExecutorMinPoolSize
     */
    public void setSlotSyncRequestExecutorMinPoolSize(int slotSyncRequestExecutorMinPoolSize) {
        this.slotSyncRequestExecutorMinPoolSize = slotSyncRequestExecutorMinPoolSize;
    }

    /**
     * Getter method for property <tt>slotSyncRequestExecutorMaxPoolSize</tt>.
     * @return property value of slotSyncRequestExecutorMaxPoolSize
     */
    public int getSlotSyncRequestExecutorMaxPoolSize() {
        return slotSyncRequestExecutorMaxPoolSize;
    }

    /**
     * Setter method for property <tt>slotSyncRequestExecutorMaxPoolSize</tt>.
     * @param slotSyncRequestExecutorMaxPoolSize value to be assigned to property slotSyncRequestExecutorMaxPoolSize
     */
    public void setSlotSyncRequestExecutorMaxPoolSize(int slotSyncRequestExecutorMaxPoolSize) {
        this.slotSyncRequestExecutorMaxPoolSize = slotSyncRequestExecutorMaxPoolSize;
    }

    /**
     * Getter method for property <tt>slotSyncRequestExecutorQueueSize</tt>.
     * @return property value of slotSyncRequestExecutorQueueSize
     */
    public int getSlotSyncRequestExecutorQueueSize() {
        return slotSyncRequestExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>slotSyncRequestExecutorQueueSize</tt>.
     * @param slotSyncRequestExecutorQueueSize value to be assigned to property slotSyncRequestExecutorQueueSize
     */
    public void setSlotSyncRequestExecutorQueueSize(int slotSyncRequestExecutorQueueSize) {
        this.slotSyncRequestExecutorQueueSize = slotSyncRequestExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>schedulerHeartbeatIntervalSec</tt>.
     * @return property value of schedulerHeartbeatIntervalSec
     */
    public int getSchedulerHeartbeatIntervalSec() {
        return schedulerHeartbeatIntervalSec;
    }

    /**
     * Setter method for property <tt>schedulerHeartbeatIntervalSec</tt>.
     * @param schedulerHeartbeatIntervalSec value to be assigned to property schedulerHeartbeatIntervalSec
     */
    public void setSchedulerHeartbeatIntervalSec(int schedulerHeartbeatIntervalSec) {
        this.schedulerHeartbeatIntervalSec = schedulerHeartbeatIntervalSec;
    }

    /**
     * Getter method for property <tt>syncSessionPort</tt>.
     * @return property value of syncSessionPort
     */
    public int getSyncSessionPort() {
        return syncSessionPort;
    }

    /**
     * Setter method for property <tt>syncSessionPort</tt>.
     * @param syncSessionPort value to be assigned to property syncSessionPort
     */
    public void setSyncSessionPort(int syncSessionPort) {
        this.syncSessionPort = syncSessionPort;
    }

    public boolean isEnableTestApi() {
        return enableTestApi;
    }

    public void setEnableTestApi(boolean enableTestApi) {
        this.enableTestApi = enableTestApi;
    }

    public int getNotifyMaxItems() {
        return notifyMaxItems;
    }

    public void setNotifyMaxItems(int notifyMaxItems) {
        this.notifyMaxItems = notifyMaxItems;
    }

    public int getNotifyExecutorPoolSize() {
        return notifyExecutorPoolSize;
    }

    public void setNotifyExecutorPoolSize(int notifyExecutorPoolSize) {
        this.notifyExecutorPoolSize = notifyExecutorPoolSize;
    }

    public int getNotifyExecutorQueueSize() {
        return notifyExecutorQueueSize;
    }

    public void setNotifyExecutorQueueSize(int notifyExecutorQueueSize) {
        this.notifyExecutorQueueSize = notifyExecutorQueueSize;
    }

    public int getNotifyTempExecutorPoolSize() {
        return notifyTempExecutorPoolSize;
    }

    public void setNotifyTempExecutorPoolSize(int notifyTempExecutorPoolSize) {
        this.notifyTempExecutorPoolSize = notifyTempExecutorPoolSize;
    }

    public int getNotifyTempExecutorQueueSize() {
        return notifyTempExecutorQueueSize;
    }

    public void setNotifyTempExecutorQueueSize(int notifyTempExecutorQueueSize) {
        this.notifyTempExecutorQueueSize = notifyTempExecutorQueueSize;
    }

    public int getNotifyRetryQueueSize() {
        return notifyRetryQueueSize;
    }

    public void setNotifyRetryQueueSize(int notifyRetryQueueSize) {
        this.notifyRetryQueueSize = notifyRetryQueueSize;
    }

    public int getNotifyRetryTimes() {
        return notifyRetryTimes;
    }

    public void setNotifyRetryTimes(int notifyRetryTimes) {
        this.notifyRetryTimes = notifyRetryTimes;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
