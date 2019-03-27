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
import java.util.Collection;

/**
 * @author xuanbei
 * @since 2018/12/28
 */
public class CollectionUtilsTest {
    Collection<String> stringCollection = new ArrayList<>(Arrays.asList("zhangsan", "lisi",
                                            "wangwu", "zhaoliu", "sunqi", "zhouba"));

    @Test
    public void testGetRandom() {
        boolean allValueSame = true;
        String firstValue = null;
        for (int i = 0; i < 10; i++) {
            String radomeValue = i % 2 == 0 ? CollectionUtils.getRandom(stringCollection).get()
                : new CollectionUtils().getRandom(stringCollection).get();
            Assert.assertTrue(stringCollection.contains(radomeValue));
            if (firstValue == null) {
                firstValue = radomeValue;
            } else if (!radomeValue.equals(firstValue)) {
                allValueSame = false;
            }
        }
        Assert.assertFalse(allValueSame);
    }
}
