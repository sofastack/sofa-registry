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
package com.alipay.sofa.registry.common.model.metaserver.metrics;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;

public class SystemLoad implements Serializable {

  private static final long serialVersionUID = 826118559184274124L;

  /**
   * 安全比较两个 SystemLoad 对象是否相等。
   *
   * <p>规则： - 两者均为 null → 相等 - 仅一个为 null → 不等 - 均非 null → 比较 cpuAverage 和 loadAverage 字段值
   *
   * @param left 左侧对象，可为 null
   * @param right 右侧对象，可为 null
   * @return 若两者逻辑相等则返回 true，否则返回 false
   */
  public static boolean equals(SystemLoad left, SystemLoad right) {
    if (left == right) {
      // 包含 both null 或 same instance
      return true;
    }
    if (left == null || right == null) {
      // 一个为 null，另一个非 null
      return false;
    }
    return Double.doubleToLongBits(left.cpuAverage) == Double.doubleToLongBits(right.cpuAverage)
        && Double.doubleToLongBits(left.loadAverage) == Double.doubleToLongBits(right.loadAverage);
  }

  private double cpuAverage;

  private double loadAverage;

  public SystemLoad() {}

  public SystemLoad(double cpuAverage, double loadAverage) {
    this.cpuAverage = cpuAverage;
    this.loadAverage = loadAverage;
  }

  public double getCpuAverage() {
    return cpuAverage;
  }

  public void setCpuAverage(double cpuAverage) {
    this.cpuAverage = cpuAverage;
  }

  public double getLoadAverage() {
    return loadAverage;
  }

  public void setLoadAverage(double loadAverage) {
    this.loadAverage = loadAverage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SystemLoad that = (SystemLoad) o;
    return Double.doubleToLongBits(cpuAverage) == Double.doubleToLongBits(that.cpuAverage)
        && Double.doubleToLongBits(loadAverage) == Double.doubleToLongBits(that.loadAverage);
  }

  @Override
  public int hashCode() {
    long temp = Double.doubleToLongBits(cpuAverage);
    int result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(loadAverage);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
