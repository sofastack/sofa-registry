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

import com.alipay.sofa.registry.client.api.RegistryClientConfig;

/**
 * The type Default registry client config.
 *
 * @author zhuoyu.sjw
 * @version $Id : DefaultRegistryClientConfig.java, v 0.1 2017-11-23 20:11 zhuoyu.sjw Exp $$
 */
public class DefaultRegistryClientConfig implements RegistryClientConfig {

  /** */
  private String env;

  /** */
  private String instanceId;

  private String ip;

  /** */
  private String zone;

  /** */
  private String registryEndpoint;

  /** */
  private int registryEndpointPort;

  /** */
  private String dataCenter;

  /** */
  private String appName;

  /** */
  private int connectTimeout;

  /** */
  private int socketTimeout;

  /** */
  private int invokeTimeout;

  /** */
  private int recheckInterval;

  /** */
  private int observerThreadCoreSize;

  /** */
  private int observerThreadMaxSize;

  /** */
  private int observerThreadQueueLength;

  /** */
  private int observerCallbackTimeout;

  /** */
  private int syncConfigRetryInterval;

  /** */
  private String accessKey;

  /** */
  private String secretKey;

  /** */
  private String algorithm;

  /** */
  private long authCacheInterval;

  /** */
  private boolean eventBusEnable;

  /**
   * Instantiates a new Default registry client config.
   *
   * @param env the env
   * @param instanceId the instance id
   * @param ip the ip
   * @param zone the zone
   * @param registryEndpoint the registry endpoint
   * @param registryEndpointPort the registry endpoint port
   * @param dataCenter the data center
   * @param appName the app name
   * @param connectTimeout the connect timeout
   * @param socketTimeout the socket timeout
   * @param invokeTimeout the invoke timeout
   * @param recheckInterval the recheck interval
   * @param observerThreadCoreSize the observer thread core size
   * @param observerThreadMaxSize the observer thread max size
   * @param observerThreadQueueLength the observer thread queue length
   * @param observerCallbackTimeout the observer callback timeout
   * @param syncConfigRetryInterval the sync config retry interval
   * @param accessKey the access key
   * @param secretKey the secret key
   * @param algorithm the algorithm
   * @param authCacheInterval the auth cache interval
   */
  public DefaultRegistryClientConfig(
      String env,
      String instanceId,
      String ip,
      String zone,
      String registryEndpoint,
      int registryEndpointPort,
      String dataCenter,
      String appName,
      int connectTimeout,
      int socketTimeout,
      int invokeTimeout,
      int recheckInterval,
      int observerThreadCoreSize,
      int observerThreadMaxSize,
      int observerThreadQueueLength,
      int observerCallbackTimeout,
      int syncConfigRetryInterval,
      String accessKey,
      String secretKey,
      String algorithm,
      long authCacheInterval,
      boolean eventBusEnable) {
    this.env = env;
    this.instanceId = instanceId;
    this.ip = ip;
    this.zone = zone;
    this.registryEndpoint = registryEndpoint;
    this.registryEndpointPort = registryEndpointPort;
    this.dataCenter = dataCenter;
    this.appName = appName;
    this.connectTimeout = connectTimeout;
    this.socketTimeout = socketTimeout;
    this.invokeTimeout = invokeTimeout;
    this.recheckInterval = recheckInterval;
    this.observerThreadCoreSize = observerThreadCoreSize;
    this.observerThreadMaxSize = observerThreadMaxSize;
    this.observerThreadQueueLength = observerThreadQueueLength;
    this.observerCallbackTimeout = observerCallbackTimeout;
    this.syncConfigRetryInterval = syncConfigRetryInterval;
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.algorithm = algorithm;
    this.authCacheInterval = authCacheInterval;
    this.eventBusEnable = eventBusEnable;
  }

  /**
   * Getter method for property <tt>env</tt>.
   *
   * @return property value of env
   */
  @Override
  public String getEnv() {
    return env;
  }

  /**
   * Getter method for property <tt>instanceId</tt>.
   *
   * @return property value of instanceId
   */
  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public String getIp() {
    return ip;
  }

  /**
   * Getter method for property <tt>zone</tt>.
   *
   * @return property value of zone
   */
  @Override
  public String getZone() {
    return zone;
  }

  /**
   * Getter method for property <tt>registryEndpoint</tt>.
   *
   * @return property value of registryEndpoint
   */
  @Override
  public String getRegistryEndpoint() {
    return registryEndpoint;
  }

  /**
   * Getter method for property <tt>registryEndpointPort</tt>.
   *
   * @return property value of registryEndpointPort
   */
  @Override
  public int getRegistryEndpointPort() {
    return registryEndpointPort;
  }

  /**
   * Getter method for property <tt>dataCenter</tt>.
   *
   * @return property value of dataCenter
   */
  @Override
  public String getDataCenter() {
    return dataCenter;
  }

  /**
   * Getter method for property <tt>appName</tt>.
   *
   * @return property value of appName
   */
  @Override
  public String getAppName() {
    return appName;
  }

  /**
   * Getter method for property <tt>connectTimeout</tt>.
   *
   * @return property value of connectTimeout
   */
  @Override
  public int getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Getter method for property <tt>socketTimeout</tt>.
   *
   * @return property value of socketTimeout
   */
  @Override
  public int getSocketTimeout() {
    return socketTimeout;
  }

  /**
   * Getter method for property <tt>invokeTimeout</tt>.
   *
   * @return property value of invokeTimeout
   */
  @Override
  public int getInvokeTimeout() {
    return invokeTimeout;
  }

  /**
   * Getter method for property <tt>recheckInterval</tt>.
   *
   * @return property value of recheckInterval
   */
  @Override
  public int getRecheckInterval() {
    return recheckInterval;
  }

  /**
   * Getter method for property <tt>observerThreadCoreSize</tt>.
   *
   * @return property value of observerThreadCoreSize
   */
  @Override
  public int getObserverThreadCoreSize() {
    return observerThreadCoreSize;
  }

  /**
   * Getter method for property <tt>observerThreadMaxSize</tt>.
   *
   * @return property value of observerThreadMaxSize
   */
  @Override
  public int getObserverThreadMaxSize() {
    return observerThreadMaxSize;
  }

  /**
   * Getter method for property <tt>observerThreadQueueLength</tt>.
   *
   * @return property value of observerThreadQueueLength
   */
  @Override
  public int getObserverThreadQueueLength() {
    return observerThreadQueueLength;
  }

  /**
   * Getter method for property <tt>observerCallbackTimeout</tt>.
   *
   * @return property value of observerCallbackTimeout
   */
  @Override
  public int getObserverCallbackTimeout() {
    return observerCallbackTimeout;
  }

  /**
   * Getter method for property <tt>syncConfigRetryInterval</tt>.
   *
   * @return property value of syncConfigRetryInterval
   */
  @Override
  public int getSyncConfigRetryInterval() {
    return syncConfigRetryInterval;
  }

  /**
   * Getter method for property <tt>accessKey</tt>.
   *
   * @return property value of accessKey
   */
  @Override
  public String getAccessKey() {
    return accessKey;
  }

  /**
   * Getter method for property <tt>secretKey</tt>.
   *
   * @return property value of secretKey
   */
  @Override
  public String getSecretKey() {
    return secretKey;
  }

  /**
   * Getter method for property <tt>algorithm</tt>.
   *
   * @return property value of algorithm
   */
  @Override
  public String getAlgorithm() {
    return algorithm;
  }

  /**
   * Setter method for property <tt>algorithm</tt>.
   *
   * @param algorithm value to be assigned to property algorithm
   */
  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  /**
   * Getter method for property <tt>authCacheInterval</tt>.
   *
   * @return property value of authCacheInterval
   */
  @Override
  public long getAuthCacheInterval() {
    return authCacheInterval;
  }

  /**
   * Setter method for property <tt>authCacheInterval</tt>.
   *
   * @param authCacheInterval value to be assigned to property authCacheInterval
   */
  public void setAuthCacheInterval(long authCacheInterval) {
    this.authCacheInterval = authCacheInterval;
  }

  /**
   * Getter method for property <tt>eventBusEnable</tt>.
   *
   * @return property value of eventBusEnable
   */
  @Override
  public boolean isEventBusEnable() {
    return eventBusEnable;
  }

  /**
   * Setter method for property <tt>eventBusEnable</tt>.
   *
   * @param eventBusEnable value to be assigned to property eventBusEnable
   */
  public void setEventBusEnable(boolean eventBusEnable) {
    this.eventBusEnable = eventBusEnable;
  }

  /**
   * Equals boolean.
   *
   * @param o the o
   * @return the boolean
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DefaultRegistryClientConfig)) {
      return false;
    }

    DefaultRegistryClientConfig that = (DefaultRegistryClientConfig) o;

    if (registryEndpointPort != that.registryEndpointPort) {
      return false;
    }
    if (env != null ? !env.equals(that.env) : that.env != null) {
      return false;
    }
    if (instanceId != null ? !instanceId.equals(that.instanceId) : that.instanceId != null) {
      return false;
    }
    if (ip != null ? !ip.equals(that.ip) : that.ip != null) {
      return false;
    }
    if (zone != null ? !zone.equals(that.zone) : that.zone != null) {
      return false;
    }
    if (registryEndpoint != null
        ? !registryEndpoint.equals(that.registryEndpoint)
        : that.registryEndpoint != null) {
      return false;
    }
    if (dataCenter != null ? !dataCenter.equals(that.dataCenter) : that.dataCenter != null) {
      return false;
    }
    return appName != null ? appName.equals(that.appName) : that.appName == null;
  }

  /**
   * Hash code int.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    int result = env != null ? env.hashCode() : 0;
    result = 31 * result + (instanceId != null ? instanceId.hashCode() : 0);
    result = 31 * result + (ip != null ? ip.hashCode() : 0);
    result = 31 * result + (zone != null ? zone.hashCode() : 0);
    result = 31 * result + (registryEndpoint != null ? registryEndpoint.hashCode() : 0);
    result = 31 * result + registryEndpointPort;
    result = 31 * result + (dataCenter != null ? dataCenter.hashCode() : 0);
    result = 31 * result + (appName != null ? appName.hashCode() : 0);
    return result;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "DefaultRegistryClientConfig{"
        + "env='"
        + env
        + '\''
        + ", instanceId='"
        + instanceId
        + ", ip='"
        + ip
        + '\''
        + ", zone='"
        + zone
        + '\''
        + ", registryEndpoint='"
        + registryEndpoint
        + '\''
        + ", registryEndpointPort="
        + registryEndpointPort
        + ", dataCenter='"
        + dataCenter
        + '\''
        + ", appName='"
        + appName
        + '\''
        + ", connectTimeout="
        + connectTimeout
        + ", socketTimeout="
        + socketTimeout
        + ", invokeTimeout="
        + invokeTimeout
        + ", recheckInterval="
        + recheckInterval
        + ", observerThreadCoreSize="
        + observerThreadCoreSize
        + ", observerThreadMaxSize="
        + observerThreadMaxSize
        + ", observerThreadQueueLength="
        + observerThreadQueueLength
        + ", observerCallbackTimeout="
        + observerCallbackTimeout
        + ", syncConfigRetryInterval="
        + syncConfigRetryInterval
        + ", accessKey='"
        + accessKey
        + '\''
        + ", secretKey='"
        + secretKey
        + '\''
        + ", algorithm='"
        + algorithm
        + '\''
        + ", authCacheInterval="
        + authCacheInterval
        + ", eventBusEnable="
        + eventBusEnable
        + '}';
  }
}
