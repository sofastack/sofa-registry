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
package com.alipay.sofa.registry.server.session;

import static junit.framework.Assert.*;

import com.alipay.sofa.registry.common.model.wrapper.Wrapper;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInterceptor;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInvocation;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.wrapper.WrapperInterceptorManager;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author shangyu.wh
 * @version 1.0: WrapperInvocationTest.java, v 0.1 2019-06-18 17:41 shangyu.wh Exp $
 */
public class WrapperInvocationTest {

  private static final Logger logger = LoggerFactory.getLogger(WrapperInvocationTest.class);

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testWrapperInterceptorException() throws Exception {
    thrown.expect(IllegalAccessException.class);
    thrown.expectMessage("test exception!");
    testAll("EXP");
  }

  @Test
  public void testWrapperInterceptorProceed() throws Exception {
    assertEquals(testAll("Proceed"), 1);
  }

  @Test
  public void testWrapperInterceptorAllRun() throws Exception {
    assertEquals(testAll("AllRun"), 2);
  }

  private int testAll(String input) throws Exception {

    AtomicInteger ret = new AtomicInteger(0);

    WrapperInterceptorManager wrapperInterceptorManager = new WrapperInterceptorManager();

    wrapperInterceptorManager.addInterceptor(
        new WrapperInterceptor<String, Boolean>() {

          @Override
          public Boolean invokeCodeWrapper(WrapperInvocation<String, Boolean> invocation)
              throws Exception {
            logger.info("0");
            if ("EXP".equals(invocation.getParameterSupplier().get())) {
              throw new IllegalAccessException("test exception!");
            }
            return invocation.proceed();
          }

          @Override
          public int getOrder() {
            return 0;
          }
        });

    wrapperInterceptorManager.addInterceptor(
        new WrapperInterceptor<String, Boolean>() {

          @Override
          public Boolean invokeCodeWrapper(WrapperInvocation<String, Boolean> invocation)
              throws Exception {
            logger.info("1");
            if ("Proceed".equals(invocation.getParameterSupplier().get())) {
              ret.set(1);
              return true;
            }
            return invocation.proceed();
          }

          @Override
          public int getOrder() {
            return 1;
          }
        });

    wrapperInterceptorManager.addInterceptor(
        new WrapperInterceptor<String, Boolean>() {

          @Override
          public Boolean invokeCodeWrapper(WrapperInvocation<String, Boolean> invocation)
              throws Exception {
            logger.info("2");
            if ("test".equals(invocation.getParameterSupplier().get())) {
              return true;
            }
            return invocation.proceed();
          }

          @Override
          public int getOrder() {
            return 2;
          }
        });

    WrapperInvocation<String, Boolean> wrapperInvocation =
        new WrapperInvocation(
            new Wrapper<String, Boolean>() {
              @Override
              public Boolean call() {
                if ("AllRun".equals(getParameterSupplier().get())) {
                  System.out.println("success");
                  ret.set(2);
                  return true;
                }
                return null;
              }

              @Override
              public Supplier<String> getParameterSupplier() {
                return () -> input;
              }
            },
            wrapperInterceptorManager.getInterceptorChain());

    wrapperInvocation.proceed();

    return ret.get();
  }
}
