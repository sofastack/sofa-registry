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
package com.alipay.sofa.registry.converter;

import com.alipay.sofa.registry.core.model.ScopeEnum;

/**
 * @author zhuoyu.sjw
 * @version $Id: ScopeEnumConverter.java, v 0.1 2018-03-01 21:08 zhuoyu.sjw Exp $$
 */
public class ScopeEnumConverter {

  /**
   * scope convert func
   *
   * @param name name
   * @return ScopeEnum
   */
  public static ScopeEnum convertToScope(String name) {
    if (ScopeEnum.contains(name)) {
      return ScopeEnum.valueOf(name);
    }
    return ScopeEnum.zone;
  }
}
