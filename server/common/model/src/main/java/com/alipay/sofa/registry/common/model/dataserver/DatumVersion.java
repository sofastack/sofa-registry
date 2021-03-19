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
package com.alipay.sofa.registry.common.model.dataserver;

import java.io.Serializable;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-04 11:54 yuzhi.lyz Exp $
 */
public final class DatumVersion implements Serializable {

  private final long value;

  public DatumVersion(long v) {
    this.value = v;
  }

  /**
   * Getter method for property <tt>value</tt>.
   *
   * @return property value of value
   */
  public long getValue() {
    return value;
  }

  @Override
  public String toString() {
    return Long.valueOf(value).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DatumVersion that = (DatumVersion) o;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Long.valueOf(value).hashCode();
  }

  public static DatumVersion of(long v) {
    return new DatumVersion(v);
  }
}
