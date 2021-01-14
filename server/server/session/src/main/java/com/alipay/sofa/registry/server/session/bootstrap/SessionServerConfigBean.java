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
package com.alipay.sofa.registry.server.session.bootstrap;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.OsUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Session server config bean.
 *
 * @author shangyu.wh
 * @version $Id : SessionServerConfigBean.java, v 0.1 2017-11-14 11:49 synex Exp $
 */
@ConfigurationProperties(prefix = SessionServerConfigBean.PREFIX)
public class SessionServerConfigBean implements SessionServerConfig {

    /**
     * The constant PREFIX.
     */
    public static final String   PREFIX                                = "session.server";

    private int                  serverPort                            = 9600;

    private int                  syncSessionPort                       = 9602;

    private int                  metaServerPort                        = 9610;

    private int                  dataServerPort                        = 9620;

    private int                  httpServerPort;

    private int                  schedulerHeartbeatIntervalSec         = 3;

    private int                  cancelDataTaskRetryTimes              = 2;

    private long                 cancelDataTaskRetryFirstDelay         = 500;

    private long                 cancelDataTaskRetryIncrementDelay     = 500;

    private int                  publishDataTaskRetryTimes             = 2;

    private long                 publishDataTaskRetryFirstDelay        = 3000;

    private long                 publishDataTaskRetryIncrementDelay    = 5000;

    private int                  unPublishDataTaskRetryTimes           = 2;

    private long                 unPublishDataTaskRetryFirstDelay      = 3000;

    private long                 unPublishDataTaskRetryIncrementDelay  = 5000;

    private int                  subscriberRegisterFetchRetryTimes     = 3;

    private int                  accessDataExecutorMinPoolSize         = OsUtils.getCpuCount() * 10;

    private int                  accessDataExecutorMaxPoolSize         = OsUtils.getCpuCount() * 20;

    private int                  accessDataExecutorQueueSize           = 10000;

    private long                 accessDataExecutorKeepAliveTime       = 60;

    private int                  dataChangeExecutorMinPoolSize         = OsUtils.getCpuCount() * 2;

    private int                  dataChangeExecutorMaxPoolSize         = OsUtils.getCpuCount() * 3;

    private int                  dataChangeExecutorQueueSize           = 20000;

    private long                 dataChangeExecutorKeepAliveTime       = 60;

    private int                  connectClientExecutorMinPoolSize      = OsUtils.getCpuCount();

    private int                  connectClientExecutorMaxPoolSize      = OsUtils.getCpuCount();

    private int                  connectClientExecutorQueueSize        = 2000;

    private int                  dataChangeFetchTaskMaxBufferSize      = 10000;

    private int                  dataChangeFetchTaskWorkerSize         = OsUtils.getCpuCount() * 2;

    private int                  slotSyncMaxBufferSize                 = 5000;

    private int                  slotSyncWorkerSize                    = OsUtils.getCpuCount() * 4;

    private int                  clientNodeExchangeTimeOut             = 1000;                      //time out cause netty HashedWheelTimer occupy a lot of mem

    private int                  dataNodeExchangeTimeOut               = 3000;

    private int                  dataNodeExchangeForFetchDatumTimeOut  = 5000;

    private int                  metaNodeExchangeTimeOut               = 3000;

    private int                  pushTaskExecutorPoolSize              = OsUtils.getCpuCount() * 3;

    private int                  pushTaskExecutorQueueSize             = 20000;

    private int                  pushDataTaskRetryFirstDelayMillis     = 500;

    private int                  pushDataTaskRetryIncrementDelayMillis = 500;

    private int                  pushDataTaskDebouncingMillis          = 500;

    private int                  pushTaskRetryTimes                    = 3;

    private int                  publishDataExecutorMinPoolSize        = OsUtils.getCpuCount() * 10;

    private int                  publishDataExecutorMaxPoolSize        = OsUtils.getCpuCount() * 20;

    private int                  publishDataExecutorQueueSize          = 10000;

    private long                 publishDataExecutorKeepAliveTime      = 60;

    private int                  schedulerFetchDataVersionIntervalMs   = 1000 * 2;

    private double               accessLimitRate                       = 100000.0;

    private String               sessionServerRegion;

    private String               sessionServerDataCenter;

    private volatile boolean     stopPushSwitch                        = false;

    //begin config for enterprise version

    /**
     * forever close push zone，such as:RZBETA
     */
    private String               invalidForeverZones                   = "";
    /**
     * config regex,exception to the rule of forever close push zone
     */
    private String               invalidIgnoreDataidRegex              = "";

    private volatile Set<String> invalidForeverZonesSet;

    private Pattern              invalidIgnoreDataIdPattern            = null;

    private String               blacklistPubDataIdRegex               = "";

    private String               blacklistSubDataIdRegex               = "";

    private int                  dataNodeRetryExecutorQueueSize        = 20000;

    private int                  dataNodeRetryExecutorThreadSize       = OsUtils.getCpuCount();

    private int                  dataClientConnNum                     = 10;

    private int                  sessionSchedulerPoolSize              = OsUtils.getCpuCount();

    private int                  slotSyncPublisherMaxNum               = 512;

    private boolean              enableSessionLoadbalancePolicy        = false;

    private int                  cacheDigestFixDelaySecs               = 60 * 10;

    //end config for enterprise version

    private CommonConfig         commonConfig;

    private volatile Set<String> metaIps;

    /**
     * constructor
     *
     * @param commonConfig
     */
    public SessionServerConfigBean(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    /**
     * Getter method for property <tt>dataNodeRetryExecutorThreadSize</tt>.
     *
     * @return property value of dataNodeRetryExecutorThreadSize
     */
    @Override
    public int getDataNodeRetryExecutorThreadSize() {
        return dataNodeRetryExecutorThreadSize;
    }

    /**
     * Setter method for property <tt>dataNodeRetryExecutorThreadSize </tt>.
     *
     * @param dataNodeRetryExecutorThreadSize value to be assigned to property dataNodeRetryExecutorThreadSize
     */
    public void setDataNodeRetryExecutorThreadSize(int dataNodeRetryExecutorThreadSize) {
        this.dataNodeRetryExecutorThreadSize = dataNodeRetryExecutorThreadSize;
    }

    /**
     * Getter method for property <tt>dataNodeRetryExecutorQueueSize</tt>.
     *
     * @return property value of dataNodeRetryExecutorQueueSize
     */
    @Override
    public int getDataNodeRetryExecutorQueueSize() {
        return dataNodeRetryExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>dataNodeRetryExecutorQueueSize </tt>.
     *
     * @param dataNodeRetryExecutorQueueSize value to be assigned to property dataNodeRetryExecutorQueueSize
     */
    public void setDataNodeRetryExecutorQueueSize(int dataNodeRetryExecutorQueueSize) {
        this.dataNodeRetryExecutorQueueSize = dataNodeRetryExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>publishDataTaskRetryTimes</tt>.
     *
     * @return property value of publishDataTaskRetryTimes
     */
    @Override
    public int getPublishDataTaskRetryTimes() {
        return publishDataTaskRetryTimes;
    }

    /**
     * Setter method for property <tt>publishDataTaskRetryTimes </tt>.
     *
     * @param publishDataTaskRetryTimes value to be assigned to property publishDataTaskRetryTimes
     */
    public void setPublishDataTaskRetryTimes(int publishDataTaskRetryTimes) {
        this.publishDataTaskRetryTimes = publishDataTaskRetryTimes;
    }

    /**
     * Getter method for property <tt>unPublishDataTaskRetryTimes</tt>.
     *
     * @return property value of unPublishDataTaskRetryTimes
     */
    @Override
    public int getUnPublishDataTaskRetryTimes() {
        return unPublishDataTaskRetryTimes;
    }

    /**
     * Setter method for property <tt>unPublishDataTaskRetryTimes </tt>.
     *
     * @param unPublishDataTaskRetryTimes value to be assigned to property unPublishDataTaskRetryTimes
     */
    public void setUnPublishDataTaskRetryTimes(int unPublishDataTaskRetryTimes) {
        this.unPublishDataTaskRetryTimes = unPublishDataTaskRetryTimes;
    }

    /**
     * Getter method for property <tt>serverPort</tt>.
     *
     * @return property value of serverPort
     */
    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public int getSyncSessionPort() {
        return syncSessionPort;
    }

    /**
     * Setter method for property <tt>serverPort</tt>.
     *
     * @param serverPort value to be assigned to property serverPort
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Getter method for property <tt>schedulerHeartbeatIntervalSec</tt>.
     *
     * @return property value of schedulerHeartbeatIntervalSec
     */
    @Override
    public int getSchedulerHeartbeatIntervalSec() {
        return schedulerHeartbeatIntervalSec;
    }

    /**
     * Setter method for property <tt>schedulerHeartbeatIntervalSec</tt>.
     *
     * @param schedulerHeartbeatIntervalSec value to be assigned to property schedulerHeartbeatIntervalSec
     */
    public void setSchedulerHeartbeatTimeout(int schedulerHeartbeatIntervalSec) {
        this.schedulerHeartbeatIntervalSec = schedulerHeartbeatIntervalSec;
    }

    /**
     * Getter method for property <tt>cancelDataTaskRetryTimes</tt>.
     *
     * @return property value of cancelDataTaskRetryTimes
     */
    @Override
    public int getCancelDataTaskRetryTimes() {
        return cancelDataTaskRetryTimes;
    }

    /**
     * Setter method for property <tt>cancelDataTaskRetryTimes</tt>.
     *
     * @param cancelDataTaskRetryTimes value to be assigned to property cancelDataTaskRetryTimes
     */
    public void setCancelDataTaskRetryTimes(int cancelDataTaskRetryTimes) {
        this.cancelDataTaskRetryTimes = cancelDataTaskRetryTimes;
    }

    /**
     * Getter method for property <tt>cancelDataTaskRetryFirstDelay</tt>.
     *
     * @return property value of cancelDataTaskRetryFirstDelay
     */
    @Override
    public long getCancelDataTaskRetryFirstDelay() {
        return cancelDataTaskRetryFirstDelay;
    }

    /**
     * Getter method for property <tt>cancelDataTaskRetryIncrementDelay</tt>.
     *
     * @return property value of cancelDataTaskRetryIncrementDelay
     */
    @Override
    public long getCancelDataTaskRetryIncrementDelay() {
        return cancelDataTaskRetryIncrementDelay;
    }

    /**
     * Setter method for property <tt>cancelDataTaskRetryIncrementDelay</tt>.
     *
     * @param cancelDataTaskRetryIncrementDelay value to be assigned to property cancelDataTaskRetryIncrementDelay
     */
    public void setCancelDataTaskRetryIncrementDelay(long cancelDataTaskRetryIncrementDelay) {
        this.cancelDataTaskRetryIncrementDelay = cancelDataTaskRetryIncrementDelay;
    }

    /**
     * Setter method for property <tt>cancelDataTaskRetryFirstDelay </tt>.
     *
     * @param cancelDataTaskRetryFirstDelay value to be assigned to property cancelDataTaskRetryFirstDelay
     */
    public void setCancelDataTaskRetryFirstDelay(long cancelDataTaskRetryFirstDelay) {
        this.cancelDataTaskRetryFirstDelay = cancelDataTaskRetryFirstDelay;
    }

    /**
     * Getter method for property <tt>publishDataTaskRetryFirstDelay</tt>.
     *
     * @return property value of publishDataTaskRetryFirstDelay
     */
    @Override
    public long getPublishDataTaskRetryFirstDelay() {
        return publishDataTaskRetryFirstDelay;
    }

    /**
     * Setter method for property <tt>publishDataTaskRetryFirstDelay </tt>.
     *
     * @param publishDataTaskRetryFirstDelay value to be assigned to property publishDataTaskRetryFirstDelay
     */
    public void setPublishDataTaskRetryFirstDelay(long publishDataTaskRetryFirstDelay) {
        this.publishDataTaskRetryFirstDelay = publishDataTaskRetryFirstDelay;
    }

    /**
     * Getter method for property <tt>publishDataTaskRetryIncrementDelay</tt>.
     *
     * @return property value of publishDataTaskRetryIncrementDelay
     */
    @Override
    public long getPublishDataTaskRetryIncrementDelay() {
        return publishDataTaskRetryIncrementDelay;
    }

    /**
     * Setter method for property <tt>publishDataTaskRetryIncrementDelay </tt>.
     *
     * @param publishDataTaskRetryIncrementDelay value to be assigned to property publishDataTaskRetryIncrementDelay
     */
    public void setPublishDataTaskRetryIncrementDelay(long publishDataTaskRetryIncrementDelay) {
        this.publishDataTaskRetryIncrementDelay = publishDataTaskRetryIncrementDelay;
    }

    /**
     * Getter method for property <tt>unPublishDataTaskRetryFirstDelay</tt>.
     *
     * @return property value of unPublishDataTaskRetryFirstDelay
     */
    @Override
    public long getUnPublishDataTaskRetryFirstDelay() {
        return unPublishDataTaskRetryFirstDelay;
    }

    /**
     * Setter method for property <tt>unPublishDataTaskRetryFirstDelay </tt>.
     *
     * @param unPublishDataTaskRetryFirstDelay value to be assigned to property unPublishDataTaskRetryFirstDelay
     */
    public void setUnPublishDataTaskRetryFirstDelay(long unPublishDataTaskRetryFirstDelay) {
        this.unPublishDataTaskRetryFirstDelay = unPublishDataTaskRetryFirstDelay;
    }

    /**
     * Getter method for property <tt>unPublishDataTaskRetryIncrementDelay</tt>.
     *
     * @return property value of unPublishDataTaskRetryIncrementDelay
     */
    @Override
    public long getUnPublishDataTaskRetryIncrementDelay() {
        return unPublishDataTaskRetryIncrementDelay;
    }

    /**
     * Setter method for property <tt>unPublishDataTaskRetryIncrementDelay </tt>.
     *
     * @param unPublishDataTaskRetryIncrementDelay value to be assigned to property unPublishDataTaskRetryIncrementDelay
     */
    public void setUnPublishDataTaskRetryIncrementDelay(long unPublishDataTaskRetryIncrementDelay) {
        this.unPublishDataTaskRetryIncrementDelay = unPublishDataTaskRetryIncrementDelay;
    }

    @Override
    public int getPushTaskRetryTimes() {
        return pushTaskRetryTimes;
    }

    public void setPushTaskRetryTimes(int pushTaskRetryTimes) {
        this.pushTaskRetryTimes = pushTaskRetryTimes;
    }

    /**
     * Getter method for property <tt>subscriberRegisterFetchRetryTimes</tt>.
     *
     * @return property value of subscriberRegisterFetchRetryTimes
     */
    @Override
    public int getSubscriberRegisterFetchRetryTimes() {
        return subscriberRegisterFetchRetryTimes;
    }

    /**
     * Setter method for property <tt>subscriberRegisterFetchRetryTimes</tt>.
     *
     * @param subscriberRegisterFetchRetryTimes value to be assigned to property subscriberRegisterFetchRetryTimes
     */
    public void setSubscriberRegisterFetchRetryTimes(int subscriberRegisterFetchRetryTimes) {
        this.subscriberRegisterFetchRetryTimes = subscriberRegisterFetchRetryTimes;
    }

    /**
     * Getter method for property <tt>clientNodeExchangeTimeOut</tt>.
     *
     * @return property value of clientNodeExchangeTimeOut
     */
    @Override
    public int getClientNodeExchangeTimeOut() {
        return clientNodeExchangeTimeOut;
    }

    /**
     * Setter method for property <tt>clientNodeExchangeTimeOut</tt>.
     *
     * @param clientNodeExchangeTimeOut value to be assigned to property clientNodeExchangeTimeOut
     */
    public void setClientNodeExchangeTimeOut(int clientNodeExchangeTimeOut) {
        this.clientNodeExchangeTimeOut = clientNodeExchangeTimeOut;
    }

    /**
     * Getter method for property <tt>dataNodeExchangeTimeOut</tt>.
     *
     * @return property value of dataNodeExchangeTimeOut
     */
    @Override
    public int getDataNodeExchangeTimeOut() {
        return dataNodeExchangeTimeOut;
    }

    /**
     * Setter method for property <tt>dataNodeExchangeTimeOut</tt>.
     *
     * @param dataNodeExchangeTimeOut value to be assigned to property dataNodeExchangeTimeOut
     */
    public void setDataNodeExchangeTimeOut(int dataNodeExchangeTimeOut) {
        this.dataNodeExchangeTimeOut = dataNodeExchangeTimeOut;
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

    @Override
    public String getSessionServerRegion() {
        if (commonConfig != null) {
            String region = commonConfig.getLocalRegion();
            if (region != null && !region.isEmpty()) {
                return commonConfig.getLocalRegion().toUpperCase();
            }
        }

        if (sessionServerRegion != null) {
            sessionServerRegion = sessionServerRegion.toUpperCase();
        }
        return sessionServerRegion;
    }

    @Override
    public String getClientCell(String subscriberCell) {
        return this.getSessionServerRegion();
    }

    /**
     * Setter method for property <tt>sessionServerRegion</tt>.
     *
     * @param sessionServerRegion value to be assigned to property sessionServerRegion
     */
    public void setSessionServerRegion(String sessionServerRegion) {
        if (sessionServerRegion != null) {
            sessionServerRegion = sessionServerRegion.toUpperCase();
        }
        this.sessionServerRegion = sessionServerRegion;
    }

    @Override
    public String getSessionServerDataCenter() {
        if (commonConfig != null) {
            String dataCenter = commonConfig.getLocalDataCenter();
            if (dataCenter != null && !dataCenter.isEmpty()) {
                return commonConfig.getLocalDataCenter();
            }
        }

        return sessionServerDataCenter;
    }

    /**
     * Setter method for property <tt>sessionServerDataCenter</tt>.
     *
     * @param sessionServerDataCenter value to be assigned to property sessionServerDataCenter
     */
    public void setSessionServerDataCenter(String sessionServerDataCenter) {
        this.sessionServerDataCenter = sessionServerDataCenter;
    }

    /**
     * Getter method for property <tt>metaNodeExchangeTimeOut</tt>.
     *
     * @return property value of metaNodeExchangeTimeOut
     */
    @Override
    public int getMetaNodeExchangeTimeOut() {
        return metaNodeExchangeTimeOut;
    }

    /**
     * Setter method for property <tt>metaNodeExchangeTimeOut</tt>.
     *
     * @param metaNodeExchangeTimeOut value to be assigned to property metaNodeExchangeTimeOut
     */
    public void setMetaNodeExchangeTimeOut(int metaNodeExchangeTimeOut) {
        this.metaNodeExchangeTimeOut = metaNodeExchangeTimeOut;
    }

    /**
     * Getter method for property <tt>dataServerPort</tt>.
     *
     * @return property value of dataServerPort
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
     * Getter method for property <tt>httpServerPort</tt>.
     *
     * @return property value of httpServerPort
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
     * Getter method for property <tt>stopPushSwitch</tt>.
     *
     * @return property value of stopPushSwitch
     */
    @Override
    public boolean isStopPushSwitch() {
        return stopPushSwitch;
    }

    /**
     * Setter method for property <tt>stopPushSwitch</tt>.
     *
     * @param stopPushSwitch value to be assigned to property stopPushSwitch
     */
    @Override
    public void setStopPushSwitch(boolean stopPushSwitch) {
        this.stopPushSwitch = stopPushSwitch;
    }

    public String getInvalidForeverZones() {
        return invalidForeverZones;
    }

    /**
     * Setter method for property <tt>invalidForeverZones</tt>.
     *
     * @param invalidForeverZones value to be assigned to property invalidForeverZones
     */
    public void setInvalidForeverZones(String invalidForeverZones) {
        this.invalidForeverZones = invalidForeverZones;
    }

    public String getInvalidIgnoreDataidRegex() {
        return invalidIgnoreDataidRegex;
    }

    /**
     * Setter method for property <tt>invalidIgnoreDataidRegex</tt>.
     *
     * @param invalidIgnoreDataidRegex value to be assigned to property invalidIgnoreDataidRegex
     */
    public void setInvalidIgnoreDataidRegex(String invalidIgnoreDataidRegex) {
        this.invalidIgnoreDataidRegex = invalidIgnoreDataidRegex;
    }

    @Override
    public int getAccessDataExecutorMinPoolSize() {
        return accessDataExecutorMinPoolSize;
    }

    /**
     * Setter method for property <tt>accessDataExecutorMinPoolSize</tt>.
     *
     * @param accessDataExecutorMinPoolSize value to be assigned to property accessDataExecutorMinPoolSize
     */
    public void setAccessDataExecutorMinPoolSize(int accessDataExecutorMinPoolSize) {
        this.accessDataExecutorMinPoolSize = accessDataExecutorMinPoolSize;
    }

    @Override
    public int getAccessDataExecutorMaxPoolSize() {
        return accessDataExecutorMaxPoolSize;
    }

    /**
     * Setter method for property <tt>accessDataExecutorMaxPoolSize</tt>.
     *
     * @param accessDataExecutorMaxPoolSize value to be assigned to property accessDataExecutorMaxPoolSize
     */
    public void setAccessDataExecutorMaxPoolSize(int accessDataExecutorMaxPoolSize) {
        this.accessDataExecutorMaxPoolSize = accessDataExecutorMaxPoolSize;
    }

    @Override
    public int getAccessDataExecutorQueueSize() {
        return accessDataExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>accessDataExecutorQueueSize</tt>.
     *
     * @param accessDataExecutorQueueSize value to be assigned to property accessDataExecutorQueueSize
     */
    public void setAccessDataExecutorQueueSize(int accessDataExecutorQueueSize) {
        this.accessDataExecutorQueueSize = accessDataExecutorQueueSize;
    }

    @Override
    public long getAccessDataExecutorKeepAliveTime() {
        return accessDataExecutorKeepAliveTime;
    }

    /**
     * Setter method for property <tt>accessDataExecutorKeepAliveTime</tt>.
     *
     * @param accessDataExecutorKeepAliveTime value to be assigned to property accessDataExecutorKeepAliveTime
     */
    public void setAccessDataExecutorKeepAliveTime(long accessDataExecutorKeepAliveTime) {
        this.accessDataExecutorKeepAliveTime = accessDataExecutorKeepAliveTime;
    }

    /**
     * Getter method for property <tt>dataChangeExecutorMinPoolSize</tt>.
     *
     * @return property value of dataChangeExecutorMinPoolSize
     */
    @Override
    public int getDataChangeExecutorMinPoolSize() {
        return dataChangeExecutorMinPoolSize;
    }

    /**
     * Getter method for property <tt>dataChangeExecutorMaxPoolSize</tt>.
     *
     * @return property value of dataChangeExecutorMaxPoolSize
     */
    @Override
    public int getDataChangeExecutorMaxPoolSize() {
        return dataChangeExecutorMaxPoolSize;
    }

    /**
     * Getter method for property <tt>dataChangeExecutorQueueSize</tt>.
     *
     * @return property value of dataChangeExecutorQueueSize
     */
    @Override
    public int getDataChangeExecutorQueueSize() {
        return dataChangeExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>dataChangeExecutorKeepAliveTime</tt>.
     *
     * @return property value of dataChangeExecutorKeepAliveTime
     */
    @Override
    public long getDataChangeExecutorKeepAliveTime() {
        return dataChangeExecutorKeepAliveTime;
    }

    /**
     * Setter method for property <tt>dataChangeExecutorMinPoolSize</tt>.
     *
     * @param dataChangeExecutorMinPoolSize value to be assigned to property dataChangeExecutorMinPoolSize
     */
    public void setDataChangeExecutorMinPoolSize(int dataChangeExecutorMinPoolSize) {
        this.dataChangeExecutorMinPoolSize = dataChangeExecutorMinPoolSize;
    }

    /**
     * Setter method for property <tt>dataChangeExecutorMaxPoolSize</tt>.
     *
     * @param dataChangeExecutorMaxPoolSize value to be assigned to property dataChangeExecutorMaxPoolSize
     */
    public void setDataChangeExecutorMaxPoolSize(int dataChangeExecutorMaxPoolSize) {
        this.dataChangeExecutorMaxPoolSize = dataChangeExecutorMaxPoolSize;
    }

    /**
     * Setter method for property <tt>dataChangeExecutorQueueSize</tt>.
     *
     * @param dataChangeExecutorQueueSize value to be assigned to property dataChangeExecutorQueueSize
     */
    public void setDataChangeExecutorQueueSize(int dataChangeExecutorQueueSize) {
        this.dataChangeExecutorQueueSize = dataChangeExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>dataChangeExecutorKeepAliveTime</tt>.
     *
     * @param dataChangeExecutorKeepAliveTime value to be assigned to property dataChangeExecutorKeepAliveTime
     */
    public void setDataChangeExecutorKeepAliveTime(long dataChangeExecutorKeepAliveTime) {
        this.dataChangeExecutorKeepAliveTime = dataChangeExecutorKeepAliveTime;
    }

    /**
     * Getter method for property <tt>pushTaskExecutorMaxPoolSize</tt>.
     *
     * @return property value of pushTaskExecutorMaxPoolSize
     */
    @Override
    public int getPushTaskExecutorPoolSize() {
        return pushTaskExecutorPoolSize;
    }

    /**
     * Getter method for property <tt>pushTaskExecutorQueueSize</tt>.
     *
     * @return property value of pushTaskExecutorQueueSize
     */
    @Override
    public int getPushTaskExecutorQueueSize() {
        return pushTaskExecutorQueueSize;
    }

    @Override
    public int getPushDataTaskDebouncingMillis() {
        return pushDataTaskDebouncingMillis;
    }

    public void setPushDataTaskDebouncingMillis(int pushDataTaskDebouncingMillis) {
        this.pushDataTaskDebouncingMillis = pushDataTaskDebouncingMillis;
    }

    /**
     * Setter method for property <tt>pushTaskExecutorPoolSize</tt>.
     *
     * @param pushTaskExecutorPoolSize value to be assigned to property pushTaskExecutorPoolSize
     */
    public void setPushTaskExecutorPoolSize(int pushTaskExecutorPoolSize) {
        this.pushTaskExecutorPoolSize = pushTaskExecutorPoolSize;
    }

    /**
     * Setter method for property <tt>pushTaskExecutorQueueSize</tt>.
     *
     * @param pushTaskExecutorQueueSize value to be assigned to property pushTaskExecutorQueueSize
     */
    public void setPushTaskExecutorQueueSize(int pushTaskExecutorQueueSize) {
        this.pushTaskExecutorQueueSize = pushTaskExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>connectClientExecutorMinPoolSize</tt>.
     *
     * @return property value of connectClientExecutorMinPoolSize
     */
    public int getConnectClientExecutorMinPoolSize() {
        return connectClientExecutorMinPoolSize;
    }

    /**
     * Getter method for property <tt>connectClientExecutorMaxPoolSize</tt>.
     *
     * @return property value of connectClientExecutorMaxPoolSize
     */
    public int getConnectClientExecutorMaxPoolSize() {
        return connectClientExecutorMaxPoolSize;
    }

    /**
     * Getter method for property <tt>connectClientExecutorQueueSize</tt>.
     *
     * @return property value of connectClientExecutorQueueSize
     */
    public int getConnectClientExecutorQueueSize() {
        return connectClientExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>connectClientExecutorMinPoolSize</tt>.
     *
     * @param connectClientExecutorMinPoolSize value to be assigned to property connectClientExecutorMinPoolSize
     */
    public void setConnectClientExecutorMinPoolSize(int connectClientExecutorMinPoolSize) {
        this.connectClientExecutorMinPoolSize = connectClientExecutorMinPoolSize;
    }

    /**
     * Setter method for property <tt>connectClientExecutorMaxPoolSize</tt>.
     *
     * @param connectClientExecutorMaxPoolSize value to be assigned to property connectClientExecutorMaxPoolSize
     */
    public void setConnectClientExecutorMaxPoolSize(int connectClientExecutorMaxPoolSize) {
        this.connectClientExecutorMaxPoolSize = connectClientExecutorMaxPoolSize;
    }

    /**
     * Setter method for property <tt>connectClientExecutorQueueSize</tt>.
     *
     * @param connectClientExecutorQueueSize value to be assigned to property connectClientExecutorQueueSize
     */
    public void setConnectClientExecutorQueueSize(int connectClientExecutorQueueSize) {
        this.connectClientExecutorQueueSize = connectClientExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>dataChangeFetchTaskMaxBufferSize</tt>.
     *
     * @return property value of dataChangeFetchTaskMaxBufferSize
     */
    @Override
    public int getDataChangeFetchTaskMaxBufferSize() {
        return dataChangeFetchTaskMaxBufferSize;
    }

    /**
     * Setter method for property <tt>dataChangeFetchTaskMaxBufferSize</tt>.
     *
     * @param dataChangeFetchTaskMaxBufferSize value to be assigned to property dataChangeFetchTaskMaxBufferSize
     */
    public void setDataChangeFetchTaskMaxBufferSize(int dataChangeFetchTaskMaxBufferSize) {
        this.dataChangeFetchTaskMaxBufferSize = dataChangeFetchTaskMaxBufferSize;
    }

    /**
     * Getter method for property <tt>dataChangeFetchTaskWorkerSize</tt>.
     *
     * @return property value of dataChangeFetchTaskWorkerSize
     */
    @Override
    public int getDataChangeFetchTaskWorkerSize() {
        return dataChangeFetchTaskWorkerSize;
    }

    /**
     * Setter method for property <tt>dataChangeFetchTaskWorkerSize</tt>.
     *
     * @param dataChangeFetchTaskWorkerSize value to be assigned to property dataChangeFetchTaskWorkerSize
     */
    public void setDataChangeFetchTaskWorkerSize(int dataChangeFetchTaskWorkerSize) {
        this.dataChangeFetchTaskWorkerSize = dataChangeFetchTaskWorkerSize;
    }

    /**
     * Getter method for property <tt>pushDataTaskRetryFirstDelayMillis</tt>.
     *
     * @return property value of pushDataTaskRetryFirstDelayMillis
     */
    @Override
    public int getPushDataTaskRetryFirstDelayMillis() {
        return pushDataTaskRetryFirstDelayMillis;
    }

    /**
     * Setter method for property <tt>pushDataTaskRetryFirstDelayMillis</tt>.
     *
     * @param pushDataTaskRetryFirstDelayMillis value to be assigned to property pushDataTaskRetryFirstDelayMillis
     */
    public void setPushDataTaskRetryFirstDelayMillis(int pushDataTaskRetryFirstDelayMillis) {
        this.pushDataTaskRetryFirstDelayMillis = pushDataTaskRetryFirstDelayMillis;
    }

    /**
     * Getter method for property <tt>pushDataTaskRetryIncrementDelayMillis</tt>.
     *
     * @return property value of pushDataTaskRetryIncrementDelayMillis
     */
    @Override
    public int getPushDataTaskRetryIncrementDelayMillis() {
        return pushDataTaskRetryIncrementDelayMillis;
    }

    /**
     * Setter method for property <tt>pushDataTaskRetryIncrementDelayMillis</tt>.
     *
     * @param pushDataTaskRetryIncrementDelayMillis value to be assigned to property pushDataTaskRetryIncrementDelay
     */
    public void setPushDataTaskRetryIncrementDelayMillis(int pushDataTaskRetryIncrementDelayMillis) {
        this.pushDataTaskRetryIncrementDelayMillis = pushDataTaskRetryIncrementDelayMillis;
    }

    /**
     * Getter method for property <tt>blacklistPubDataIdRegex</tt>.
     *
     * @return property value of blacklistPubDataIdRegex
     */
    public String getBlacklistPubDataIdRegex() {
        return blacklistPubDataIdRegex;
    }

    /**
     * Getter method for property <tt>blacklistSubDataIdRegex</tt>.
     *
     * @return property value of blacklistSubDataIdRegex
     */
    public String getBlacklistSubDataIdRegex() {
        return blacklistSubDataIdRegex;
    }

    /**
     * Setter method for property <tt>blacklistPubDataIdRegex</tt>.
     *
     * @param blacklistPubDataIdRegex value to be assigned to property blacklistPubDataIdRegex
     */
    public void setBlacklistPubDataIdRegex(String blacklistPubDataIdRegex) {
        this.blacklistPubDataIdRegex = blacklistPubDataIdRegex;
    }

    /**
     * Setter method for property <tt>blacklistSubDataIdRegex</tt>.
     *
     * @param blacklistSubDataIdRegex value to be assigned to property blacklistSubDataIdRegex
     */
    public void setBlacklistSubDataIdRegex(String blacklistSubDataIdRegex) {
        this.blacklistSubDataIdRegex = blacklistSubDataIdRegex;
    }

    /**
     * Getter method for property <tt>publishDataExecutorMinPoolSize</tt>.
     *
     * @return property value of publishDataExecutorMinPoolSize
     */
    public int getPublishDataExecutorMinPoolSize() {
        return publishDataExecutorMinPoolSize;
    }

    /**
     * Getter method for property <tt>publishDataExecutorMaxPoolSize</tt>.
     *
     * @return property value of publishDataExecutorMaxPoolSize
     */
    public int getPublishDataExecutorMaxPoolSize() {
        return publishDataExecutorMaxPoolSize;
    }

    /**
     * Getter method for property <tt>publishDataExecutorQueueSize</tt>.
     *
     * @return property value of publishDataExecutorQueueSize
     */
    public int getPublishDataExecutorQueueSize() {
        return publishDataExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>publishDataExecutorKeepAliveTime</tt>.
     *
     * @return property value of publishDataExecutorKeepAliveTime
     */
    public long getPublishDataExecutorKeepAliveTime() {
        return publishDataExecutorKeepAliveTime;
    }

    /**
     * Setter method for property <tt>publishDataExecutorMinPoolSize</tt>.
     *
     * @param publishDataExecutorMinPoolSize value to be assigned to property publishDataExecutorMinPoolSize
     */
    public void setPublishDataExecutorMinPoolSize(int publishDataExecutorMinPoolSize) {
        this.publishDataExecutorMinPoolSize = publishDataExecutorMinPoolSize;
    }

    /**
     * Setter method for property <tt>publishDataExecutorMaxPoolSize</tt>.
     *
     * @param publishDataExecutorMaxPoolSize value to be assigned to property publishDataExecutorMaxPoolSize
     */
    public void setPublishDataExecutorMaxPoolSize(int publishDataExecutorMaxPoolSize) {
        this.publishDataExecutorMaxPoolSize = publishDataExecutorMaxPoolSize;
    }

    /**
     * Setter method for property <tt>publishDataExecutorQueueSize</tt>.
     *
     * @param publishDataExecutorQueueSize value to be assigned to property publishDataExecutorQueueSize
     */
    public void setPublishDataExecutorQueueSize(int publishDataExecutorQueueSize) {
        this.publishDataExecutorQueueSize = publishDataExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>publishDataExecutorKeepAliveTime</tt>.
     *
     * @param publishDataExecutorKeepAliveTime value to be assigned to property publishDataExecutorKeepAliveTime
     */
    public void setPublishDataExecutorKeepAliveTime(long publishDataExecutorKeepAliveTime) {
        this.publishDataExecutorKeepAliveTime = publishDataExecutorKeepAliveTime;
    }

    /**
     * Getter method for property <tt>accessLimitRate</tt>.
     *
     * @return property value of accessLimitRate
     */
    public double getAccessLimitRate() {
        return accessLimitRate;
    }

    /**
     * Setter method for property <tt>accessLimitRate</tt>.
     *
     * @param accessLimitRate value to be assigned to property accessLimitRate
     */
    public void setAccessLimitRate(double accessLimitRate) {
        this.accessLimitRate = accessLimitRate;
    }

    /**
     * Getter method for property <tt>dataClientConnNum</tt>.
     *
     * @return property value of dataClientConnNum
     */
    @Override
    public int getDataClientConnNum() {
        return dataClientConnNum;
    }

    /**
     * Setter method for property <tt>dataClientConnNum </tt>.
     *
     * @param dataClientConnNum value to be assigned to property dataClientConnNum
     */
    public void setDataClientConnNum(int dataClientConnNum) {
        this.dataClientConnNum = dataClientConnNum;
    }

    @Override
    public boolean isInvalidForeverZone(String zoneId) {
        if (invalidForeverZonesSet == null) {
            String[] zoneNameArr = getInvalidForeverZones().split(";");
            Set<String> set = new HashSet<>();
            for (String str : zoneNameArr) {
                if (str.trim().length() > 0) {
                    set.add(str);
                }
            }
            invalidForeverZonesSet = set;
        }
        return invalidForeverZonesSet.contains(zoneId);
    }

    @Override
    public boolean isInvalidIgnored(String dataId) {

        String invalidIgnoreDataidRegex = getInvalidIgnoreDataidRegex();
        if (null != invalidIgnoreDataidRegex && !invalidIgnoreDataidRegex.isEmpty()) {
            invalidIgnoreDataIdPattern = Pattern.compile(invalidIgnoreDataidRegex);
        }

        return null != invalidIgnoreDataIdPattern
               && invalidIgnoreDataIdPattern.matcher(dataId).find();
    }

    /**
     * Getter method for property <tt>sessionSchedulerPoolSize</tt>.
     *
     * @return property value of sessionSchedulerPoolSize
     */
    @Override
    public int getSessionSchedulerPoolSize() {
        return sessionSchedulerPoolSize;
    }

    /**
     * Setter method for property <tt>sessionSchedulerPoolSize </tt>.
     *
     * @param sessionSchedulerPoolSize value to be assigned to property sessionSchedulerPoolSize
     */
    public void setSessionSchedulerPoolSize(int sessionSchedulerPoolSize) {
        this.sessionSchedulerPoolSize = sessionSchedulerPoolSize;
    }

    /**
     * Getter method for property <tt>dataNodeExchangeForFetchDatumTimeOut</tt>.
     *
     * @return property value of dataNodeExchangeForFetchDatumTimeOut
     */
    public int getDataNodeExchangeForFetchDatumTimeOut() {
        return dataNodeExchangeForFetchDatumTimeOut;
    }

    /**
     * Setter method for property <tt>dataNodeExchangeForFetchDatumTimeOut </tt>.
     *
     * @param dataNodeExchangeForFetchDatumTimeOut value to be assigned to property dataNodeExchangeForFetchDatumTimeOut
     */
    public void setDataNodeExchangeForFetchDatumTimeOut(int dataNodeExchangeForFetchDatumTimeOut) {
        this.dataNodeExchangeForFetchDatumTimeOut = dataNodeExchangeForFetchDatumTimeOut;
    }

    /**
     * Getter method for property <tt>slotSyncPublisherMaxNum</tt>.
     *
     * @return property value of slotSyncPublisherMaxNum
     */
    @Override
    public int getSlotSyncPublisherMaxNum() {
        return slotSyncPublisherMaxNum;
    }

    @Override
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
     * Setter method for property <tt>slotSyncPublisherMaxNum</tt>.
     *
     * @param slotSyncPublisherMaxNum value to be assigned to property slotSyncPublisherMaxNum
     */
    public void setSlotSyncPublisherMaxNum(int slotSyncPublisherMaxNum) {
        this.slotSyncPublisherMaxNum = slotSyncPublisherMaxNum;
    }

    /**
     * Getter method for property <tt>schedulerFetchDataVersionIntervalMs</tt>.
     *
     * @return property value of schedulerFetchDataVersionIntervalMs
     */
    @Override
    public int getSchedulerFetchDataVersionIntervalMs() {
        return schedulerFetchDataVersionIntervalMs;
    }

    /**
     * Setter method for property <tt>schedulerFetchDataVersionIntervalMs</tt>.
     *
     * @param schedulerFetchDataVersionIntervalMs value to be assigned to property schedulerFetchDataVersionIntervalMs
     */
    public void setSchedulerFetchDataVersionIntervalMs(int schedulerFetchDataVersionIntervalMs) {
        this.schedulerFetchDataVersionIntervalMs = schedulerFetchDataVersionIntervalMs;
    }

    public int getSlotSyncMaxBufferSize() {
        return slotSyncMaxBufferSize;
    }

    public void setSlotSyncMaxBufferSize(int slotSyncMaxBufferSize) {
        this.slotSyncMaxBufferSize = slotSyncMaxBufferSize;
    }

    public int getSlotSyncWorkerSize() {
        return slotSyncWorkerSize;
    }

    @Override
    public int getCacheDigestFixDelaySecs() {
        return cacheDigestFixDelaySecs;
    }

    public void setCacheDigestFixDelaySecs(int cacheDigestFixDelaySecs) {
        this.cacheDigestFixDelaySecs = cacheDigestFixDelaySecs;
    }

    public void setSlotSyncWorkerSize(int slotSyncWorkerSize) {
        this.slotSyncWorkerSize = slotSyncWorkerSize;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public boolean isEnableSessionLoadbalancePolicy() {
        return enableSessionLoadbalancePolicy;
    }

    public void setEnableSessionLoadbalancePolicy(boolean enableSessionLoadbalancePolicy) {
        this.enableSessionLoadbalancePolicy = enableSessionLoadbalancePolicy;
    }
}