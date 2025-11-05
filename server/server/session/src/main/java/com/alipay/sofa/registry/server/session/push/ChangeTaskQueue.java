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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * 变更任务队列实现类
 *
 * <p>基于跳表数据结构实现的线程安全任务队列，支持按键查找和按截止时间排序： 1. 使用 ConcurrentHashMap 保存键到任务的映射，实现O(1)查找 2. 使用
 * ConcurrentSkipListSet 按截止时间维护任务排序 3. 通过读写锁分离读写操作，提高并发性能
 *
 * @param <Key> 任务键类型
 * @param <Task> 任务类型，必须实现 ChangeTask 接口
 */
public class ChangeTaskQueue<Key extends Comparable<Key>, Task extends ChangeTask<Key>> {

  /** 读写锁实例，用于保护并发访问 */
  private final ReadWriteLock lock;

  private final Lock readLock;
  private final Lock writeLock;

  /** 任务映射表，用于快速按键查找任务 */
  private final Map<Key, Task> taskMap;

  /** 跳表集合，按截止时间排序维护所有任务 */
  private final ConcurrentSkipListSet<Task> taskLinkList;

  /** 默认构造函数 初始化读写锁和数据结构 */
  public ChangeTaskQueue() {
    this.lock = new ReentrantReadWriteLock();
    this.readLock = lock.readLock();
    this.writeLock = lock.writeLock();
    this.taskMap = Maps.newHashMap();
    this.taskLinkList = new ConcurrentSkipListSet<>();
  }

  /**
   * 根据键查找任务
   *
   * @param key 任务键
   * @return 对应的任务对象，如果不存在返回null
   */
  public Task findTask(Key key) {
    this.readLock.lock();
    try {
      return this.taskMap.get(key);
    } finally {
      this.readLock.unlock();
    }
  }

  /**
   * 添加或更新任务
   *
   * @param task 要添加或更新的任务对象
   * @return 添加或更新操作的结果，具体含义由子类定义： - 当添加新任务时返回特定值 - 当更新已有任务时返回另一特定值 - 操作失败时返回失败标识
   */
  public boolean pushTask(Task task) {
    this.writeLock.lock();
    try {
      Key key = task.key();
      if (null == key) {
        throw new IllegalArgumentException("task key is null");
      }
      Task existTask = this.taskMap.get(key);
      if (null == existTask) {
        // 新增任务
        if (!this.taskLinkList.add(task)) {
          return false;
        }
      } else {
        // 更新任务，先移除旧任务再添加新任务
        if (!this.taskLinkList.remove(existTask)) {
          return false;
        }
        if (!this.taskLinkList.add(task)) {
          return false;
        }
      }
      this.taskMap.put(key, task);
      return true;
    } finally {
      this.writeLock.unlock();
    }
  }

  /**
   * 获取第一个过期任务
   *
   * @param now 当前时间戳
   * @return 第一个过期任务，如果没有过期任务返回null
   */
  public Task popTimeoutTask(long now) {
    this.writeLock.lock();
    try {
      Iterator<Task> taskIter = this.taskLinkList.iterator();
      while (taskIter.hasNext()) {
        Task task = taskIter.next();
        if (task.deadline() > now) {
          break; // 由于已排序，后续任务都不会过期
        }

        taskIter.remove(); // 从跳表中移除
        Key key = task.key();
        this.taskMap.remove(key); // 同步更新映射表

        return task;
      }
      return null;
    } finally {
      this.writeLock.unlock();
    }
  }

  /**
   * 批量获取所有过期任务
   *
   * @param now 当前时间戳
   * @return 过期任务列表
   */
  public List<Task> popTimeoutTasks(long now) {
    this.writeLock.lock();
    try {
      List<Task> timeoutTasks = new ArrayList<>();
      Iterator<Task> taskIter = this.taskLinkList.iterator();
      while (taskIter.hasNext()) {
        Task task = taskIter.next();
        if (task.deadline() > now) {
          break; // 由于已排序，后续任务都不会过期
        }

        timeoutTasks.add(task);

        taskIter.remove(); // 从跳表中移除
        Key key = task.key();
        this.taskMap.remove(key); // 同步更新映射表
      }
      return timeoutTasks;
    } finally {
      this.writeLock.unlock();
    }
  }

  /**
   * 清空队列中的所有任务
   *
   * <p>线程安全：通过写锁保证并发操作的原子性
   */
  public void clear() {
    this.writeLock.lock();
    try {
      this.taskMap.clear();
      this.taskLinkList.clear();
    } finally {
      this.writeLock.unlock();
    }
  }

  /**
   * 遍历所有任务（测试用途）
   *
   * @param visitor 任务访问者函数
   */
  @VisibleForTesting
  public void visitTasks(Consumer<Task> visitor) {
    this.readLock.lock();
    try {
      for (Task task : this.taskLinkList) {
        visitor.accept(task);
      }
    } finally {
      this.readLock.unlock();
    }
  }
}
