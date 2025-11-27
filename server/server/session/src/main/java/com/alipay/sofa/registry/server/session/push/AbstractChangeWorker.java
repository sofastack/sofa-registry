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

import java.util.List;

/**
 * 抽象变更任务处理 Worker
 *
 * <p>作为所有变更任务处理器的基类，提供了通用的任务管理功能： 1. 线程安全保障机制 2. 防抖延迟控制 3. 任务提交、查找和过期任务获取的基础框架
 *
 * <p>子类需要实现具体的任务存储和检索逻辑
 */
public abstract class AbstractChangeWorker implements ChangeWorker<ChangeKey, ChangeTaskImpl> {

  /** 同步锁对象，保护所有状态修改操作的线程安全 */
  protected final Object lock;

  /** 默认防抖延迟时间（毫秒） 控制任务提交的最小延迟时间 */
  protected volatile int changeDebouncingMillis;

  /** 最大防抖延迟时间（毫秒） 控制任务提交的最大延迟上限 */
  protected volatile int changeDebouncingMaxMillis;

  /** 任务等待时间（毫秒） 控制任务等待队列的超时时间 */
  protected volatile int changeTaskWaitingMillis;

  /**
   * 构造函数，指定所有延迟参数
   *
   * @param changeDebouncingMillis 默认防抖延迟时间
   * @param changeDebouncingMaxMillis 最大防抖延迟时间
   * @param changeTaskWaitingMillis 任务等待时间
   */
  public AbstractChangeWorker(
      int changeDebouncingMillis, int changeDebouncingMaxMillis, int changeTaskWaitingMillis) {
    this.lock = new Object();
    this.changeDebouncingMillis = changeDebouncingMillis;
    this.changeDebouncingMaxMillis = changeDebouncingMaxMillis;
    this.changeTaskWaitingMillis = changeTaskWaitingMillis;
  }

  /**
   * 动态设置防抖延迟参数
   *
   * @param pushEfficiencyImproveConfig 配置对象，包含新的延迟参数
   */
  public void setChangeTaskWorkDelay(PushEfficiencyImproveConfig pushEfficiencyImproveConfig) {
    this.changeDebouncingMillis = pushEfficiencyImproveConfig.getChangeDebouncingMillis();
    this.changeDebouncingMaxMillis = pushEfficiencyImproveConfig.getChangeDebouncingMaxMillis();
    this.changeTaskWaitingMillis = pushEfficiencyImproveConfig.getChangeTaskWaitingMillis();
  }

  /**
   * 设置防抖延迟时间
   *
   * @param changeDebouncingMillis 默认防抖延迟时间
   * @param changeDebouncingMaxMillis 最大防抖延迟时间
   */
  public void setChangeDebouncingMillis(int changeDebouncingMillis, int changeDebouncingMaxMillis) {
    this.changeDebouncingMillis = changeDebouncingMillis;
    this.changeDebouncingMaxMillis = changeDebouncingMaxMillis;
  }

  /**
   * 获取当前的防抖延迟时间配置
   *
   * @return 包含默认延迟和最大延迟的配置对象
   */
  public ChangeDebouncingTime getChangeDebouncingTime() {
    return new ChangeDebouncingTime(this.changeDebouncingMillis, this.changeDebouncingMaxMillis);
  }

  /**
   * 子类需要实现的任务查找方法
   *
   * @param key 任务键
   * @return 对应的任务对象，如果不存在返回null
   */
  protected abstract ChangeTaskImpl doFindTask(ChangeKey key);

  /**
   * 子类需要实现的任务添加方法
   *
   * @param key 任务键
   * @param task 要添加的任务对象
   * @return 添加操作的结果
   */
  protected abstract boolean pushTask(ChangeKey key, ChangeTaskImpl task);

  /**
   * 子类需要实现的任务更新方法
   *
   * @param key 任务键
   * @param task 要更新的任务对象
   * @return 更新操作的结果
   */
  protected abstract boolean updateTask(ChangeKey key, ChangeTaskImpl task);

  /**
   * 默认的任务合并逻辑实现 子类可以根据需要重写此方法以实现特定的合并策略
   *
   * @param key 任务键
   * @param task 新任务对象
   * @param handler 任务处理器
   * @param changeCtx 变更上下文
   * @param now 当前时间戳
   * @return 是否成功提交变更
   */
  protected boolean doCommitChange(
      ChangeKey key, ChangeTaskImpl task, TriggerPushContext changeCtx) {
    final ChangeTaskImpl exist = this.findTask(key);
    if (exist == null) {
      return this.pushTask(key, task);
    }

    if (task.changeCtx.smallerThan(exist.changeCtx)) {
      return false;
    }

    task.changeCtx.mergeVersion(exist.changeCtx);

    // compare with exist
    if (task.expireTimestamp <= exist.expireDeadlineTimestamp) {
      // not reach deadline, requeue to wait
      task.expireDeadlineTimestamp = exist.expireDeadlineTimestamp;
      // merge change, merge tracetimes
      task.changeCtx.addTraceTime(exist.changeCtx.getFirstTimes());
      // tasks is linkedMap, must remove the exist first, then enqueue in the tail
      return this.updateTask(key, task);
    } else {
      // reach deadline, could not requeue, use exist.expire as newTask.expire
      exist.changeCtx.setExpectDatumVersion(task.changeCtx.getExpectDatumVersion());
      return true;
    }
  }

  /**
   * 创建任务对象的默认实现 使用默认防抖延迟时间作为初始延迟
   *
   * @param key 任务键
   * @param changeCtx 变更上下文
   * @param handler 任务处理器
   * @param now 当前时间戳
   * @return 新创建的任务对象
   */
  protected ChangeTaskImpl createTask(
      ChangeKey key, TriggerPushContext changeCtx, ChangeHandler handler, long now) {
    ChangeTaskImpl changeTaskImpl =
        new ChangeTaskImpl(key, changeCtx, handler, now + this.changeDebouncingMillis);
    changeTaskImpl.expireDeadlineTimestamp = now + this.changeDebouncingMaxMillis;
    return changeTaskImpl;
  }

  /**
   * 子类需要实现的获取第一个过期任务的方法
   *
   * @param now 当前时间戳
   * @return 第一个过期的任务对象，如果没有过期任务返回null
   */
  protected abstract ChangeTaskImpl doGetExpireTask(long now);

  /**
   * 子类需要实现的获取所有过期任务的方法
   *
   * @param now 当前时间戳
   * @return 所有过期任务的列表
   */
  protected abstract List<ChangeTaskImpl> doGetExpireTasks(long now);

  /**
   * 线程安全的任务查找接口 通过同步锁保护对doFindTask方法的调用
   *
   * @param key 任务键
   * @return 对应的任务对象，如果不存在返回null
   */
  @Override
  public ChangeTaskImpl findTask(ChangeKey key) {
    synchronized (this.lock) {
      return this.doFindTask(key);
    }
  }

  /**
   * 线程安全的任务提交接口 1. 创建新任务 2. 通过同步锁保护对doCommitChange方法的调用
   *
   * @param key 任务键
   * @param handler 任务处理器
   * @param changeCtx 变更上下文
   * @return 是否成功提交变更
   */
  @Override
  public boolean commitChange(ChangeKey key, ChangeHandler handler, TriggerPushContext changeCtx) {
    final long now = System.currentTimeMillis();
    final ChangeTaskImpl task = this.createTask(key, changeCtx, handler, now);
    synchronized (this.lock) {
      return this.doCommitChange(key, task, changeCtx);
    }
  }

  /**
   * 线程安全的获取第一个过期任务接口 通过同步锁保护对doGetExpireTask方法的调用
   *
   * @return 第一个过期的任务对象，如果没有过期任务返回null
   */
  @Override
  public ChangeTaskImpl getExpireTask() {
    final long now = System.currentTimeMillis();
    synchronized (this.lock) {
      return this.doGetExpireTask(now);
    }
  }

  /**
   * 线程安全的获取所有过期任务接口 通过同步锁保护对doGetExpireTasks方法的调用
   *
   * @return 所有过期任务的列表
   */
  @Override
  public List<ChangeTaskImpl> getExpireTasks() {
    final long now = System.currentTimeMillis();
    synchronized (this.lock) {
      return this.doGetExpireTasks(now);
    }
  }

  @Override
  public int getWaitingMillis() {
    return this.changeTaskWaitingMillis;
  }
}
