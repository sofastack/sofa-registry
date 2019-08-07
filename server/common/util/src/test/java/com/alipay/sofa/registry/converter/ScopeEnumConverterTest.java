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
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xuanbei
 * @since 2018/12/28
 */
public class ScopeEnumConverterTest {
    @Test
    public void doTest() {
        Assert.assertEquals(ScopeEnum.zone, new ScopeEnumConverter().convertToScope("zone"));
        Assert.assertEquals(ScopeEnum.dataCenter, ScopeEnumConverter.convertToScope("dataCenter"));
        Assert.assertEquals(ScopeEnum.global, ScopeEnumConverter.convertToScope("global"));
        Assert.assertEquals(ScopeEnum.zone, ScopeEnumConverter.convertToScope("other value"));
    }
}
