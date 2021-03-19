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
package com.alipay.sofa.registry.common.model;

import java.util.Objects;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-15 10:28 yuzhi.lyz Exp $
 */
public final class Tuple<T1, T2> {
  public final T1 o1;
  public final T2 o2;

  public Tuple(T1 o1, T2 o2) {
    this.o1 = o1;
    this.o2 = o2;
  }

  public static <T1, T2> Tuple<T1, T2> of(T1 t1, T2 t2) {
    return new Tuple<>(t1, t2);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Tuple)) return false;
    Tuple<?, ?> tuple = (Tuple<?, ?>) o;
    return Objects.equals(o1, tuple.o1) && Objects.equals(o2, tuple.o2);
  }

  public T1 getFirst() {
    return o1;
  }

  public T2 getSecond() {
    return o2;
  }

  @Override
  public int hashCode() {
    return Objects.hash(o1, o2);
  }

  @Override
  public String toString() {
    return "Tuple{" + "o1=" + o1 + ", o2=" + o2 + '}';
  }
}
