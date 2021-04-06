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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Optional;

/**
 * The type Collection utils.
 *
 * @author zhuoyu.sjw
 * @version $Id : CollectionUtils.java, v 0.1 2018-04-12 14:54 zhuoyu.sjw Exp $$
 */
public class CollectionUtils {

  /**
   * Gets random.
   *
   * @param <E> the type parameter
   * @param e the e
   * @return the random
   */
  public static <E> Optional<E> getRandom(Collection<E> e) {
    return e.stream().skip((int) (e.size() * Math.random())).findFirst();
  }

  /**
   * 取差集
   *
   * @param collection1
   * @param collection2
   * @return
   */
  public static <E> Collection<E> reduce(Collection<E> collection1, Collection<E> collection2) {
    Collection<E> reduce =
        collection1.stream().filter(item -> !collection2.contains(item)).collect(toList());

    return reduce;
  }
}
