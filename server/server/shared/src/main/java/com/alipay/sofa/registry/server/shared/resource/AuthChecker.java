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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.apache.commons.lang.StringUtils;

/**
 * @author xiaojian.xj
 * @version : AuthChecker.java, v 0.1 2021年10月18日 14:22 xiaojian.xj Exp $
 */
public final class AuthChecker {

  /** JVM property takes precedence, including an explicitly empty (disabled) value. */
  private static final String AUTH_TOKEN_PROPERTY = "sofa.registry.management.auth-token";

  private static final String AUTH_TOKEN_ENV = "SOFA_REGISTRY_MANAGEMENT_AUTH_TOKEN";

  public static boolean authCheck(String token) {
    String expectedToken = System.getProperty(AUTH_TOKEN_PROPERTY);
    if (expectedToken == null) {
      expectedToken = System.getenv(AUTH_TOKEN_ENV);
    }
    // Management APIs are disabled until a deployment-specific secret is supplied.
    if (StringUtils.isBlank(expectedToken) || StringUtils.isBlank(token)) {
      return false;
    }
    return MessageDigest.isEqual(
        expectedToken.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8));
  }
}
