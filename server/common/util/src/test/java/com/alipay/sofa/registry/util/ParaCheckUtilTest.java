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
package com.alipay.sofa.registry.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.TestCase.fail;

/**
 * @author xuanbei
 * @since 2018/12/28
 */
public class ParaCheckUtilTest {
    @Test
    public void testCheckNotNull() {
        ParaCheckUtil.checkNotNull("zhangsan", "name");
        try {
            ParaCheckUtil.checkNotNull(null, "name");
            fail("cannot access here.");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertEquals("name is not allowed to be null", e.getMessage());
        }
    }

    @Test
    public void testCheckNotBlank() {
        ParaCheckUtil.checkNotBlank("zhangsan", "name");
        try {
            ParaCheckUtil.checkNotBlank("", "name");
            fail("cannot access here.");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertEquals("name is not allowed to be blank", e.getMessage());
        }
    }

    @Test
    public void testCheckNotEmpty() {
        ParaCheckUtil.checkNotEmpty(Arrays.asList("zhangsan", "lisi"), "names");
        try {
            ParaCheckUtil.checkNotEmpty(new ArrayList<>(), "names");
            fail("cannot access here.");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertEquals("names is not allowed to be empty", e.getMessage());
        }
    }
}
