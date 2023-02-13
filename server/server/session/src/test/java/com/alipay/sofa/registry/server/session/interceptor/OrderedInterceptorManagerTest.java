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
package com.alipay.sofa.registry.server.session.interceptor;

import com.alipay.sofa.registry.common.model.store.StoreData;
import com.alipay.sofa.registry.exception.InterceptorExecutionException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** */
public class OrderedInterceptorManagerTest {

  private RegisterInvokeData registerInvokeData;

  @Before
  public void init() {
    registerInvokeData =
        new RegisterInvokeData(
            new StoreData() {
              @Override
              public DataType getDataType() {
                return null;
              }

              @Override
              public Object getId() {
                return "dataId";
              }
            },
            null);
  }

  @Test
  public void testOrder() {
    OrderedInterceptorManager orderedInterceptorManager = new OrderedInterceptorManager();
    // empty interceptors
    orderedInterceptorManager.executeInterceptors(registerInvokeData);

    List<Integer> result = new ArrayList<>();
    orderedInterceptorManager.addInterceptor(
        new Interceptor() {
          @Override
          public boolean process(RegisterInvokeData registerInvokeData)
              throws InterceptorExecutionException {
            result.add(order());
            return true;
          }

          @Override
          public int order() {
            return 2;
          }
        });

    orderedInterceptorManager.addInterceptor(
        new Interceptor() {
          @Override
          public boolean process(RegisterInvokeData registerInvokeData)
              throws InterceptorExecutionException {
            result.add(order());
            return true;
          }

          @Override
          public int order() {
            return 1;
          }
        });
    orderedInterceptorManager.executeInterceptors(registerInvokeData);
    Assert.assertEquals(1, result.get(0).intValue());
    Assert.assertEquals(2, result.get(1).intValue());
  }

  @Test
  public void testEarlyTermination() {
    OrderedInterceptorManager orderedInterceptorManager = new OrderedInterceptorManager();
    List<Integer> result = new ArrayList<>();
    orderedInterceptorManager.addInterceptor(
        new Interceptor() {
          @Override
          public boolean process(RegisterInvokeData registerInvokeData)
              throws InterceptorExecutionException {
            result.add(order());
            return true;
          }

          @Override
          public int order() {
            return 2;
          }
        });

    orderedInterceptorManager.addInterceptor(
        new Interceptor() {
          @Override
          public boolean process(RegisterInvokeData registerInvokeData)
              throws InterceptorExecutionException {
            result.add(order());
            return false;
          }

          @Override
          public int order() {
            return 1;
          }
        });
    orderedInterceptorManager.executeInterceptors(registerInvokeData);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals(1, result.get(0).intValue());
  }

  @Test
  public void testExceptionTermination() {
    OrderedInterceptorManager orderedInterceptorManager = new OrderedInterceptorManager();
    List<Integer> result = new ArrayList<>();
    orderedInterceptorManager.addInterceptor(
        new Interceptor() {
          @Override
          public boolean process(RegisterInvokeData registerInvokeData)
              throws InterceptorExecutionException {
            result.add(order());
            return true;
          }

          @Override
          public int order() {
            return 2;
          }
        });

    orderedInterceptorManager.addInterceptor(
        new Interceptor() {
          @Override
          public boolean process(RegisterInvokeData registerInvokeData)
              throws InterceptorExecutionException {
            result.add(order());
            throw new InterceptorExecutionException("test exception");
          }

          @Override
          public int order() {
            return 1;
          }
        });
    Exception exception = null;
    try {
      orderedInterceptorManager.executeInterceptors(registerInvokeData);
    } catch (Exception e) {
      exception = e;
    }
    Assert.assertNotNull(exception);
    Assert.assertTrue(exception instanceof InterceptorExecutionException);
  }
}
