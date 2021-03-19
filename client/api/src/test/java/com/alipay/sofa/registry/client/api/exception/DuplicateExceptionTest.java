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
package com.alipay.sofa.registry.client.api.exception;

import org.junit.Assert;
import org.junit.Test;

/** @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a> */
public class DuplicateExceptionTest {

  @Test
  public void testConstruct() {
    DuplicateException exception = new DuplicateException();
    Assert.assertNull(exception.getMessage());

    exception = new DuplicateException("1");
    Assert.assertEquals("1", exception.getMessage());

    RuntimeException runtimeException = new RuntimeException("xxx");
    exception = new DuplicateException(runtimeException);
    Assert.assertEquals("java.lang.RuntimeException: xxx", exception.getMessage());
    Assert.assertEquals(runtimeException, exception.getCause());

    exception = new DuplicateException("1", runtimeException);
    Assert.assertEquals("1", exception.getMessage());
    Assert.assertEquals(runtimeException, exception.getCause());
  }
}
