package com.alipay.sofa.registry.server.session.push;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 默认变更任务处理 Worker
 * <p>
 * 继承自 AbstractChangeWorker，使用 LinkedHashMap 实现任务队列管理：
 * 1. 保持任务的插入顺序(FIFO)
 * 2. 提供基础的任务去重和合并功能
 * 3. 支持过期任务检测与清理
 * <p>
 * 适用于普通规模的变更任务处理场景
 */
public class DefaultChangeWorker extends AbstractChangeWorker {

  /**
   * 任务映射表，使用 LinkedHashMap 保持插入顺序
   */
  private final LinkedHashMap<ChangeKey, ChangeTaskImpl> tasks;

  /**
   * 构造函数
   *
   * @param changeDebouncingMillis      任务去重时间间隔
   * @param changeDebouncingMaxMillis   任务去重最大时间间隔
   * @param changeTaskWaitingMillis     任务等待时间间隔
   */
  public DefaultChangeWorker(int changeDebouncingMillis, int changeDebouncingMaxMillis, int changeTaskWaitingMillis) {
    super(changeDebouncingMillis, changeDebouncingMaxMillis, changeTaskWaitingMillis);
    this.tasks = Maps.newLinkedHashMap();
  }

  /**
   * 实现 AbstractChangeWorker 的任务查找方法
   * 直接从 LinkedHashMap 中查找任务
   *
   * @param key 任务键
   * @return 对应的任务对象，如果不存在返回null
   */
  @Override
  protected ChangeTaskImpl doFindTask(ChangeKey key) {
    return this.tasks.get(key);
  }

  /**
   * 实现 AbstractChangeWorker 的任务添加方法
   * 直接将任务放入 LinkedHashMap
   *
   * @param key  任务键
   * @param task 要添加的任务对象
   */
  @Override
  protected void pushTask(ChangeKey key, ChangeTaskImpl task) {
    this.tasks.put(key, task);
  }

  /**
   * 实现 AbstractChangeWorker 的任务更新方法
   * 先删除旧任务再添加新任务，保持 FIFO 语义
   *
   * @param key  任务键
   * @param task 要更新的任务对象
   */
  @Override
  protected void updateTask(ChangeKey key, ChangeTaskImpl task) {
    // tasks is linkedMap, must remove the exist first, then enqueue in the tail
    this.tasks.remove(key);
    this.tasks.put(key, task);
  }

  /**
   * 实现 AbstractChangeWorker 的获取第一个过期任务方法
   * 按照 LinkedHashMap 的插入顺序检查第一个任务是否过期
   *
   * @param now 当前时间戳
   * @return 第一个过期的任务对象，如果没有过期任务返回null
   */
  @Override
  protected ChangeTaskImpl doGetExpireTask(long now) {
    if (tasks.isEmpty()) {
      return null;
    }
    Iterator<ChangeTaskImpl> it = tasks.values().iterator();
    final ChangeTaskImpl first = it.next();
    if (first.expireTimestamp <= now) {
      it.remove();
      return first;
    }
    return null;
  }

  /**
   * 实现 AbstractChangeWorker 的获取所有过期任务方法
   * 按照 LinkedHashMap 的插入顺序依次检查任务是否过期
   * 一旦遇到未过期任务就停止检查
   *
   * @param now 当前时间戳
   * @return 所有过期任务的列表
   */
  @Override
  protected List<ChangeTaskImpl> doGetExpireTasks(long now) {
    if (tasks.isEmpty()) {
      return null;
    }
    List<ChangeTaskImpl> timeoutTasks = new ArrayList<>();
    Iterator<ChangeTaskImpl> it = tasks.values().iterator();
    while (it.hasNext()) {
      ChangeTaskImpl task = it.next();
      if (task.expireTimestamp <= now) {
        it.remove();
        timeoutTasks.add(task);
      } else {
        break; // LinkedHashMap 按插入顺序，后续任务无需检查
      }
    }
    return timeoutTasks;
  }

  /**
   * 清空所有任务
   * <p>
   * 清空 LinkedHashMap 中的所有任务数据
   */
  @Override
  public void clear() {
    this.tasks.clear();
  }
}
