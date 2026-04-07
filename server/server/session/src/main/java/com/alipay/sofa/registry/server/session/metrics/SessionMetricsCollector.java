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
package com.alipay.sofa.registry.server.session.metrics;

import com.alipay.sofa.registry.common.model.metaserver.metrics.SystemLoad;
import java.lang.management.ManagementFactory;

public final class SessionMetricsCollector {

  // 单例模式
  private static final SessionMetricsCollector INSTANCE = new SessionMetricsCollector();

  // 获取单例实例
  public static SessionMetricsCollector getInstance() {
    return INSTANCE;
  }

  // OpenJDK / HotSpot 扩展的 MXBean（提供 getSystemCpuLoad 等）
  private final com.sun.management.OperatingSystemMXBean openJdkOperatingSystemMXBean;

  // Java SE 标准 MXBean（提供 getSystemLoadAverage 等）
  private final java.lang.management.OperatingSystemMXBean standardOperatingSystemMXBean;

  private SessionMetricsCollector() {
    this.standardOperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    // 尝试转换为 OpenJDK 扩展接口
    if (this.standardOperatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean) {
      this.openJdkOperatingSystemMXBean =
          (com.sun.management.OperatingSystemMXBean) this.standardOperatingSystemMXBean;
    } else {
      this.openJdkOperatingSystemMXBean = null; // 部分 JDK 如 IBM J9, GraalVM Native 等不支持
    }
  }

  public SystemLoad getSystemLoad() {
    SystemLoad systemLoad = new SystemLoad();
    systemLoad.setCpuAverage(this.getSystemCpuAverage());
    systemLoad.setLoadAverage(this.getSystemLoadAverage());
    return systemLoad;
  }

  /**
   * 返回整个操作系统近期的 CPU 使用率（以百分比表示）。
   *
   * <p>该值是一个介于 [0.0, 100.0] 之间的 double 值： {@code 0.0} 表示所有 CPU 在最近观测期间完全空闲， {@code 100.0} 表示所有 CPU
   * 在最近观测期间始终处于满负荷运行。
   *
   * <p>如果无法获取近期 CPU 使用率，则返回 -1。
   *
   * @return 整个系统的近期 CPU 使用率（百分比，范围 [0.0, 100.0]），不可用时返回 -1
   * @see com.sun.management.OperatingSystemMXBean#getSystemCpuLoad()
   */
  public double getSystemCpuAverage() {
    if (this.openJdkOperatingSystemMXBean != null) {
      double systemCpuLoad = this.openJdkOperatingSystemMXBean.getSystemCpuLoad();
      if (systemCpuLoad < 0) {
        return -1;
      }
      return systemCpuLoad * 100;
    } else {
      return -1; // JDK 不支持此操作
    }
  }

  /**
   * 返回整个操作系统的系统平均负载（system load average）。
   *
   * <p>系统平均负载是操作系统层面的一个指标，表示在最近一段时间内（通常为 1 分钟）， 等待 CPU 或处于不可中断睡眠状态（如 I/O）的进程/线程的平均数量。
   *
   * <p>该值没有固定上限，其合理范围取决于系统 CPU 核心数。例如，在 4 核系统上， 负载为 4.0 通常表示 CPU 资源被充分利用。
   *
   * <p>如果系统不支持或无法获取该指标，则返回 -1。
   *
   * @return 系统平均负载（非负浮点数），不可用时返回 -1
   * @see java.lang.management.OperatingSystemMXBean#getSystemLoadAverage()
   */
  public double getSystemLoadAverage() {
    if (this.standardOperatingSystemMXBean == null) {
      return -1;
    }
    double systemLoadAverage = this.standardOperatingSystemMXBean.getSystemLoadAverage();
    return systemLoadAverage < 0 ? -1 : systemLoadAverage;
  }
}
