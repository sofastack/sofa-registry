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
package com.alipay.sofa.registry.client.provider;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_DATA_CENTER;
import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_ZONE;

/**
 * The type Default registry client config builder.
 *
 * @author zhuoyu.sjw
 * @version $Id : DefaultRegistryClientConfig.java, v 0.1 2017-11-23 20:12 zhuoyu.sjw Exp $$
 */
public class DefaultRegistryClientConfigBuilder {
  private String env;
  private String instanceId;
  private String ip;
  private String zone = DEFAULT_ZONE;
  private String registryEndpoint;
  private int registryEndpointPort = 9603;
  private String dataCenter = DEFAULT_DATA_CENTER;
  private String appName;
  private int connectTimeout = 3000;
  private int socketTimeout = 3000;
  private int invokeTimeout = 1000;
  private int recheckInterval = 500;
  private int observerThreadCoreSize = 5;
  private int observerThreadMaxSize = 10;
  private int observerThreadQueueLength = 1000;
  private int observerCallbackTimeout = 5000;
  private int syncConfigRetryInterval = 30000;
  private String accessKey;
  private String secretKey;
  private String algorithm = "HmacSHA256";
  private long authCacheInterval = 5 * 60 * 1000;
  private boolean eventBusEnable = true;

  /**
   * Start default registry client config builder.
   *
   * @return the default registry client config builder
   */
  public static DefaultRegistryClientConfigBuilder start() {
    return new DefaultRegistryClientConfigBuilder();
  }

  /**
   * Sets env.
   *
   * @param env the env
   * @return the env
   */
  public DefaultRegistryClientConfigBuilder setEnv(String env) {
    this.env = env;
    return this;
  }

  /**
   * Sets instance id.
   *
   * @param instanceId the instance id
   * @return the instance id
   */
  public DefaultRegistryClientConfigBuilder setInstanceId(String instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  /**
   * Sets ip.
   *
   * @param ip the ip
   */
  public DefaultRegistryClientConfigBuilder setIp(String ip) {
    this.ip = ip;
    return this;
  }

  /**
   * Sets zone.
   *
   * @param zone the zone
   * @return the zone
   */
  public DefaultRegistryClientConfigBuilder setZone(String zone) {
    this.zone = zone;
    return this;
  }

  /**
   * Sets registry endpoint.
   *
   * @param registryEndpoint the registry endpoint
   * @return the registry endpoint
   */
  public DefaultRegistryClientConfigBuilder setRegistryEndpoint(String registryEndpoint) {
    this.registryEndpoint = registryEndpoint;
    return this;
  }

  /**
   * Sets registry endpoint port.
   *
   * @param registryEndpointPort the registry endpoint port
   * @return the registry endpoint port
   */
  public DefaultRegistryClientConfigBuilder setRegistryEndpointPort(int registryEndpointPort) {
    this.registryEndpointPort = registryEndpointPort;
    return this;
  }

  /**
   * Sets data center.
   *
   * @param dataCenter the data center
   * @return the data center
   */
  public DefaultRegistryClientConfigBuilder setDataCenter(String dataCenter) {
    this.dataCenter = dataCenter;
    return this;
  }

  /**
   * Sets app name.
   *
   * @param appName the app name
   * @return the app name
   */
  public DefaultRegistryClientConfigBuilder setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  /**
   * Setter method for property <tt>observerThreadCoreSize</tt>.
   *
   * @param observerThreadCoreSize value to be assigned to property observerThreadCoreSize
   */
  public DefaultRegistryClientConfigBuilder setObserverThreadCoreSize(int observerThreadCoreSize) {
    this.observerThreadCoreSize = observerThreadCoreSize;
    return this;
  }

  /**
   * Setter method for property <tt>observerThreadMaxSize</tt>.
   *
   * @param observerThreadMaxSize value to be assigned to property observerThreadMaxSize
   */
  public DefaultRegistryClientConfigBuilder setObserverThreadMaxSize(int observerThreadMaxSize) {
    this.observerThreadMaxSize = observerThreadMaxSize;
    return this;
  }

  /**
   * Setter method for property <tt>observerThreadQueueLength</tt>.
   *
   * @param observerThreadQueueLength value to be assigned to property observerThreadQueueLength
   */
  public DefaultRegistryClientConfigBuilder setObserverThreadQueueLength(
      int observerThreadQueueLength) {
    this.observerThreadQueueLength = observerThreadQueueLength;
    return this;
  }

  /**
   * Setter method for property <tt>observerCallbackTimeout</tt>.
   *
   * @param observerCallbackTimeout value to be assigned to property observerCallbackTimeout
   */
  public DefaultRegistryClientConfigBuilder setObserverCallbackTimeout(
      int observerCallbackTimeout) {
    this.observerCallbackTimeout = observerCallbackTimeout;
    return this;
  }

  /**
   * Sets connect timeout.
   *
   * @param connectTimeout the connect timeout
   * @return the connect timeout
   */
  public DefaultRegistryClientConfigBuilder setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  /**
   * Sets socket timeout.
   *
   * @param socketTimeout the socket timeout
   * @return the socket timeout
   */
  public DefaultRegistryClientConfigBuilder setSocketTimeout(int socketTimeout) {
    this.socketTimeout = socketTimeout;
    return this;
  }

  /**
   * Sets invoke timeout.
   *
   * @param invokeTimeout the invoke timeout
   * @return the invoke timeout
   */
  public DefaultRegistryClientConfigBuilder setInvokeTimeout(int invokeTimeout) {
    this.invokeTimeout = invokeTimeout;
    return this;
  }

  /**
   * Sets recheck interval.
   *
   * @param recheckInterval the recheck interval
   * @return the recheck interval
   */
  public DefaultRegistryClientConfigBuilder setRecheckInterval(int recheckInterval) {
    this.recheckInterval = recheckInterval;
    return this;
  }

  /**
   * Setter method for property <tt>syncConfigRetryInterval</tt>.
   *
   * @param syncConfigRetryInterval value to be assigned to property syncConfigRetryInterval
   */
  public DefaultRegistryClientConfigBuilder setSyncConfigRetryInterval(
      int syncConfigRetryInterval) {
    this.syncConfigRetryInterval = syncConfigRetryInterval;
    return this;
  }

  /**
   * Setter method for property <tt>accessKey</tt>.
   *
   * @param accessKey value to be assigned to property accessKey
   */
  public DefaultRegistryClientConfigBuilder setAccessKey(String accessKey) {
    this.accessKey = accessKey;
    return this;
  }

  /**
   * Setter method for property <tt>secretKey</tt>.
   *
   * @param secretKey value to be assigned to property secretKey
   */
  public DefaultRegistryClientConfigBuilder setSecretKey(String secretKey) {
    this.secretKey = secretKey;
    return this;
  }

  /**
   * Setter method for property <tt>algorithm</tt>.
   *
   * @param algorithm value to be assigned to property algorithm
   */
  public DefaultRegistryClientConfigBuilder setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
    return this;
  }

  /**
   * Setter method for property <tt>authCacheInterval</tt>.
   *
   * @param authCacheInterval value to be assigned to property authCacheInterval
   */
  public DefaultRegistryClientConfigBuilder setAuthCacheInterval(long authCacheInterval) {
    this.authCacheInterval = authCacheInterval;
    return this;
  }

  /**
   * Setter method for property <tt>eventBusEnable</tt>.
   *
   * @param eventBusEnable value to be assigned to property eventBusEnable
   */
  public DefaultRegistryClientConfigBuilder setEventBusEnable(boolean eventBusEnable) {
    this.eventBusEnable = eventBusEnable;
    return this;
  }

  /**
   * Create default registry client config default registry client config.
   *
   * @return the default registry client config
   */
  public DefaultRegistryClientConfig build() {
    return new DefaultRegistryClientConfig(
        env,
        instanceId,
        ip,
        zone,
        registryEndpoint,
        registryEndpointPort,
        dataCenter,
        appName,
        connectTimeout,
        socketTimeout,
        invokeTimeout,
        recheckInterval,
        observerThreadCoreSize,
        observerThreadMaxSize,
        observerThreadQueueLength,
        observerCallbackTimeout,
        syncConfigRetryInterval,
        accessKey,
        secretKey,
        algorithm,
        authCacheInterval,
        eventBusEnable);
  }
}
