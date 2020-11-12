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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The type Session server config bean.
 * @author shangyu.wh
 * @version $Id : SessionServerConfigBean.java, v 0.1 2017-11-14 11:49 synex Exp $
 */
@ConfigurationProperties(prefix = SessionServerConfigBean.PREFIX)
public class SessionServerConfigBean implements SessionServerConfig {

    /**
     * The constant PREFIX.
     */
    public static final String PREFIX                                  = "session.server";

    private int                serverPort                              = 9600;

    private int                metaServerPort                          = 9610;

    private int                dataServerPort                          = 9620;

    private int                httpServerPort;

    private int                schedulerHeartbeatTimeout               = 3;

    private int                schedulerHeartbeatFirstDelay            = 3;

    private int                schedulerHeartbeatExpBackOffBound       = 1;

    private int                schedulerGetSessionNodeTimeout          = 3;

    private int                schedulerGetSessionNodeFirstDelay       = 5;

    private int                schedulerGetSessionNodeExpBackOffBound  = 10;

    private int                schedulerFetchDataTimeout               = 1;               //MINUTES

    private int                schedulerFetchDataFirstDelay            = 30;

    private int                schedulerFetchDataExpBackOffBound       = 10;

    private int                schedulerConnectMetaTimeout             = 5;

    private int                schedulerConnectMetaFirstDelay          = 5;

    private int                schedulerConnectMetaExpBackOffBound     = 3;

    private int                schedulerConnectDataTimeout             = 10;

    private int                schedulerConnectDataFirstDelay          = 10;

    private int                schedulerConnectDataExpBackOffBound     = 3;

    private int                schedulerCleanInvalidClientTimeOut      = 3;

    private int                schedulerCleanInvalidClientFirstDelay   = 10;

    private int                schedulerCleanInvalidClientBackOffBound = 5;

    private int                cancelDataTaskRetryTimes                = 2;

    private long               cancelDataTaskRetryFirstDelay           = 500;

    private long               cancelDataTaskRetryIncrementDelay       = 500;

    private int                publishDataTaskRetryTimes               = 2;

    private long               publishDataTaskRetryFirstDelay          = 3000;

    private long               publishDataTaskRetryIncrementDelay      = 5000;

    private int                unPublishDataTaskRetryTimes             = 2;

    private long               unPublishDataTaskRetryFirstDelay        = 3000;

    private long               unPublishDataTaskRetryIncrementDelay    = 5000;

    private int                datumSnapshotTaskRetryTimes             = 1;

    private long               datumSnapshotTaskRetryFirstDelay        = 5000;

    private long               datumSnapshotTaskRetryIncrementDelay    = 5000;

    private int                renewDatumTaskRetryTimes                = 1;

    private int                dataChangeFetchTaskRetryTimes           = 3;

    private int                subscriberRegisterFetchRetryTimes       = 3;

    private int                receivedDataMultiPushTaskRetryTimes     = 3;

    private int                sessionRegisterDataServerTaskRetryTimes = 5;

    private int                defaultSessionExecutorMinPoolSize       = cpus();

    private int                defaultSessionExecutorMaxPoolSize       = cpus() * 5;      //5*CPUs by default

    private long               defaultSessionExecutorKeepAliveTime     = 60;

    private int                accessDataExecutorMinPoolSize           = 100;

    private int                accessDataExecutorMaxPoolSize           = 400;

    private int                accessDataExecutorQueueSize             = 10000;

    private long               accessDataExecutorKeepAliveTime         = 60;

    private int                pushTaskExecutorMinPoolSize             = 40;

    private int                pushTaskExecutorMaxPoolSize             = 400;

    private int                pushTaskExecutorQueueSize               = 100000;

    private long               pushTaskExecutorKeepAliveTime           = 60;

    private int                dataChangeExecutorMinPoolSize           = 40;

    private int                dataChangeExecutorMaxPoolSize           = 400;

    private int                dataChangeExecutorQueueSize             = 100000;

    private long               dataChangeExecutorKeepAliveTime         = 60;

    private int                connectClientExecutorMinPoolSize        = 60;

    private int                connectClientExecutorMaxPoolSize        = 400;

    private int                connectClientExecutorQueueSize          = 10000;

    private int                dataChangeFetchTaskMaxBufferSize        = 1000000;

    private int                dataChangeFetchTaskWorkerSize           = 100;

    private int                clientNodeExchangeTimeOut               = 1000;            //time out cause netty HashedWheelTimer occupy a lot of mem

    private int                dataNodeExchangeTimeOut                 = 3000;

    private int                dataNodeExchangeForFetchDatumTimeOut    = 5000;

    private int                metaNodeExchangeTimeOut                 = 3000;

    private int                numberOfReplicas                        = 1000;

    private int                userDataPushRetryWheelTicksSize         = 5120;

    private int                userDataPushRetryWheelTicksDuration     = 100;

    private int                userDataPushRetryExecutorQueueSize      = 1000000;

    private int                userDataPushRetryExecutorThreadSize     = 10;

    private int                renewDatumWheelTicksSize                = 2048;

    private int                renewDatumWheelTicksDuration            = 500;

    private int                renewDatumWheelTaskDelaySec             = 180;

    private int                renewDatumWheelTaskRandomFirstDelaySec  = 200;

    private int                renewDatumWheelThreadSize               = 10;

    private int                renewDatumWheelQueueSize                = 10000;

    private int                pushDataTaskRetryFirstDelay             = 500;

    private long               pushDataTaskRetryIncrementDelay         = 500;

    private long               pushTaskConfirmWaitTimeout              = 10000;

    private int                pushTaskConfirmCheckWheelTicksSize      = 1024;

    private int                pushTaskConfirmCheckWheelTicksDuration  = 100;

    private int                pushTaskConfirmCheckExecutorQueueSize   = 10000;

    private int                pushTaskConfirmCheckExecutorThreadSize  = 10;

    private int                publishDataExecutorMinPoolSize          = 100;

    private int                publishDataExecutorMaxPoolSize          = 400;

    private int                publishDataExecutorQueueSize            = 10000;

    private long               publishDataExecutorKeepAliveTime        = 60;

    private double             accessLimitRate                         = 100000.0;

    private String             sessionServerRegion;

    private String             sessionServerDataCenter;

    private boolean            stopPushSwitch                          = false;

    private boolean            beginDataFetchTask                      = true;

    //begin config for enterprise version

    /** forever close push zone，such as:RZBETA */
    private String             invalidForeverZones                     = "";
    /** config regex,exception to the rule of forever close push zone*/
    private String             invalidIgnoreDataidRegex                = "";

    private Set<String>        invalidForeverZonesSet;

    private Pattern            invalidIgnoreDataIdPattern              = null;

    private String             blacklistPubDataIdRegex                 = "";

    private String             blacklistSubDataIdRegex                 = "";

    private int                renewAndSnapshotSilentPeriodSec         = 20;

    private int                writeDataAcceptorQueueSize              = 10000;

    private int                dataNodeRetryExecutorQueueSize          = 1000000;

    private int                dataNodeRetryExecutorThreadSize         = 100;

    private int                dataClientConnNum                       = 10;

    private int                sessionSchedulerPoolSize                = 6;

    private boolean            enableSessionLoadbalancePolicy          = false;

    //end config for enterprise version

    private CommonConfig       commonConfig;

    /**
     * constructor
     * @param commonConfig
     */
    public SessionServerConfigBean(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    /**
     * Getter method for property <tt>renewDatumWheelThreadSize</tt>.
     *
     * @return property value of renewDatumWheelThreadSize
     */
    public int getRenewDatumWheelThreadSize() {
        return renewDatumWheelThreadSize;
    }

    /**
     * Setter method for property <tt>renewDatumWheelThreadSize </tt>.
     *
     * @param renewDatumWheelThreadSize  value to be assigned to property renewDatumWheelThreadSize
     */
    public void setRenewDatumWheelThreadSize(int renewDatumWheelThreadSize) {
        this.renewDatumWheelThreadSize = renewDatumWheelThreadSize;
    }

    /**
     * Getter method for property <tt>renewDatumWheelQueueSize</tt>.
     *
     * @return property value of renewDatumWheelQueueSize
     */
    public int getRenewDatumWheelQueueSize() {
        return renewDatumWheelQueueSize;
    }

    /**
     * Setter method for property <tt>renewDatumWheelQueueSize </tt>.
     *
     * @param renewDatumWheelQueueSize  value to be assigned to property renewDatumWheelQueueSize
     */
    public void setRenewDatumWheelQueueSize(int renewDatumWheelQueueSize) {
        this.renewDatumWheelQueueSize = renewDatumWheelQueueSize;
    }

    /**
     * Getter method for property <tt>userDataPushRetryExecutorQueueSize</tt>.
     *
     * @return property value of userDataPushRetryExecutorQueueSize
     */
    @Override
    public int getUserDataPushRetryExecutorQueueSize() {
        return userDataPushRetryExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>userDataPushRetryExecutorQueueSize </tt>.
     *
     * @param userDataPushRetryExecutorQueueSize  value to be assigned to property userDataPushRetryExecutorQueueSize
     */
    public void setUserDataPushRetryExecutorQueueSize(int userDataPushRetryExecutorQueueSize) {
        this.userDataPushRetryExecutorQueueSize = userDataPushRetryExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>userDataPushRetryExecutorThreadSize</tt>.
     *
     * @return property value of userDataPushRetryExecutorThreadSize
     */
    @Override
    public int getUserDataPushRetryExecutorThreadSize() {
        return userDataPushRetryExecutorThreadSize;
    }

    /**
     * Setter method for property <tt>userDataPushRetryExecutorThreadSize </tt>.
     *
     * @param userDataPushRetryExecutorThreadSize  value to be assigned to property userDataPushRetryExecutorThreadSize
     */
    public void setUserDataPushRetryExecutorThreadSize(int userDataPushRetryExecutorThreadSize) {
        this.userDataPushRetryExecutorThreadSize = userDataPushRetryExecutorThreadSize;
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
     * @param dataNodeRetryExecutorThreadSize  value to be assigned to property dataNodeRetryExecutorThreadSize
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
     * @param dataNodeRetryExecutorQueueSize  value to be assigned to property dataNodeRetryExecutorQueueSize
     */
    public void setDataNodeRetryExecutorQueueSize(int dataNodeRetryExecutorQueueSize) {
        this.dataNodeRetryExecutorQueueSize = dataNodeRetryExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>writeDataAcceptorQueueSize</tt>.
     *
     * @return property value of writeDataAcceptorQueueSize
     */
    public int getWriteDataAcceptorQueueSize() {
        return writeDataAcceptorQueueSize;
    }

    /**
     * Setter method for property <tt>writeDataAcceptorQueueSize </tt>.
     *
     * @param writeDataAcceptorQueueSize  value to be assigned to property writeDataAcceptorQueueSize
     */
    public void setWriteDataAcceptorQueueSize(int writeDataAcceptorQueueSize) {
        this.writeDataAcceptorQueueSize = writeDataAcceptorQueueSize;
    }

    /**
     * Getter method for property <tt>renewAndSnapshotSilentPeriodSec</tt>.
     *
     * @return property value of renewAndSnapshotSilentPeriodSec
     */
    public int getRenewAndSnapshotSilentPeriodSec() {
        return renewAndSnapshotSilentPeriodSec;
    }

    /**
     * Setter method for property <tt>renewAndSnapshotSilentPeriodSec </tt>.
     *
     * @param renewAndSnapshotSilentPeriodSec  value to be assigned to property renewAndSnapshotSilentPeriodSec
     */
    public void setRenewAndSnapshotSilentPeriodSec(int renewAndSnapshotSilentPeriodSec) {
        this.renewAndSnapshotSilentPeriodSec = renewAndSnapshotSilentPeriodSec;
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
     * @param publishDataTaskRetryTimes  value to be assigned to property publishDataTaskRetryTimes
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
     * @param unPublishDataTaskRetryTimes  value to be assigned to property unPublishDataTaskRetryTimes
     */
    public void setUnPublishDataTaskRetryTimes(int unPublishDataTaskRetryTimes) {
        this.unPublishDataTaskRetryTimes = unPublishDataTaskRetryTimes;
    }

    /**
     * Getter method for property <tt>datumSnapshotTaskRetryTimes</tt>.
     *
     * @return property value of datumSnapshotTaskRetryTimes
     */
    @Override
    public int getDatumSnapshotTaskRetryTimes() {
        return datumSnapshotTaskRetryTimes;
    }

    /**
     * Setter method for property <tt>datumSnapshotTaskRetryTimes </tt>.
     *
     * @param datumSnapshotTaskRetryTimes  value to be assigned to property datumSnapshotTaskRetryTimes
     */
    public void setDatumSnapshotTaskRetryTimes(int datumSnapshotTaskRetryTimes) {
        this.datumSnapshotTaskRetryTimes = datumSnapshotTaskRetryTimes;
    }

    /**
     * Getter method for property <tt>renewDatumTaskRetryTimes</tt>.
     *
     * @return property value of renewDatumTaskRetryTimes
     */
    @Override
    public int getRenewDatumTaskRetryTimes() {
        return renewDatumTaskRetryTimes;
    }

    /**
     * Setter method for property <tt>renewDatumTaskRetryTimes </tt>.
     *
     * @param renewDatumTaskRetryTimes  value to be assigned to property renewDatumTaskRetryTimes
     */
    public void setRenewDatumTaskRetryTimes(int renewDatumTaskRetryTimes) {
        this.renewDatumTaskRetryTimes = renewDatumTaskRetryTimes;
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

    /**
     * Setter method for property <tt>serverPort</tt>.
     *
     * @param serverPort value to be assigned to property serverPort
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Getter method for property <tt>schedulerHeartbeatTimeout</tt>.
     *
     * @return property value of schedulerHeartbeatTimeout
     */
    @Override
    public int getSchedulerHeartbeatTimeout() {
        return schedulerHeartbeatTimeout;
    }

    /**
     * Setter method for property <tt>schedulerHeartbeatTimeout</tt>.
     *
     * @param schedulerHeartbeatTimeout value to be assigned to property schedulerHeartbeatTimeout
     */
    public void setSchedulerHeartbeatTimeout(int schedulerHeartbeatTimeout) {
        this.schedulerHeartbeatTimeout = schedulerHeartbeatTimeout;
    }

    /**
     * Getter method for property <tt>schedulerHeartbeatFirstDelay</tt>.
     *
     * @return property value of schedulerHeartbeatFirstDelay
     */
    @Override
    public int getSchedulerHeartbeatFirstDelay() {
        return schedulerHeartbeatFirstDelay;
    }

    /**
     * Setter method for property <tt>schedulerHeartbeatFirstDelay</tt>.
     *
     * @param schedulerHeartbeatFirstDelay value to be assigned to property schedulerHeartbeatFirstDelay
     */
    public void setSchedulerHeartbeatFirstDelay(int schedulerHeartbeatFirstDelay) {
        this.schedulerHeartbeatFirstDelay = schedulerHeartbeatFirstDelay;
    }

    /**
     * Getter method for property <tt>schedulerHeartbeatExpBackOffBound</tt>.
     *
     * @return property value of schedulerHeartbeatExpBackOffBound
     */
    @Override
    public int getSchedulerHeartbeatExpBackOffBound() {
        return schedulerHeartbeatExpBackOffBound;
    }

    /**
     * Setter method for property <tt>schedulerHeartbeatExpBackOffBound</tt>.
     *
     * @param schedulerHeartbeatExpBackOffBound value to be assigned to property schedulerHeartbeatExpBackOffBound
     */
    public void setSchedulerHeartbeatExpBackOffBound(int schedulerHeartbeatExpBackOffBound) {
        this.schedulerHeartbeatExpBackOffBound = schedulerHeartbeatExpBackOffBound;
    }

    /**
     * Getter method for property <tt>schedulerFetchDataTimeout</tt>.
     *
     * @return property value of schedulerFetchDataTimeout
     */
    @Override
    public int getSchedulerFetchDataTimeout() {
        return schedulerFetchDataTimeout;
    }

    /**
     * Setter method for property <tt>schedulerFetchDataTimeout</tt>.
     *
     * @param schedulerFetchDataTimeout value to be assigned to property schedulerFetchDataTimeout
     */
    public void setSchedulerFetchDataTimeout(int schedulerFetchDataTimeout) {
        this.schedulerFetchDataTimeout = schedulerFetchDataTimeout;
    }

    /**
     * Getter method for property <tt>schedulerFetchDataFirstDelay</tt>.
     *
     * @return property value of schedulerFetchDataFirstDelay
     */
    @Override
    public int getSchedulerFetchDataFirstDelay() {
        return schedulerFetchDataFirstDelay;
    }

    /**
     * Setter method for property <tt>schedulerFetchDataFirstDelay</tt>.
     *
     * @param schedulerFetchDataFirstDelay value to be assigned to property schedulerFetchDataFirstDelay
     */
    public void setSchedulerFetchDataFirstDelay(int schedulerFetchDataFirstDelay) {
        this.schedulerFetchDataFirstDelay = schedulerFetchDataFirstDelay;
    }

    /**
     * Getter method for property <tt>schedulerFetchDataExpBackOffBound</tt>.
     *
     * @return property value of schedulerFetchDataExpBackOffBound
     */
    @Override
    public int getSchedulerFetchDataExpBackOffBound() {
        return schedulerFetchDataExpBackOffBound;
    }

    /**
     * Setter method for property <tt>schedulerFetchDataExpBackOffBound</tt>.
     *
     * @param schedulerFetchDataExpBackOffBound value to be assigned to property schedulerFetchDataExpBackOffBound
     */
    public void setSchedulerFetchDataExpBackOffBound(int schedulerFetchDataExpBackOffBound) {
        this.schedulerFetchDataExpBackOffBound = schedulerFetchDataExpBackOffBound;
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
     * @param cancelDataTaskRetryIncrementDelay  value to be assigned to property cancelDataTaskRetryIncrementDelay
     */
    public void setCancelDataTaskRetryIncrementDelay(long cancelDataTaskRetryIncrementDelay) {
        this.cancelDataTaskRetryIncrementDelay = cancelDataTaskRetryIncrementDelay;
    }

    /**
     * Setter method for property <tt>cancelDataTaskRetryFirstDelay </tt>.
     *
     * @param cancelDataTaskRetryFirstDelay  value to be assigned to property cancelDataTaskRetryFirstDelay
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
     * @param publishDataTaskRetryFirstDelay  value to be assigned to property publishDataTaskRetryFirstDelay
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
     * @param publishDataTaskRetryIncrementDelay  value to be assigned to property publishDataTaskRetryIncrementDelay
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
     * @param unPublishDataTaskRetryFirstDelay  value to be assigned to property unPublishDataTaskRetryFirstDelay
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
     * @param unPublishDataTaskRetryIncrementDelay  value to be assigned to property unPublishDataTaskRetryIncrementDelay
     */
    public void setUnPublishDataTaskRetryIncrementDelay(long unPublishDataTaskRetryIncrementDelay) {
        this.unPublishDataTaskRetryIncrementDelay = unPublishDataTaskRetryIncrementDelay;
    }

    /**
     * Getter method for property <tt>datumSnapshotTaskRetryFirstDelay</tt>.
     *
     * @return property value of datumSnapshotTaskRetryFirstDelay
     */
    @Override
    public long getDatumSnapshotTaskRetryFirstDelay() {
        return datumSnapshotTaskRetryFirstDelay;
    }

    /**
     * Setter method for property <tt>datumSnapshotTaskRetryFirstDelay </tt>.
     *
     * @param datumSnapshotTaskRetryFirstDelay  value to be assigned to property datumSnapshotTaskRetryFirstDelay
     */
    public void setDatumSnapshotTaskRetryFirstDelay(long datumSnapshotTaskRetryFirstDelay) {
        this.datumSnapshotTaskRetryFirstDelay = datumSnapshotTaskRetryFirstDelay;
    }

    /**
     * Getter method for property <tt>datumSnapshotTaskRetryIncrementDelay</tt>.
     *
     * @return property value of datumSnapshotTaskRetryIncrementDelay
     */
    @Override
    public long getDatumSnapshotTaskRetryIncrementDelay() {
        return datumSnapshotTaskRetryIncrementDelay;
    }

    /**
     * Setter method for property <tt>datumSnapshotTaskRetryIncrementDelay </tt>.
     *
     * @param datumSnapshotTaskRetryIncrementDelay  value to be assigned to property datumSnapshotTaskRetryIncrementDelay
     */
    public void setDatumSnapshotTaskRetryIncrementDelay(long datumSnapshotTaskRetryIncrementDelay) {
        this.datumSnapshotTaskRetryIncrementDelay = datumSnapshotTaskRetryIncrementDelay;
    }

    /**
     * Getter method for property <tt>receivedDataMultiPushTaskRetryTimes</tt>.
     *
     * @return property value of receivedDataMultiPushTaskRetryTimes
     */
    @Override
    public int getReceivedDataMultiPushTaskRetryTimes() {
        return receivedDataMultiPushTaskRetryTimes;
    }

    /**
     * Setter method for property <tt>receivedDataMultiPushTaskRetryTimes</tt>.
     *
     * @param receivedDataMultiPushTaskRetryTimes value to be assigned to property receivedDataMultiPushTaskRetryTimes
     */
    public void setReceivedDataMultiPushTaskRetryTimes(int receivedDataMultiPushTaskRetryTimes) {
        this.receivedDataMultiPushTaskRetryTimes = receivedDataMultiPushTaskRetryTimes;
    }

    /**
     * Getter method for property <tt>dataChangeFetchTaskRetryTimes</tt>.
     *
     * @return property value of dataChangeFetchTaskRetryTimes
     */
    @Override
    public int getDataChangeFetchTaskRetryTimes() {
        return dataChangeFetchTaskRetryTimes;
    }

    /**
     * Setter method for property <tt>dataChangeFetchTaskRetryTimes</tt>.
     *
     * @param dataChangeFetchTaskRetryTimes value to be assigned to property dataChangeFetchTaskRetryTimes
     */
    public void setDataChangeFetchTaskRetryTimes(int dataChangeFetchTaskRetryTimes) {
        this.dataChangeFetchTaskRetryTimes = dataChangeFetchTaskRetryTimes;
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
     * Getter method for property <tt>sessionRegisterDataServerTaskRetryTimes</tt>.
     *
     * @return property value of sessionRegisterDataServerTaskRetryTimes
     */
    @Override
    public int getSessionRegisterDataServerTaskRetryTimes() {
        return sessionRegisterDataServerTaskRetryTimes;
    }

    /**
     * Setter method for property <tt>sessionRegisterDataServerTaskRetryTimes</tt>.
     *
     * @param sessionRegisterDataServerTaskRetryTimes  value to be assigned to property sessionRegisterDataServerTaskRetryTimes
     */
    public void setSessionRegisterDataServerTaskRetryTimes(int sessionRegisterDataServerTaskRetryTimes) {
        this.sessionRegisterDataServerTaskRetryTimes = sessionRegisterDataServerTaskRetryTimes;
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
     * Getter method for property <tt>numberOfReplicas</tt>.
     *
     * @return property value of numberOfReplicas
     */
    @Override
    public int getNumberOfReplicas() {
        return numberOfReplicas;
    }

    /**
     * Setter method for property <tt>numberOfReplicas</tt>.
     *
     * @param numberOfReplicas value to be assigned to property numberOfReplicas
     */
    public void setNumberOfReplicas(int numberOfReplicas) {
        this.numberOfReplicas = numberOfReplicas;
    }

    /**
     * Getter method for property <tt>schedulerGetSessionNodeTimeout</tt>.
     *
     * @return property value of schedulerGetSessionNodeTimeout
     */
    @Override
    public int getSchedulerGetSessionNodeTimeout() {
        return schedulerGetSessionNodeTimeout;
    }

    /**
     * Setter method for property <tt>schedulerGetSessionNodeTimeout</tt>.
     *
     * @param schedulerGetSessionNodeTimeout value to be assigned to property schedulerGetSessionNodeTimeout
     */
    public void setSchedulerGetSessionNodeTimeout(int schedulerGetSessionNodeTimeout) {
        this.schedulerGetSessionNodeTimeout = schedulerGetSessionNodeTimeout;
    }

    /**
     * Getter method for property <tt>schedulerGetSessionNodeFirstDelay</tt>.
     *
     * @return property value of schedulerGetSessionNodeFirstDelay
     */
    @Override
    public int getSchedulerGetSessionNodeFirstDelay() {
        return schedulerGetSessionNodeFirstDelay;
    }

    /**
     * Setter method for property <tt>schedulerGetSessionNodeFirstDelay</tt>.
     *
     * @param schedulerGetSessionNodeFirstDelay value to be assigned to property schedulerGetSessionNodeFirstDelay
     */
    public void setSchedulerGetSessionNodeFirstDelay(int schedulerGetSessionNodeFirstDelay) {
        this.schedulerGetSessionNodeFirstDelay = schedulerGetSessionNodeFirstDelay;
    }

    /**
     * Getter method for property <tt>schedulerGetSessionNodeExpBackOffBound</tt>.
     *
     * @return property value of schedulerGetSessionNodeExpBackOffBound
     */
    @Override
    public int getSchedulerGetSessionNodeExpBackOffBound() {
        return schedulerGetSessionNodeExpBackOffBound;
    }

    /**
     * Setter method for property <tt>schedulerGetSessionNodeExpBackOffBound</tt>.
     *
     * @param schedulerGetSessionNodeExpBackOffBound value to be assigned to property schedulerGetSessionNodeExpBackOffBound
     */
    public void setSchedulerGetSessionNodeExpBackOffBound(int schedulerGetSessionNodeExpBackOffBound) {
        this.schedulerGetSessionNodeExpBackOffBound = schedulerGetSessionNodeExpBackOffBound;
    }

    /**
     * Getter method for property <tt>schedulerConnectMetaTimeout</tt>.
     *
     * @return property value of schedulerConnectMetaTimeout
     */
    @Override
    public int getSchedulerConnectMetaTimeout() {
        return schedulerConnectMetaTimeout;
    }

    /**
     * Getter method for property <tt>schedulerConnectMetaFirstDelay</tt>.
     *
     * @return property value of schedulerConnectMetaFirstDelay
     */
    @Override
    public int getSchedulerConnectMetaFirstDelay() {
        return schedulerConnectMetaFirstDelay;
    }

    /**
     * Getter method for property <tt>schedulerConnectMetaExpBackOffBound</tt>.
     *
     * @return property value of schedulerConnectMetaExpBackOffBound
     */
    @Override
    public int getSchedulerConnectMetaExpBackOffBound() {
        return schedulerConnectMetaExpBackOffBound;
    }

    /**
     * Setter method for property <tt>schedulerConnectMetaTimeout</tt>.
     *
     * @param schedulerConnectMetaTimeout  value to be assigned to property schedulerConnectMetaTimeout
     */
    public void setSchedulerConnectMetaTimeout(int schedulerConnectMetaTimeout) {
        this.schedulerConnectMetaTimeout = schedulerConnectMetaTimeout;
    }

    /**
     * Setter method for property <tt>schedulerConnectMetaFirstDelay</tt>.
     *
     * @param schedulerConnectMetaFirstDelay  value to be assigned to property schedulerConnectMetaFirstDelay
     */
    public void setSchedulerConnectMetaFirstDelay(int schedulerConnectMetaFirstDelay) {
        this.schedulerConnectMetaFirstDelay = schedulerConnectMetaFirstDelay;
    }

    /**
     * Setter method for property <tt>schedulerConnectMetaExpBackOffBound</tt>.
     *
     * @param schedulerConnectMetaExpBackOffBound  value to be assigned to property schedulerConnectMetaExpBackOffBound
     */
    public void setSchedulerConnectMetaExpBackOffBound(int schedulerConnectMetaExpBackOffBound) {
        this.schedulerConnectMetaExpBackOffBound = schedulerConnectMetaExpBackOffBound;
    }

    @Override
    public int getSchedulerConnectDataTimeout() {
        return schedulerConnectDataTimeout;
    }

    /**
     * Setter method for property <tt>schedulerConnectDataTimeout</tt>.
     *
     * @param schedulerConnectDataTimeout  value to be assigned to property schedulerConnectDataTimeout
     */
    public void setSchedulerConnectDataTimeout(int schedulerConnectDataTimeout) {
        this.schedulerConnectDataTimeout = schedulerConnectDataTimeout;
    }

    @Override
    public int getSchedulerConnectDataFirstDelay() {
        return schedulerConnectDataFirstDelay;
    }

    /**
     * Setter method for property <tt>schedulerConnectDataFirstDelay</tt>.
     *
     * @param schedulerConnectDataFirstDelay  value to be assigned to property schedulerConnectDataFirstDelay
     */
    public void setSchedulerConnectDataFirstDelay(int schedulerConnectDataFirstDelay) {
        this.schedulerConnectDataFirstDelay = schedulerConnectDataFirstDelay;
    }

    @Override
    public int getSchedulerConnectDataExpBackOffBound() {
        return schedulerConnectDataExpBackOffBound;
    }

    /**
     * Setter method for property <tt>schedulerConnectDataExpBackOffBound</tt>.
     *
     * @param schedulerConnectDataExpBackOffBound  value to be assigned to property schedulerConnectDataExpBackOffBound
     */
    public void setSchedulerConnectDataExpBackOffBound(int schedulerConnectDataExpBackOffBound) {
        this.schedulerConnectDataExpBackOffBound = schedulerConnectDataExpBackOffBound;
    }

    /**
     * Getter method for property <tt>schedulerCleanInvolidClientTimeOut</tt>.
     *
     * @return property value of schedulerCleanInvolidClientTimeOut
     */
    public int getSchedulerCleanInvalidClientTimeOut() {
        return schedulerCleanInvalidClientTimeOut;
    }

    /**
     * Getter method for property <tt>schedulerCleanInvolidClientFirstDelay</tt>.
     *
     * @return property value of schedulerCleanInvolidClientFirstDelay
     */
    public int getSchedulerCleanInvalidClientFirstDelay() {
        return schedulerCleanInvalidClientFirstDelay;
    }

    /**
     * Getter method for property <tt>schedulerCleanInvolidClientBackOffBound</tt>.
     *
     * @return property value of schedulerCleanInvolidClientBackOffBound
     */
    public int getSchedulerCleanInvalidClientBackOffBound() {
        return schedulerCleanInvalidClientBackOffBound;
    }

    /**
     * Setter method for property <tt>schedulerCleanInvolidClientTimeOut</tt>.
     *
     * @param schedulerCleanInvalidClientTimeOut  value to be assigned to property schedulerCleanInvolidClientTimeOut
     */
    public void setSchedulerCleanInvalidClientTimeOut(int schedulerCleanInvalidClientTimeOut) {
        this.schedulerCleanInvalidClientTimeOut = schedulerCleanInvalidClientTimeOut;
    }

    /**
     * Setter method for property <tt>schedulerCleanInvolidClientFirstDelay</tt>.
     *
     * @param schedulerCleanInvalidClientFirstDelay  value to be assigned to property schedulerCleanInvolidClientFirstDelay
     */
    public void setSchedulerCleanInvalidClientFirstDelay(int schedulerCleanInvalidClientFirstDelay) {
        this.schedulerCleanInvalidClientFirstDelay = schedulerCleanInvalidClientFirstDelay;
    }

    /**
     * Setter method for property <tt>schedulerCleanInvolidClientBackOffBound</tt>.
     *
     * @param schedulerCleanInvalidClientBackOffBound  value to be assigned to property schedulerCleanInvolidClientBackOffBound
     */
    public void setSchedulerCleanInvalidClientBackOffBound(int schedulerCleanInvalidClientBackOffBound) {
        this.schedulerCleanInvalidClientBackOffBound = schedulerCleanInvalidClientBackOffBound;
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
     * @param stopPushSwitch  value to be assigned to property stopPushSwitch
     */
    @Override
    public void setStopPushSwitch(boolean stopPushSwitch) {
        this.stopPushSwitch = stopPushSwitch;
    }

    /**
     * Getter method for property <tt>beginDataFetchTask</tt>.
     *
     * @return property value of beginDataFetchTask
     */
    @Override
    public boolean isBeginDataFetchTask() {
        return beginDataFetchTask;
    }

    /**
     * Setter method for property <tt>beginDataFetchTask</tt>.
     *
     * @param beginDataFetchTask  value to be assigned to property beginDataFetchTask
     */
    @Override
    public void setBeginDataFetchTask(boolean beginDataFetchTask) {
        this.beginDataFetchTask = beginDataFetchTask;
    }

    public String getInvalidForeverZones() {
        return invalidForeverZones;
    }

    /**
     * Setter method for property <tt>invalidForeverZones</tt>.
     *
     * @param invalidForeverZones  value to be assigned to property invalidForeverZones
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
     * @param invalidIgnoreDataidRegex  value to be assigned to property invalidIgnoreDataidRegex
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
     * @param accessDataExecutorMinPoolSize  value to be assigned to property accessDataExecutorMinPoolSize
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
     * @param accessDataExecutorMaxPoolSize  value to be assigned to property accessDataExecutorMaxPoolSize
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
     * @param accessDataExecutorQueueSize  value to be assigned to property accessDataExecutorQueueSize
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
     * @param accessDataExecutorKeepAliveTime  value to be assigned to property accessDataExecutorKeepAliveTime
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
     * @param dataChangeExecutorMinPoolSize  value to be assigned to property dataChangeExecutorMinPoolSize
     */
    public void setDataChangeExecutorMinPoolSize(int dataChangeExecutorMinPoolSize) {
        this.dataChangeExecutorMinPoolSize = dataChangeExecutorMinPoolSize;
    }

    /**
     * Setter method for property <tt>dataChangeExecutorMaxPoolSize</tt>.
     *
     * @param dataChangeExecutorMaxPoolSize  value to be assigned to property dataChangeExecutorMaxPoolSize
     */
    public void setDataChangeExecutorMaxPoolSize(int dataChangeExecutorMaxPoolSize) {
        this.dataChangeExecutorMaxPoolSize = dataChangeExecutorMaxPoolSize;
    }

    /**
     * Setter method for property <tt>dataChangeExecutorQueueSize</tt>.
     *
     * @param dataChangeExecutorQueueSize  value to be assigned to property dataChangeExecutorQueueSize
     */
    public void setDataChangeExecutorQueueSize(int dataChangeExecutorQueueSize) {
        this.dataChangeExecutorQueueSize = dataChangeExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>dataChangeExecutorKeepAliveTime</tt>.
     *
     * @param dataChangeExecutorKeepAliveTime  value to be assigned to property dataChangeExecutorKeepAliveTime
     */
    public void setDataChangeExecutorKeepAliveTime(long dataChangeExecutorKeepAliveTime) {
        this.dataChangeExecutorKeepAliveTime = dataChangeExecutorKeepAliveTime;
    }

    /**
     * Getter method for property <tt>pushTaskExecutorMinPoolSize</tt>.
     *
     * @return property value of pushTaskExecutorMinPoolSize
     */
    @Override
    public int getPushTaskExecutorMinPoolSize() {
        return pushTaskExecutorMinPoolSize;
    }

    /**
     * Getter method for property <tt>pushTaskExecutorMaxPoolSize</tt>.
     *
     * @return property value of pushTaskExecutorMaxPoolSize
     */
    @Override
    public int getPushTaskExecutorMaxPoolSize() {
        return pushTaskExecutorMaxPoolSize;
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

    /**
     * Getter method for property <tt>pushTaskExecutorKeepAliveTime</tt>.
     *
     * @return property value of pushTaskExecutorKeepAliveTime
     */
    @Override
    public long getPushTaskExecutorKeepAliveTime() {
        return pushTaskExecutorKeepAliveTime;
    }

    /**
     * Setter method for property <tt>pushTaskExecutorMinPoolSize</tt>.
     *
     * @param pushTaskExecutorMinPoolSize  value to be assigned to property pushTaskExecutorMinPoolSize
     */
    public void setPushTaskExecutorMinPoolSize(int pushTaskExecutorMinPoolSize) {
        this.pushTaskExecutorMinPoolSize = pushTaskExecutorMinPoolSize;
    }

    /**
     * Setter method for property <tt>pushTaskExecutorMaxPoolSize</tt>.
     *
     * @param pushTaskExecutorMaxPoolSize  value to be assigned to property pushTaskExecutorMaxPoolSize
     */
    public void setPushTaskExecutorMaxPoolSize(int pushTaskExecutorMaxPoolSize) {
        this.pushTaskExecutorMaxPoolSize = pushTaskExecutorMaxPoolSize;
    }

    /**
     * Setter method for property <tt>pushTaskExecutorQueueSize</tt>.
     *
     * @param pushTaskExecutorQueueSize  value to be assigned to property pushTaskExecutorQueueSize
     */
    public void setPushTaskExecutorQueueSize(int pushTaskExecutorQueueSize) {
        this.pushTaskExecutorQueueSize = pushTaskExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>pushTaskExecutorKeepAliveTime</tt>.
     *
     * @param pushTaskExecutorKeepAliveTime  value to be assigned to property pushTaskExecutorKeepAliveTime
     */
    public void setPushTaskExecutorKeepAliveTime(long pushTaskExecutorKeepAliveTime) {
        this.pushTaskExecutorKeepAliveTime = pushTaskExecutorKeepAliveTime;
    }

    /**
     * Getter method for property <tt>defaultSessionExecutorMinPoolSize</tt>.
     *
     * @return property value of defaultSessionExecutorMinPoolSize
     */
    public int getDefaultSessionExecutorMinPoolSize() {
        return defaultSessionExecutorMinPoolSize;
    }

    /**
     * Getter method for property <tt>defaultSessionExecutorMaxPoolSize</tt>.
     *
     * @return property value of defaultSessionExecutorMaxPoolSize
     */
    public int getDefaultSessionExecutorMaxPoolSize() {
        return defaultSessionExecutorMaxPoolSize;
    }

    /**
     * Getter method for property <tt>defaultSessionExecutorKeepAliveTime</tt>.
     *
     * @return property value of defaultSessionExecutorKeepAliveTime
     */
    public long getDefaultSessionExecutorKeepAliveTime() {
        return defaultSessionExecutorKeepAliveTime;
    }

    /**
     * Setter method for property <tt>defaultSessionExecutorMinPoolSize</tt>.
     *
     * @param defaultSessionExecutorMinPoolSize  value to be assigned to property defaultSessionExecutorMinPoolSize
     */
    public void setDefaultSessionExecutorMinPoolSize(int defaultSessionExecutorMinPoolSize) {
        this.defaultSessionExecutorMinPoolSize = defaultSessionExecutorMinPoolSize;
    }

    /**
     * Setter method for property <tt>defaultSessionExecutorMaxPoolSize</tt>.
     *
     * @param defaultSessionExecutorMaxPoolSize  value to be assigned to property defaultSessionExecutorMaxPoolSize
     */
    public void setDefaultSessionExecutorMaxPoolSize(int defaultSessionExecutorMaxPoolSize) {
        this.defaultSessionExecutorMaxPoolSize = defaultSessionExecutorMaxPoolSize;
    }

    /**
     * Setter method for property <tt>defaultSessionExecutorKeepAliveTime</tt>.
     *
     * @param defaultSessionExecutorKeepAliveTime  value to be assigned to property defaultSessionExecutorKeepAliveTime
     */
    public void setDefaultSessionExecutorKeepAliveTime(long defaultSessionExecutorKeepAliveTime) {
        this.defaultSessionExecutorKeepAliveTime = defaultSessionExecutorKeepAliveTime;
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
     * @param connectClientExecutorMinPoolSize  value to be assigned to property connectClientExecutorMinPoolSize
     */
    public void setConnectClientExecutorMinPoolSize(int connectClientExecutorMinPoolSize) {
        this.connectClientExecutorMinPoolSize = connectClientExecutorMinPoolSize;
    }

    /**
     * Setter method for property <tt>connectClientExecutorMaxPoolSize</tt>.
     *
     * @param connectClientExecutorMaxPoolSize  value to be assigned to property connectClientExecutorMaxPoolSize
     */
    public void setConnectClientExecutorMaxPoolSize(int connectClientExecutorMaxPoolSize) {
        this.connectClientExecutorMaxPoolSize = connectClientExecutorMaxPoolSize;
    }

    /**
     * Setter method for property <tt>connectClientExecutorQueueSize</tt>.
     *
     * @param connectClientExecutorQueueSize  value to be assigned to property connectClientExecutorQueueSize
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
     * @param dataChangeFetchTaskMaxBufferSize  value to be assigned to property dataChangeFetchTaskMaxBufferSize
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
     * @param dataChangeFetchTaskWorkerSize  value to be assigned to property dataChangeFetchTaskWorkerSize
     */
    public void setDataChangeFetchTaskWorkerSize(int dataChangeFetchTaskWorkerSize) {
        this.dataChangeFetchTaskWorkerSize = dataChangeFetchTaskWorkerSize;
    }

    /**
     * Getter method for property <tt>userDataPushRetryWheelTicksSize</tt>.
     *
     * @return property value of userDataPushRetryWheelTicksSize
     */
    @Override
    public int getUserDataPushRetryWheelTicksSize() {
        return userDataPushRetryWheelTicksSize;
    }

    /**
     * Setter method for property <tt>userDataPushRetryWheelTicksSize</tt>.
     *
     * @param userDataPushRetryWheelTicksSize  value to be assigned to property userDataPushRetryWheelTicksSize
     */
    public void setUserDataPushRetryWheelTicksSize(int userDataPushRetryWheelTicksSize) {
        this.userDataPushRetryWheelTicksSize = userDataPushRetryWheelTicksSize;
    }

    /**
     * Getter method for property <tt>userDataPushRetryWheelTicksDuration</tt>.
     *
     * @return property value of userDataPushRetryWheelTicksDuration
     */
    @Override
    public int getUserDataPushRetryWheelTicksDuration() {
        return userDataPushRetryWheelTicksDuration;
    }

    /**
     * Setter method for property <tt>userDataPushRetryWheelTicksDuration</tt>.
     *
     * @param userDataPushRetryWheelTicksDuration  value to be assigned to property userDataPushRetryWheelTicksDuration
     */
    public void setUserDataPushRetryWheelTicksDuration(int userDataPushRetryWheelTicksDuration) {
        this.userDataPushRetryWheelTicksDuration = userDataPushRetryWheelTicksDuration;
    }

    /**
     * Getter method for property <tt>pushDataTaskRetryFirstDelay</tt>.
     *
     * @return property value of pushDataTaskRetryFirstDelay
     */
    @Override
    public int getPushDataTaskRetryFirstDelay() {
        return pushDataTaskRetryFirstDelay;
    }

    /**
     * Setter method for property <tt>pushDataTaskRetryFirstDelay</tt>.
     *
     * @param pushDataTaskRetryFirstDelay  value to be assigned to property pushDataTaskRetryFirstDelay
     */
    public void setPushDataTaskRetryFirstDelay(int pushDataTaskRetryFirstDelay) {
        this.pushDataTaskRetryFirstDelay = pushDataTaskRetryFirstDelay;
    }

    /**
     * Getter method for property <tt>pushDataTaskRetryIncrementDelay</tt>.
     *
     * @return property value of pushDataTaskRetryIncrementDelay
     */
    @Override
    public long getPushDataTaskRetryIncrementDelay() {
        return pushDataTaskRetryIncrementDelay;
    }

    /**
     * Getter method for property <tt>renewDatumWheelTicksSize</tt>.
     *
     * @return property value of renewDatumWheelTicksSize
     */
    public int getRenewDatumWheelTicksSize() {
        return renewDatumWheelTicksSize;
    }

    /**
     * Getter method for property <tt>renewDatumWheelTicksDuration</tt>.
     *
     * @return property value of renewDatumWheelTicksDuration
     */
    public int getRenewDatumWheelTicksDuration() {
        return renewDatumWheelTicksDuration;
    }

    /**
     * Getter method for property <tt>renewDatumWheelTaskDelaySec</tt>.
     *
     * @return property value of renewDatumWheelTaskDelaySec
     */
    public int getRenewDatumWheelTaskDelaySec() {
        return renewDatumWheelTaskDelaySec;
    }

    /**
     * Setter method for property <tt>renewDatumWheelTaskDelaySec </tt>.
     *
     * @param renewDatumWheelTaskDelaySec  value to be assigned to property renewDatumWheelTaskDelaySec
     */
    public void setRenewDatumWheelTaskDelaySec(int renewDatumWheelTaskDelaySec) {
        this.renewDatumWheelTaskDelaySec = renewDatumWheelTaskDelaySec;
    }

    /**
     * Getter method for property <tt>renewDatumWheelTaskRandomFirstDelaySec</tt>.
     *
     * @return property value of renewDatumWheelTaskRandomFirstDelaySec
     */
    public int getRenewDatumWheelTaskRandomFirstDelaySec() {
        return renewDatumWheelTaskRandomFirstDelaySec;
    }

    /**
     * Setter method for property <tt>renewDatumWheelTaskRandomFirstDelaySec </tt>.
     *
     * @param renewDatumWheelTaskRandomFirstDelaySec  value to be assigned to property renewDatumWheelTaskRandomFirstDelaySec
     */
    public void setRenewDatumWheelTaskRandomFirstDelaySec(int renewDatumWheelTaskRandomFirstDelaySec) {
        this.renewDatumWheelTaskRandomFirstDelaySec = renewDatumWheelTaskRandomFirstDelaySec;
    }

    /**
     * Setter method for property <tt>renewDatumWheelTicksSize</tt>.
     *
     * @param renewDatumWheelTicksSize  value to be assigned to property renewDatumWheelTicksSize
     */
    public void setRenewDatumWheelTicksSize(int renewDatumWheelTicksSize) {
        this.renewDatumWheelTicksSize = renewDatumWheelTicksSize;
    }

    /**
     * Setter method for property <tt>renewDatumWheelTicksDuration</tt>.
     *
     * @param renewDatumWheelTicksDuration  value to be assigned to property renewDatumWheelTicksDuration
     */
    public void setRenewDatumWheelTicksDuration(int renewDatumWheelTicksDuration) {
        this.renewDatumWheelTicksDuration = renewDatumWheelTicksDuration;
    }

    /**
     * Setter method for property <tt>pushDataTaskRetryIncrementDelay</tt>.
     *
     * @param pushDataTaskRetryIncrementDelay  value to be assigned to property pushDataTaskRetryIncrementDelay
     */
    public void setPushDataTaskRetryIncrementDelay(long pushDataTaskRetryIncrementDelay) {
        this.pushDataTaskRetryIncrementDelay = pushDataTaskRetryIncrementDelay;
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
     * @param blacklistPubDataIdRegex  value to be assigned to property blacklistPubDataIdRegex
     */
    public void setBlacklistPubDataIdRegex(String blacklistPubDataIdRegex) {
        this.blacklistPubDataIdRegex = blacklistPubDataIdRegex;
    }

    /**
     * Setter method for property <tt>blacklistSubDataIdRegex</tt>.
     *
     * @param blacklistSubDataIdRegex  value to be assigned to property blacklistSubDataIdRegex
     */
    public void setBlacklistSubDataIdRegex(String blacklistSubDataIdRegex) {
        this.blacklistSubDataIdRegex = blacklistSubDataIdRegex;
    }

    /**
     * Getter method for property <tt>pushTaskConfirmWaitTimeout</tt>.
     *
     * @return property value of pushTaskConfirmWaitTimeout
     */
    public long getPushTaskConfirmWaitTimeout() {
        return pushTaskConfirmWaitTimeout;
    }

    /**
     * Setter method for property <tt>pushTaskConfirmWaitTimeout</tt>.
     *
     * @param pushTaskConfirmWaitTimeout  value to be assigned to property pushTaskConfirmWaitTimeout
     */
    public void setPushTaskConfirmWaitTimeout(long pushTaskConfirmWaitTimeout) {
        this.pushTaskConfirmWaitTimeout = pushTaskConfirmWaitTimeout;
    }

    /**
     * Getter method for property <tt>pushTaskConfirmCheckWheelTicksSize</tt>.
     *
     * @return property value of pushTaskConfirmCheckWheelTicksSize
     */
    public int getPushTaskConfirmCheckWheelTicksSize() {
        return pushTaskConfirmCheckWheelTicksSize;
    }

    /**
     * Getter method for property <tt>pushTaskConfirmCheckWheelTicksDuration</tt>.
     *
     * @return property value of pushTaskConfirmCheckWheelTicksDuration
     */
    public int getPushTaskConfirmCheckWheelTicksDuration() {
        return pushTaskConfirmCheckWheelTicksDuration;
    }

    /**
     * Getter method for property <tt>pushTaskConfirmCheckExecutorQueueSize</tt>.
     *
     * @return property value of pushTaskConfirmCheckExecutorQueueSize
     */
    public int getPushTaskConfirmCheckExecutorQueueSize() {
        return pushTaskConfirmCheckExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>pushTaskConfirmCheckExecutorThreadSize</tt>.
     *
     * @return property value of pushTaskConfirmCheckExecutorThreadSize
     */
    public int getPushTaskConfirmCheckExecutorThreadSize() {
        return pushTaskConfirmCheckExecutorThreadSize;
    }

    /**
     * Setter method for property <tt>pushTaskConfirmCheckWheelTicksSize</tt>.
     *
     * @param pushTaskConfirmCheckWheelTicksSize  value to be assigned to property pushTaskConfirmCheckWheelTicksSize
     */
    public void setPushTaskConfirmCheckWheelTicksSize(int pushTaskConfirmCheckWheelTicksSize) {
        this.pushTaskConfirmCheckWheelTicksSize = pushTaskConfirmCheckWheelTicksSize;
    }

    /**
     * Setter method for property <tt>pushTaskConfirmCheckWheelTicksDuration</tt>.
     *
     * @param pushTaskConfirmCheckWheelTicksDuration  value to be assigned to property pushTaskConfirmCheckWheelTicksDuration
     */
    public void setPushTaskConfirmCheckWheelTicksDuration(int pushTaskConfirmCheckWheelTicksDuration) {
        this.pushTaskConfirmCheckWheelTicksDuration = pushTaskConfirmCheckWheelTicksDuration;
    }

    /**
     * Setter method for property <tt>pushTaskConfirmCheckExecutorQueueSize</tt>.
     *
     * @param pushTaskConfirmCheckExecutorQueueSize  value to be assigned to property pushTaskConfirmCheckExecutorQueueSize
     */
    public void setPushTaskConfirmCheckExecutorQueueSize(int pushTaskConfirmCheckExecutorQueueSize) {
        this.pushTaskConfirmCheckExecutorQueueSize = pushTaskConfirmCheckExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>pushTaskConfirmCheckExecutorThreadSize</tt>.
     *
     * @param pushTaskConfirmCheckExecutorThreadSize  value to be assigned to property pushTaskConfirmCheckExecutorThreadSize
     */
    public void setPushTaskConfirmCheckExecutorThreadSize(int pushTaskConfirmCheckExecutorThreadSize) {
        this.pushTaskConfirmCheckExecutorThreadSize = pushTaskConfirmCheckExecutorThreadSize;
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
     * @param publishDataExecutorMinPoolSize  value to be assigned to property publishDataExecutorMinPoolSize
     */
    public void setPublishDataExecutorMinPoolSize(int publishDataExecutorMinPoolSize) {
        this.publishDataExecutorMinPoolSize = publishDataExecutorMinPoolSize;
    }

    /**
     * Setter method for property <tt>publishDataExecutorMaxPoolSize</tt>.
     *
     * @param publishDataExecutorMaxPoolSize  value to be assigned to property publishDataExecutorMaxPoolSize
     */
    public void setPublishDataExecutorMaxPoolSize(int publishDataExecutorMaxPoolSize) {
        this.publishDataExecutorMaxPoolSize = publishDataExecutorMaxPoolSize;
    }

    /**
     * Setter method for property <tt>publishDataExecutorQueueSize</tt>.
     *
     * @param publishDataExecutorQueueSize  value to be assigned to property publishDataExecutorQueueSize
     */
    public void setPublishDataExecutorQueueSize(int publishDataExecutorQueueSize) {
        this.publishDataExecutorQueueSize = publishDataExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>publishDataExecutorKeepAliveTime</tt>.
     *
     * @param publishDataExecutorKeepAliveTime  value to be assigned to property publishDataExecutorKeepAliveTime
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
     * @param accessLimitRate  value to be assigned to property accessLimitRate
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
     * @param dataClientConnNum  value to be assigned to property dataClientConnNum
     */
    public void setDataClientConnNum(int dataClientConnNum) {
        this.dataClientConnNum = dataClientConnNum;
    }

    @Override
    public boolean isInvalidForeverZone(String zoneId) {

        String[] zoneNameArr = getInvalidForeverZones().split(";");
        if (invalidForeverZonesSet == null) {
            invalidForeverZonesSet = new HashSet<>();
            for (String str : zoneNameArr) {
                if (str.trim().length() > 0) {
                    invalidForeverZonesSet.add(str);
                }
            }
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
     * @param sessionSchedulerPoolSize  value to be assigned to property sessionSchedulerPoolSize
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
     * @param dataNodeExchangeForFetchDatumTimeOut  value to be assigned to property dataNodeExchangeForFetchDatumTimeOut
     */
    public void setDataNodeExchangeForFetchDatumTimeOut(int dataNodeExchangeForFetchDatumTimeOut) {
        this.dataNodeExchangeForFetchDatumTimeOut = dataNodeExchangeForFetchDatumTimeOut;
    }

    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
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