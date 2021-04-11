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

import org.junit.Assert;
import org.junit.Test;

public class ResultTest {
  @Test
  public void test() {
    Result result = new Result(false, "xxx");
    Assert.assertFalse(result.isSuccess());
    Assert.assertEquals(result.getMessage(), "xxx");

    result = Result.failed("yyy");
    Assert.assertFalse(result.isSuccess());
    Assert.assertEquals(result.getMessage(), "yyy");

    result = Result.success();
    Assert.assertTrue(result.isSuccess());
  }
}
