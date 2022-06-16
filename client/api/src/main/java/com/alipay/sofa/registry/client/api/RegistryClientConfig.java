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
package com.alipay.sofa.registry.client.api;

/**
 * The interface Registry client config.
 *
 * @author zhuoyu.sjw
 * @version $Id : RegistryClientConfig.java, v 0.1 2017-11-23 19:52 zhuoyu.sjw Exp $$
 */
public interface RegistryClientConfig {

  /**
   * Gets env.
   *
   * @return the env
   */
  String getEnv();

  /**
   * Gets instance id.
   *
   * @return the instance id
   */
  String getInstanceId();

  String getIp();

  /**
   * Gets cell.
   *
   * @return the cell
   */
  String getZone();

  /**
   * Gets registry endpoint.
   *
   * @return the registry endpoint
   */
  String getRegistryEndpoint();

  /**
   * Gets registry endpoint port.
   *
   * @return the registry endpoint port
   */
  int getRegistryEndpointPort();

  /**
   * Gets data center.
   *
   * @return the data center
   */
  String getDataCenter();

  /**
   * Gets app name.
   *
   * @return the app name
   */
  String getAppName();

  /**
   * Gets connect timeout.
   *
   * @return the connect timeout
   */
  int getConnectTimeout();

  /**
   * Gets socket timeout.
   *
   * @return the socket timeout
   */
  int getSocketTimeout();

  /**
   * Gets invoke timeout.
   *
   * @return the invoke timeout
   */
  int getInvokeTimeout();

  /**
   * Gets recheck interval.
   *
   * @return the recheck interval
   */
  int getRecheckInterval();

  /**
   * Gets observer thread core size.
   *
   * @return the observer thread core size
   */
  int getObserverThreadCoreSize();

  /**
   * Gets observer thread max size.
   *
   * @return the observer thread max size
   */
  int getObserverThreadMaxSize();

  /**
   * Gets observer thread queue length.
   *
   * @return the observer thread queue length
   */
  int getObserverThreadQueueLength();

  /**
   * Gets observer callback timeout.
   *
   * @return the observer callback timeout
   */
  int getObserverCallbackTimeout();

  /**
   * Gets sync config retry interval.
   *
   * @return the sync config retry interval
   */
  int getSyncConfigRetryInterval();

  /**
   * Gets access key.
   *
   * @return the access key
   */
  String getAccessKey();

  /**
   * Gets secret key.
   *
   * @return the secret key
   */
  String getSecretKey();

  /**
   * Gets algorithm.
   *
   * @return the algorithm
   */
  String getAlgorithm();

  /**
   * Gets auth cache interval.
   *
   * @return the auth cache interval
   */
  long getAuthCacheInterval();

  /**
   * Is event bus enable boolean.
   *
   * @return the boolean
   */
  boolean isEventBusEnable();
}
