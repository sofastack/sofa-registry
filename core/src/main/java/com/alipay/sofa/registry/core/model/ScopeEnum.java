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
package com.alipay.sofa.registry.core.model;

/**
 * The enum Scope enum.
 *
 * @author zhuoyu.sjw
 * @version $Id : ScopeEnum.java, v 0.1 2017-11-22 15:36 zhuoyu.sjw Exp $$
 */
public enum ScopeEnum {

  /** zone scope: only can receive pub list at same zone */
  zone,
  /** dataCenter scope: only can receive pub list at same dataCenter (multi zone) */
  dataCenter,
  /** global scope: can receive pub list at all dataCenter (multi zone) */
  global;

  public static boolean contains(String name) {
    for (ScopeEnum scopeEnum : values()) {
      if (scopeEnum.name().equals(name)) {
        return true;
      }
    }
    return false;
  }
}
