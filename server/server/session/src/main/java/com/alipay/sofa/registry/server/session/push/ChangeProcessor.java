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
package com.alipay.sofa.registry.server.session.push;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

public class ChangeProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChangeProcessor.class);

  @Autowired private SessionServerConfig sessionServerConfig;

  private Worker[] workers;
  private int changeDebouncingMillis;
  private int changeDebouncingMaxMillis;

  @PostConstruct
  public void init() {
    this.changeDebouncingMillis = sessionServerConfig.getDataChangeDebouncingMillis();
    this.changeDebouncingMaxMillis = sessionServerConfig.getDataChangeMaxDebouncingMillis();

    this.workers = new Worker[sessionServerConfig.getDataChangeFetchTaskWorkerSize()];
    for (int i = 0; i < workers.length; i++) {
      workers[i] = new Worker();
      ConcurrentUtils.createDaemonThread("ChangeExecutor-" + i, workers[i]).start();
    }
  }

  boolean fireChange(
      String dataCenter, String dataInfoId, ChangeHandler handler, long expectDatumVersion) {
    ChangeKey key = new ChangeKey(dataCenter, dataInfoId);
    Worker worker = workerOf(key);
    return worker.commitChange(key, handler, expectDatumVersion);
  }

  interface ChangeHandler {
    void onChange(String dataCenter, String dataInfoId, long expectDatumVersion);
  }

  private static final class ChangeTask {
    final ChangeKey key;
    final ChangeHandler changeHandler;
    long expectDatumVersion;
    final long expireTimestamp;
    long expireDeadlineTimestamp;

    ChangeTask(
        ChangeKey key, long expectDatumVersion, ChangeHandler changeHandler, long expireTimestamp) {
      this.key = key;
      this.changeHandler = changeHandler;
      this.expectDatumVersion = expectDatumVersion;
      this.expireTimestamp = expireTimestamp;
    }

    void doChange() {
      changeHandler.onChange(key.dataCenter, key.dataInfoId, expectDatumVersion);
    }

    @Override
    public String toString() {
      return "ChangeTask{"
          + "key="
          + key
          + ", version="
          + expectDatumVersion
          + ", expire="
          + expireTimestamp
          + ", deadline="
          + expireDeadlineTimestamp
          + '}';
    }
  }

  private final class Worker extends WakeUpLoopRunnable {
    final LinkedHashMap<ChangeKey, ChangeTask> tasks = Maps.newLinkedHashMap();

    boolean commitChange(ChangeKey key, ChangeHandler handler, long expectDatumVersion) {
      synchronized (tasks) {
        final long now = System.currentTimeMillis();
        final ChangeTask task =
            new ChangeTask(key, expectDatumVersion, handler, now + changeDebouncingMillis);
        final ChangeTask exist = tasks.get(key);
        if (exist == null) {
          task.expireDeadlineTimestamp = now + changeDebouncingMaxMillis;
          tasks.put(key, task);
          return true;
        }
        if (task.expectDatumVersion <= exist.expectDatumVersion) {
          return false;
        }

        if (task.expireTimestamp <= exist.expireDeadlineTimestamp) {
          // requeue, expire is before deadline, remove first, unlink the list
          task.expireDeadlineTimestamp = exist.expireDeadlineTimestamp;
          tasks.remove(key);
          tasks.put(key, task);
        } else {
          // reach deadline, could not requeue, replace the exist
          exist.expectDatumVersion = task.expectDatumVersion;
        }
        return true;
      }
    }

    ChangeTask getExpire() {
      final long now = System.currentTimeMillis();
      synchronized (tasks) {
        if (tasks.isEmpty()) {
          return null;
        }
        Iterator<ChangeTask> it = tasks.values().iterator();
        final ChangeTask first = it.next();
        if (first.expireTimestamp <= now) {
          it.remove();
          return first;
        }
        return null;
      }
    }

    @Override
    public void runUnthrowable() {
      for (; ; ) {
        final ChangeTask task = getExpire();
        if (task == null) {
          break;
        }
        try {
          task.doChange();
        } catch (Throwable e) {
          LOGGER.error("failed to doChange, {}", task);
        }
      }
    }

    @Override
    public int getWaitingMillis() {
      return 100;
    }
  }

  private static final class ChangeKey {
    final String dataInfoId;
    final String dataCenter;

    ChangeKey(String dataCenter, String dataInfoId) {
      this.dataCenter = dataCenter;
      this.dataInfoId = dataInfoId;
    }

    @Override
    public String toString() {
      return dataInfoId + "@" + dataCenter;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ChangeKey changeKey = (ChangeKey) o;
      return Objects.equals(dataInfoId, changeKey.dataInfoId)
          && Objects.equals(dataCenter, changeKey.dataCenter);
    }

    @Override
    public int hashCode() {
      return Objects.hash(dataInfoId, dataCenter);
    }
  }

  private Worker workerOf(ChangeKey key) {
    int n = (key.hashCode() & 0x7fffffff) % workers.length;
    return workers[n];
  }
}
