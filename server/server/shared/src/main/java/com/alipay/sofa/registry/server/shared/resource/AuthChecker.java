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
package com.alipay.sofa.registry.server.shared.resource;

import org.apache.commons.lang.StringUtils;

/**
 * @author xiaojian.xj
 * @version : AuthChecker.java, v 0.1 2021年10月18日 14:22 xiaojian.xj Exp $
 */
public final class AuthChecker {

  private static final String AUTH_TOKEN = "6c62lk8dmQoE5B8X";

  public static boolean authCheck(String token) {
    return StringUtils.equals(AUTH_TOKEN, token);
  }
}
