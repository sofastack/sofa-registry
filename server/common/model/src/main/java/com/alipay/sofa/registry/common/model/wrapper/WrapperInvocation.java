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
package com.alipay.sofa.registry.common.model.wrapper;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author shangyu.wh
 * @version 1.0: WrapperInvocation.java, v 0.1 2019-06-18 11:43 shangyu.wh Exp $
 */
public class WrapperInvocation<T, R> {

  private final Wrapper<T, R> target;

  private Iterator<WrapperInterceptor> iterator;

  public WrapperInvocation(Wrapper<T, R> target, List<WrapperInterceptor> interceptorChain) {
    this.iterator = interceptorChain.iterator();
    this.target = target;
  }

  public R proceed() throws Exception {
    if (iterator.hasNext()) {
      WrapperInterceptor<T, R> interceptor = iterator.next();
      return interceptor.invokeCodeWrapper(this);
    }
    return target.call();
  }

  /**
   * Getter method for property <tt>ParameterSupplier</tt>.
   *
   * @return property value of ParameterSupplier
   */
  public Supplier<T> getParameterSupplier() {
    return target.getParameterSupplier();
  }
}
