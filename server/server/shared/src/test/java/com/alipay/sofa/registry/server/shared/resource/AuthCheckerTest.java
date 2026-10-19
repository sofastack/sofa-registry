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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AuthCheckerTest {
  private static final String PROPERTY = "sofa.registry.management.auth-token";
  private String previousToken;

  @Before
  public void saveConfiguration() {
    previousToken = System.getProperty(PROPERTY);
  }

  @After
  public void restoreConfiguration() {
    if (previousToken == null) {
      System.clearProperty(PROPERTY);
    } else {
      System.setProperty(PROPERTY, previousToken);
    }
  }

  @Test
  public void rejectsLegacyTokenWhenUnconfigured() {
    System.setProperty(PROPERTY, "");
    Assert.assertFalse(AuthChecker.authCheck("6c62lk8dmQoE5B8X"));
    Assert.assertFalse(AuthChecker.authCheck(null));
    Assert.assertFalse(AuthChecker.authCheck(""));
  }

  @Test
  public void rejectsBlankConfiguration() {
    System.setProperty(PROPERTY, " \t");
    Assert.assertFalse(AuthChecker.authCheck(" \t"));
  }

  @Test
  public void acceptsOnlyTheConfiguredToken() {
    String token = UUID.randomUUID().toString();
    System.setProperty(PROPERTY, token);
    Assert.assertTrue(AuthChecker.authCheck(token));
    Assert.assertFalse(AuthChecker.authCheck(null));
    Assert.assertFalse(AuthChecker.authCheck(""));
    Assert.assertFalse(AuthChecker.authCheck("wrong-token"));
    Assert.assertFalse(AuthChecker.authCheck("6c62lk8dmQoE5B8X"));
    Assert.assertFalse(AuthChecker.authCheck(token + " "));
  }

  @Test
  public void rejectsRequestsWhenBothConfigurationSourcesAreAbsent() throws Exception {
    checkInProcess(null, null, "6c62lk8dmQoE5B8X", false);
    checkInProcess(null, null, "", false);
  }

  @Test
  public void readsEnvironmentWhenPropertyIsAbsent() throws Exception {
    String token = UUID.randomUUID().toString();
    checkInProcess(token, null, token, true);
    checkInProcess(token, null, "wrong-token", false);
    checkInProcess(" ", null, " ", false);
  }

  @Test
  public void explicitPropertyOverridesEnvironmentIncludingBlankValues() throws Exception {
    String environmentToken = UUID.randomUUID().toString();
    String propertyToken = UUID.randomUUID().toString();
    checkInProcess(environmentToken, propertyToken, propertyToken, true);
    checkInProcess(environmentToken, propertyToken, environmentToken, false);
    checkInProcess(environmentToken, "", environmentToken, false);
  }

  private static void checkInProcess(
      String environmentToken, String propertyToken, String requestToken, boolean expected)
      throws Exception {
    List<String> command = new ArrayList<>();
    command.add(new File(System.getProperty("java.home"), "bin/java").getPath());
    if (propertyToken != null) {
      command.add("-D" + PROPERTY + "=" + propertyToken);
    }
    command.addAll(
        Arrays.asList(
            "-cp",
            System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
            AuthProbe.class.getName(),
            requestToken,
            Boolean.toString(expected)));
    ProcessBuilder builder = new ProcessBuilder(command).inheritIO();
    if (environmentToken == null) {
      builder.environment().remove("SOFA_REGISTRY_MANAGEMENT_AUTH_TOKEN");
    } else {
      builder.environment().put("SOFA_REGISTRY_MANAGEMENT_AUTH_TOKEN", environmentToken);
    }
    Process process = builder.start();
    try {
      Assert.assertTrue("Authentication probe timed out", process.waitFor(15, TimeUnit.SECONDS));
      Assert.assertEquals("Unexpected authentication result", 0, process.exitValue());
    } finally {
      process.destroy();
    }
  }

  public static class AuthProbe {
    public static void main(String[] args) {
      Assert.assertEquals(Boolean.parseBoolean(args[1]), AuthChecker.authCheck(args[0]));
    }
  }
}
