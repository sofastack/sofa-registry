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
package com.alipay.sofa.registry.cache;

import com.alipay.sofa.registry.util.ConcurrentUtils;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class ConsecutiveSuccessTest {

  @Test
  public void testCheck() {
    ConsecutiveSuccess s = new ConsecutiveSuccess(3, 50);
    s.success();
    s.success();
    Assert.assertFalse(s.check());
    s.success();
    Assert.assertTrue(s.check());
    s.fail();
    Assert.assertFalse(s.check());
    s.fail();
    s.fail();
    s.fail();
    Assert.assertFalse(s.check());
    s.success();
    s.success();
    s.success();
    Assert.assertTrue(s.check());
    ConcurrentUtils.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
    Assert.assertFalse(s.check());
    s.success();
    s.success();
    s.success();
    s.clear();
    Assert.assertFalse(s.check());
    Assert.assertFalse(s.check());
  }
}
