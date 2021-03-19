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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author shangyu.wh
 * @version 1.0: WrapperInterceptorManager.java, v 0.1 2019-06-18 14:51 shangyu.wh Exp $
 */
public class WrapperInterceptorManager {

  private List<WrapperInterceptor> interceptorChain = new CopyOnWriteArrayList<>();

  public int addInterceptor(WrapperInterceptor item) {
    // The index of the search key, if it is contained in the list; otherwise, (-(insertion point) -
    // 1)
    final int index =
        Collections.binarySearch(
            interceptorChain,
            item,
            (o1, o2) -> {
              if (o1 == null && o2 == null) {
                return 0;
              } else if (o1 == null) {
                return 1;
              } else if (o2 == null) {
                return -1;
              }

              if (o1.getOrder() < o2.getOrder()) {
                return -1;
              } else if (o1.getOrder() > o2.getOrder()) {
                return 1;
              }
              return 0;
            });
    final int insertAt;
    if (index < 0) {
      insertAt = -(index + 1);
    } else {
      insertAt = index + 1;
    }

    interceptorChain.add(insertAt, item);
    return insertAt;
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
