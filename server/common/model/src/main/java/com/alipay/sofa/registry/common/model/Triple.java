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
 * @author chen.zhu
 *     <p>Jan 12, 2021
 */
public final class Triple<F, M, L> {

  private final F first;

  private final M middle;

  private final L last;

  public Triple(F first, M middle, L last) {
    this.first = first;
    this.middle = middle;
    this.last = last;
  }

  public static <F, M, L> Triple<F, M, L> from(F first, M middle, L last) {
    return new Triple<F, M, L>(first, middle, last);
  }

  public F getFirst() {
    return this.first;
  }

  public L getLast() {
    return this.last;
  }

  public M getMiddle() {
    return this.middle;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
    return Objects.equals(first, triple.first)
        && Objects.equals(middle, triple.middle)
        && Objects.equals(last, triple.last);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, middle, last);
  }

  public int size() {
    return 3;
  }

  public String toString() {
    return String.format(
        "Triple[first=%s, middle=%s, last=%s]", this.first, this.middle, this.last);
  }
}
