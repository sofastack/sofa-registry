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
package com.alipay.sofa.registry.server.meta.resource;

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.CIRCUIT_BREAKER_DATA_ID;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.console.CircuitBreakerData;
import com.alipay.sofa.registry.common.model.console.CircuitBreakerData.CircuitBreakOption;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author xiaojian.xj
 * @version : CircuitBreakerResourcesTest.java, v 0.1 2022年01月17日 21:27 xiaojian.xj Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class CircuitBreakerResourcesTest extends AbstractMetaServerTestBase {

  @InjectMocks private CircuitBreakerResources circuitBreakerResources;

  @Spy private InMemoryProvideDataRepo provideDataService;

  @Test
  public void testSwitch() {
    // 1.add
    CommonResponse add =
        circuitBreakerResources.add(CircuitBreakOption.STOP_PUSH.getOption(), "1.1.1.1;2.2.2.2");
    Assert.assertTrue(add.isSuccess());
    add = circuitBreakerResources.add(CircuitBreakOption.STOP_PUSH.getOption(), "3.3.3.3");
    Assert.assertTrue(add.isSuccess());

    CircuitBreakerData query = query();
    Assert.assertNull(query.isAddressSwitch());
    Assert.assertEquals(3, query.getAddress().size());
    Assert.assertEquals(Sets.newHashSet("1.1.1.1", "2.2.2.2", "3.3.3.3"), query.getAddress());

    // 2.remove
    CommonResponse remove =
        circuitBreakerResources.remove(CircuitBreakOption.STOP_PUSH.getOption(), "2.2.2.2");
    Assert.assertTrue(remove.isSuccess());
    query = query();
    Assert.assertEquals(2, query.getAddress().size());
    Assert.assertEquals(Sets.newHashSet("1.1.1.1", "3.3.3.3"), query.getAddress());
    Assert.assertNull(query.isAddressSwitch());

    // 3.open
    CommonResponse open = circuitBreakerResources.switchOpen();
    Assert.assertTrue(open.isSuccess());
    query = query();
    Assert.assertEquals(2, query.getAddress().size());
    Assert.assertEquals(Sets.newHashSet("1.1.1.1", "3.3.3.3"), query.getAddress());
    Assert.assertTrue(query.isAddressSwitch());

    // 4.close
    CommonResponse close = circuitBreakerResources.switchClose();
    Assert.assertTrue(close.isSuccess());
    query = query();
    Assert.assertEquals(2, query.getAddress().size());
    Assert.assertEquals(Sets.newHashSet("1.1.1.1", "3.3.3.3"), query.getAddress());
    Assert.assertFalse(query.isAddressSwitch());

    // 5.add
    add = circuitBreakerResources.add(CircuitBreakOption.STOP_PUSH.getOption(), "4.4.4.4");
    Assert.assertTrue(add.isSuccess());

    query = query();
    Assert.assertFalse(query.isAddressSwitch());
    Assert.assertEquals(3, query.getAddress().size());
    Assert.assertEquals(Sets.newHashSet("1.1.1.1", "3.3.3.3", "4.4.4.4"), query.getAddress());
  }

  private CircuitBreakerData query() {
    DBResponse<PersistenceData> response =
        provideDataService.queryProvideData(CIRCUIT_BREAKER_DATA_ID);
    Assert.assertEquals(response.getOperationStatus(), OperationStatus.SUCCESS);
    CircuitBreakerData read =
        JsonUtils.read(response.getEntity().getData(), CircuitBreakerData.class);
    return read;
  }
}
