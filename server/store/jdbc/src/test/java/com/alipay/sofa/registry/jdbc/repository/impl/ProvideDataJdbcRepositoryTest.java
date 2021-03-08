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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProvideDataJdbcRepositoryTest extends AbstractH2DbTestBase {

    @Autowired
    private ProvideDataRepository provideDataJdbcRepository;

    @Test
    public void testPut() {
        provideDataJdbcRepository.put(getDc(), "key", "val");
        Assert.assertEquals("val", provideDataJdbcRepository.get(getDc(), "key").getEntity());
    }

    @Test
    public void testRemove() {
        provideDataJdbcRepository.put(getDc(), "key", "val");
        Assert.assertEquals("val", provideDataJdbcRepository.get(getDc(), "key").getEntity());
        provideDataJdbcRepository.remove(getDc(), "key");
        Assert.assertEquals(OperationStatus.NOTFOUND, provideDataJdbcRepository.get(getDc(), "key").getOperationStatus());
    }
}