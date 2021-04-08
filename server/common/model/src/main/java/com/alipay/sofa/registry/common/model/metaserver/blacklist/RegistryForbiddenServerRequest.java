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
package com.alipay.sofa.registry.common.model.metaserver.blacklist;

import com.alipay.sofa.registry.common.model.metaserver.DataOperation;
import java.io.Serializable;

/**
 * @author chen.zhu
 *     <p>Mar 18, 2021
 */
public class RegistryForbiddenServerRequest implements Serializable {

  private DataOperation operation;

  private String ip;

  /**
   * Constructor.
   *
   * @param operation the operation
   * @param ip the ip
   */
  public RegistryForbiddenServerRequest(DataOperation operation, String ip) {
    this.operation = operation;
    this.ip = ip;
  }

  /**
   * Gets get operation.
   *
   * @return the get operation
   */
  public DataOperation getOperation() {
    return operation;
  }

  /**
   * Sets set operation.
   *
   * @param operation the operation
   * @return the set operation
   */
  public RegistryForbiddenServerRequest setOperation(DataOperation operation) {
    this.operation = operation;
    return this;
  }

  /**
   * Gets get ip.
   *
   * @return the get ip
   */
  public String getIp() {
    return ip;
  }

  /**
   * Sets set ip.
   *
   * @param ip the ip
   * @return the set ip
   */
  public RegistryForbiddenServerRequest setIp(String ip) {
    this.ip = ip;
    return this;
  }
}
