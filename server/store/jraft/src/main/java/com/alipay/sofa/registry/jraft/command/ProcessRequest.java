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
package com.alipay.sofa.registry.jraft.command;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author shangyu.wh
 * @version $Id: ProcessRequest.java, v 0.1 2018-05-21 17:36 shangyu.wh Exp $
 */
public class ProcessRequest implements Serializable {

  /** invoke method */
  private String methodName;

  /** invoke method arguments name */
  private String[] methodArgSigs;

  /** invoke method arguments object */
  private Object[] methodArgs;

  /** traget service unique name */
  private String serviceName;

  public ProcessRequest() {}

  /**
   * Getter method for property <tt>methodName</tt>.
   *
   * @return property value of methodName
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Setter method for property <tt>methodName</tt>.
   *
   * @param methodName value to be assigned to property methodName
   */
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  /**
   * Getter method for property <tt>methodArgSigs</tt>.
   *
   * @return property value of methodArgSigs
   */
  public String[] getMethodArgSigs() {
    return methodArgSigs;
  }

  /**
   * Setter method for property <tt>methodArgSigs</tt>.
   *
   * @param methodArgSigs value to be assigned to property methodArgSigs
   */
  public void setMethodArgSigs(String[] methodArgSigs) {
    this.methodArgSigs = methodArgSigs;
  }

  /**
   * Getter method for property <tt>methodArgs</tt>.
   *
   * @return property value of methodArgs
   */
  public Object[] getMethodArgs() {
    return methodArgs;
  }

  /**
   * Setter method for property <tt>methodArgs</tt>.
   *
   * @param methodArgs value to be assigned to property methodArgs
   */
  public void setMethodArgs(Object[] methodArgs) {
    this.methodArgs = methodArgs;
  }

  /**
   * Getter method for property <tt>serviceName</tt>.
   *
   * @return property value of serviceName
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * Setter method for property <tt>serviceName</tt>.
   *
   * @param serviceName value to be assigned to property serviceName
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ProcessRequest{");
    sb.append("methodName='").append(methodName).append('\'');
    sb.append(", methodArgSigs=").append(Arrays.toString(methodArgSigs));
    sb.append(", methodArgs=").append(Arrays.toString(methodArgs));
    sb.append(", serviceName='").append(serviceName).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
