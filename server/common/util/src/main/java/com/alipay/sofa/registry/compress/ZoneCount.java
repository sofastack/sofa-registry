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
package com.alipay.sofa.registry.compress;

import com.alipay.sofa.registry.util.StringUtils;
import java.util.Objects;

/**
 * @author huicha
 * @date 2025/3/24
 */
public class ZoneCount {
  private final String zone;
  private final int count;

  public static ZoneCount of(String zone, int count) {
    return new ZoneCount(zone, count);
  }

  public ZoneCount(String zone, int count) {
    this.zone = zone;
    this.count = count;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ZoneCount zoneCount = (ZoneCount) o;
    return count == zoneCount.count && Objects.equals(zone, zoneCount.zone);
  }

  @Override
  public int hashCode() {
    return Objects.hash(zone, count);
  }

  public int size() {
    return StringUtils.sizeof(zone) + 4;
  }
}
