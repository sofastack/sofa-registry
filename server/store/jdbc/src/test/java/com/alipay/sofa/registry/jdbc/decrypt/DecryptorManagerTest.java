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
package com.alipay.sofa.registry.jdbc.decrypt;

import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DecryptorManagerTest extends AbstractH2DbTestBase {
  private final MockDecryptor decryptor = new MockDecryptor(true);
  private DecryptorManager decryptorManager;

  static class MockDecryptor implements Decryptor {
    private volatile boolean enabled;

    public MockDecryptor(boolean enabled) {
      this.enabled = enabled;
    }

    @Override
    public String name() {
      return "mock";
    }

    @Override
    public String type() {
      return DecryptConstants.JDBC_PASSWORD;
    }

    @Override
    public String decrypt(String cipher) {
      return "pwd:" + cipher;
    }

    @Override
    public boolean enabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  @Before
  public void before() {
    decryptorManager = applicationContext.getBean(DecryptorManager.class);
  }

  @Test
  public void test() {
    decryptorManager.setDecryptors(Lists.newArrayList(decryptor));
    Assert.assertEquals("pwd:123", decryptorManager.decrypt(DecryptConstants.JDBC_PASSWORD, "123"));
    decryptor.setEnabled(false);
    Assert.assertEquals("123", decryptorManager.decrypt(DecryptConstants.JDBC_PASSWORD, "123"));
    Assert.assertEquals("123", decryptorManager.decrypt(DecryptConstants.JDBC_PASSWORD, "123"));
  }
}
