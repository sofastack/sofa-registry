package com.alipay.sofa.registry.server.session.push;

import java.util.List;

/**
 * 变更任务处理接口
 * <p>
 * 定义了变更任务处理的核心操作，所有实现类都需要保证线程安全：
 * 1. 任务查找功能
 * 2. 变更提交和合并功能
 * 3. 过期任务获取功能
 *
 * @param <Key>  任务键类型
 * @param <Task> 任务类型，必须实现 ChangeTask 接口
 */
public interface ChangeWorker<Key, Task extends ChangeTask<Key>> {

  /**
   * 根据键查找任务
   *
   * @param key 任务键
   * @return 对应的任务对象，如果不存在返回null
   */
  Task findTask(Key key);

  /**
   * 提交变更请求
   *
   * @param key       任务键
   * @param handler   任务处理器
   * @param changeCtx 变更上下文信息
   * @return 是否成功提交变更
   */
  boolean commitChange(ChangeKey key, ChangeHandler handler, TriggerPushContext changeCtx);

  /**
   * 获取第一个过期任务
   *
   * @return 第一个过期任务，如果没有过期任务返回null
   */
  Task getExpireTask();

  /**
   * 获取所有过期任务
   *
   * @return 过期任务列表
   */
  List<Task> getExpireTasks();

  /**
   * 获取任务处理等待时间
   *
   * @return 任务处理等待时间
   */
  int getWaitingMillis();

  /**
   * 清空所有任务
   */
  void clear();
}
