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
package com.alipay.sofa.registry.server.session.utils;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitTestGCMonitor implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(UnitTestGCMonitor.class);

  private static volatile UnitTestGCMonitor INSTANCE;

  // Heap Dump 保存路径
  private final String dumpDir;

  // GC 次数阈值
  private final long gcCountThreshold;

  // 监控间隔（单位：毫秒）
  private final long monitorIntervalMs;

  private Lock lock;

  private boolean running;

  private Thread monitoryThread;

  private final Map<String, Long> countMap;

  public static UnitTestGCMonitor getInstance() {
    if (null == INSTANCE) {
      return createInstance();
    }
    return INSTANCE;
  }

  private static synchronized UnitTestGCMonitor createInstance() {
    if (null == INSTANCE) {
      INSTANCE = new UnitTestGCMonitor("/tmp", 3, 10, TimeUnit.SECONDS);
    }
    return INSTANCE;
  }

  private UnitTestGCMonitor(
      String dumpDir, long gcCountThreshold, long monitorInterval, TimeUnit timeUnit) {
    this.dumpDir = dumpDir;
    this.gcCountThreshold = gcCountThreshold;
    this.monitorIntervalMs = timeUnit.toMillis(monitorInterval);
    this.lock = new ReentrantLock(false);
    this.running = false;
    this.countMap = new HashMap<>();
  }

  public void startMonitoring() {
    this.lock.lock();
    try {
      if (this.running) {
        LOGGER.error("[UnitTestGCMonitor] GC 监控已经开启了");
        return;
      }

      LOGGER.error("[UnitTestGCMonitor] 开始监控 GC 情况");

      Thread monitoryThread = new Thread(this, "UnitTestGCMonitorThread");
      monitoryThread.start();

      this.monitoryThread = monitoryThread;
    } finally {
      this.lock.unlock();
    }
  }

  public void stopMonitoring() {
    this.lock.lock();
    try {
      if (!this.running) {
        LOGGER.error("[UnitTestGCMonitor] GC 监控已经停止");
        return;
      }

      // 中断线程，然后退出
      this.monitoryThread.interrupt();
      this.monitoryThread = null;
    } finally {
      this.lock.unlock();
    }
  }

  @Override
  public void run() {
    try {
      while (true) {
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
          String gcBeanName = gcBean.getName();
          long currentGCCount = gcBean.getCollectionCount();

          LOGGER.error(
              "[UnitTestGCMonitor] 获取到 GC Bean: {}, 当前 GC 次数: {}", gcBeanName, currentGCCount);

          if (!this.isFGC(gcBeanName)) {
            continue;
          }

          LOGGER.error("[UnitTestGCMonitor] GC Bean: {} 是涉及 FGC 的 GC Bean!", gcBeanName);

          // 获取上一次的 GC 次数
          long oldGCCount = this.countMap.getOrDefault(gcBeanName, 0L);

          // 更新一下新的
          this.countMap.put(gcBeanName, currentGCCount);

          LOGGER.error(
              "[UnitTestGCMonitor] 获取到 GC Bean: {}, 上一次 GC 次数: {}, 当前 GC 次数: {}",
              gcBeanName,
              oldGCCount,
              currentGCCount);

          // 计算一下期间的 GC 次数
          long count = currentGCCount - oldGCCount;
          if (count < this.gcCountThreshold) {
            continue;
          }

          String dumpPath = this.dumpDir + "/" + gcBeanName + ".hprof";

          if (Files.exists(Paths.get(dumpPath))) {
            LOGGER.error(
                "[UnitTestGCMonitor] 获取到 GC Bean: {}, Heap Dump 文件已经存在: {}，跳过 Heap Dump",
                gcBeanName,
                dumpPath);
            continue;
          }

          LOGGER.error(
              "[UnitTestGCMonitor] 获取到 GC Bean: {}, 上一次 GC 次数: {}, 当前 GC 次数: {}, 触发 Heap Dump: {}",
              gcBeanName,
              oldGCCount,
              currentGCCount,
              dumpPath);
          if (this.generateHeapDump(dumpPath)) {
            LOGGER.error(
                "[UnitTestGCMonitor] 获取到 GC Bean: {}, Heap Dump 成功: {}", gcBeanName, dumpPath);
            return;
          } else {
            LOGGER.error(
                "[UnitTestGCMonitor] 获取到 GC Bean: {}, Heap Dump 失败: {}", gcBeanName, dumpPath);
          }
        }

        // 全部检查完成，等待下一个监控周期
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(monitorIntervalMs));

        if (Thread.interrupted()) {
          // 响应中断
          LOGGER.error("[UnitTestGCMonitor] 监控 GC 线程被中断，退出!");
        }
      }
    } catch (Throwable throwable) {
      LOGGER.error("[UnitTestGCMonitor] 监控 GC 线程执行异常，退出" + throwable);
    }
  }

  private boolean isFGC(String gcBeanName) {
    // 涵盖 JDK 8 常见收集器的 Full GC/Major GC Bean 名称
    return gcBeanName.contains("MarkSweep")
        || // Parallel GC (PS MarkSweep) 或 Serial GC
        gcBeanName.contains("Old")
        || // G1 Old Generation
        gcBeanName.contains("ConcurrentMarkSweep"); // CMS
  }

  private boolean generateHeapDump(String dumpPath) {
    try {
      // 尝试获取 HotSpotDiagnosticMXBean
      HotSpotDiagnosticMXBean hotSpotBean =
          ManagementFactory.newPlatformMXBeanProxy(
              ManagementFactory.getPlatformMBeanServer(),
              "com.sun.management:type=HotSpotDiagnostic",
              HotSpotDiagnosticMXBean.class);

      if (null == hotSpotBean) {
        LOGGER.error("[UnitTestGCMonitor] 获取 HotSpotDiagnosticMXBean 失败");
        return false;
      }

      Path filePath = Paths.get(dumpPath);
      Path dirPath = filePath.getParent();

      if (!Files.exists(dirPath)) {
        LOGGER.error("[UnitTestGCMonitor] Heap Dump 路径不存在，创建一次: {}", dirPath.toAbsolutePath());
        Files.createDirectories(dirPath);
      }

      // 执行 Dump
      hotSpotBean.dumpHeap(dumpPath, true);

      LOGGER.error("[UnitTestGCMonitor] Heap Dump 成功: {}", dumpPath);
      return true;
    } catch (Throwable throwable) {
      LOGGER.error("[UnitTestGCMonitor] Heap Dump 异常: {}", dumpPath, throwable);
      return false;
    }
  }
}
