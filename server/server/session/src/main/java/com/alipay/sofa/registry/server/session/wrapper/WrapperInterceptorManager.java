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
package com.alipay.sofa.registry.server.session.wrapper;

import com.alipay.sofa.registry.common.model.wrapper.WrapperInterceptor;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author shangyu.wh
 * @version 1.0: WrapperInterceptorManager.java, v 0.1 2019-06-18 14:51 shangyu.wh Exp $
 */
public class WrapperInterceptorManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(WrapperInterceptorManager.class);
  private volatile List<WrapperInterceptor> interceptorChain = Collections.EMPTY_LIST;

  public void addInterceptor(WrapperInterceptor item) {
    List<WrapperInterceptor> list = Lists.newArrayListWithCapacity(interceptorChain.size() + 1);
    list.addAll(interceptorChain);
    list.add(item);
    list.sort(new Comp());
    this.interceptorChain = list;
    LOGGER.info("add interceptor:{}, now={}", item.getClass(), interceptorChain);
  }

  static final class Comp implements Comparator<WrapperInterceptor> {
    @Override
    public int compare(WrapperInterceptor o1, WrapperInterceptor o2) {
      return o1.getOrder() - o2.getOrder();
    }
  }
  /**
   * Getter method for property <tt>interceptorChain</tt>.
   *
   * @return property value of interceptorChain
   */
  public List<WrapperInterceptor> getInterceptorChain() {
    return interceptorChain;
  }
}
