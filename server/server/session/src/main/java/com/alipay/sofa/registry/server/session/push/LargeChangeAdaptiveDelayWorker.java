package com.alipay.sofa.registry.server.session.push;

import com.google.common.annotations.VisibleForTesting;

import java.util.List;

/**
 * 大变更自适应延迟工作器
 * <p>
 * 专为处理大量发布者(Publisher)的变更任务而设计，具有以下特性：
 * 1. 根据发布者数量动态调整任务执行延迟
 * 2. 使用跳表实现的任务队列，支持按截止时间排序
 * 3. 实现大任务特有的版本合并策略
 * <p>
 * 延迟计算策略：
 * - 发布者数量 <= 阈值：使用父类默认延迟策略
 * - 阈值 < 发布者数量 <= 上限：线性增长延迟
 * - 发布者数量 > 上限：固定最大延迟
 */
public class LargeChangeAdaptiveDelayWorker extends AbstractChangeWorker {

  /**
   * 发布者数量阈值，超过此值开始计算动态延迟
   */
  private volatile long publisherThreshold;

  /**
   * 发布者数量上限，达到此值后延迟固定为最大值
   */
  private volatile long maxPublisherCount;

  /**
   * 基础延迟时间（毫秒），用于发布者数量较少的情况
   */
  private volatile long baseDelay;

  /**
   * 每个发布者单位的延迟增量（毫秒）
   */
  private volatile long delayPerUnit;

  /**
   * 最大延迟时间（毫秒）
   */
  private volatile long maxDelay;

  /**
   * 变更任务队列，使用跳表实现，支持按截止时间排序
   */
  private final ChangeTaskQueue<ChangeKey, ChangeTaskImpl> taskQueue;

  /**
   * 构造函数
   *
   * @param changeDebouncingMillis    默认防抖延迟时间
   * @param changeDebouncingMaxMillis 最大防抖延迟时间
   * @param changeTaskWaitingMillis   任务等待时间
   * @param baseDelay                 基础延迟时间
   * @param delayPerUnit              每单位发布者的延迟增量
   * @param publisherThreshold        发布者数量阈值
   * @param maxPublisherCount         最大发布者数量
   */
  public LargeChangeAdaptiveDelayWorker(int changeDebouncingMillis, int changeDebouncingMaxMillis, int changeTaskWaitingMillis,
                                        long baseDelay, long delayPerUnit, long publisherThreshold, long maxPublisherCount) {
    super(changeDebouncingMillis, changeDebouncingMaxMillis, changeTaskWaitingMillis);
    this.baseDelay = baseDelay;
    this.delayPerUnit = delayPerUnit;
    this.publisherThreshold = publisherThreshold;
    this.maxPublisherCount = maxPublisherCount;
    this.maxDelay = this.computeDelay(baseDelay, delayPerUnit, publisherThreshold, maxPublisherCount);
    this.taskQueue = new ChangeTaskQueue<>();
  }

  /**
   * 重置自适应延迟配置参数
   * <p>
   * 根据提供的配置对象更新所有延迟计算相关参数，并重新计算最大延迟值
   *
   * @param largeChangeAdaptiveDelayConfig 包含新配置参数的配置对象
   */
  public void reset(LargeChangeAdaptiveDelayConfig largeChangeAdaptiveDelayConfig) {
    this.baseDelay = largeChangeAdaptiveDelayConfig.getBaseDelay();
    this.delayPerUnit = largeChangeAdaptiveDelayConfig.getDelayPerUnit();
    this.publisherThreshold = largeChangeAdaptiveDelayConfig.getPublisherThreshold();
    this.maxPublisherCount = largeChangeAdaptiveDelayConfig.getMaxPublisherCount();
    this.maxDelay = this.computeDelay(baseDelay, delayPerUnit, publisherThreshold, maxPublisherCount);
  }

  /**
   * 计算动态延迟值（线性增长）
   *
   * @param baseDelay          基础延迟时间
   * @param delayPerUnit       每单位发布者的延迟增量
   * @param publisherThreshold 发布者数量阈值
   * @param publisherCount     当前任务的发布者数量
   * @return 计算后的延迟值
   */
  private long computeDelay(long baseDelay, long delayPerUnit, long publisherThreshold, long publisherCount) {
    return baseDelay + (delayPerUnit * (publisherCount - publisherThreshold));
  }

  /**
   * 实现 AbstractChangeWorker 的任务查找方法
   * 使用 ChangeTaskQueue 的查找功能
   *
   * @param key 任务键
   * @return 对应的任务对象，如果不存在返回null
   */
  @Override
  protected ChangeTaskImpl doFindTask(ChangeKey key) {
    return this.taskQueue.findTask(key);
  }

  /**
   * 实现 AbstractChangeWorker 的任务添加方法
   * 使用 ChangeTaskQueue 的任务推送功能
   *
   * @param key  任务键
   * @param task 要添加的任务对象
   */
  @Override
  protected void pushTask(ChangeKey key, ChangeTaskImpl task) {
    this.taskQueue.pushTask(task);
  }

  /**
   * 实现 AbstractChangeWorker 的任务更新方法
   * 使用 ChangeTaskQueue 的任务推送功能，会自动覆盖同键任务
   *
   * @param key  任务键
   * @param task 要更新的任务对象
   */
  @Override
  protected void updateTask(ChangeKey key, ChangeTaskImpl task) {
    this.taskQueue.pushTask(task);
  }

  /**
   * 实现 AbstractChangeWorker 的获取第一个过期任务方法
   * 使用 ChangeTaskQueue 的单个过期任务获取功能
   *
   * @param now 当前时间戳
   * @return 第一个过期的任务对象，如果没有过期任务返回null
   */
  @Override
  protected ChangeTaskImpl doGetExpireTask(long now) {
    return this.taskQueue.popTimeoutTask(now);
  }

  /**
   * 实现 AbstractChangeWorker 的获取所有过期任务方法
   * 使用 ChangeTaskQueue 的批量过期任务获取功能
   *
   * @param now 当前时间戳
   * @return 所有过期任务的列表
   */
  @Override
  protected List<ChangeTaskImpl> doGetExpireTasks(long now) {
    return this.taskQueue.popTimeoutTasks(now);
  }

  /**
   * 重写 AbstractChangeWorker 的任务创建方法
   * 根据发布者数量计算自适应延迟时间
   *
   * @param key       任务键
   * @param changeCtx 变更上下文
   * @param handler   任务处理器
   * @param now       当前时间戳
   * @return 新创建的任务对象
   */
  @Override
  protected ChangeTaskImpl createTask(ChangeKey key, TriggerPushContext changeCtx, ChangeHandler handler, long now) {
    Integer publisherCount = changeCtx.getPublisherCount();
    if (null == publisherCount) {
      return super.createTask(key, changeCtx, handler, now);
    }

    if (publisherCount <= this.publisherThreshold) {
      return super.createTask(key, changeCtx, handler, now);
    }

    long delay = publisherCount >= this.maxPublisherCount ?
            this.maxDelay :
            this.computeDelay(this.baseDelay, this.delayPerUnit, this.publisherThreshold, publisherCount);

    long deadline = now + delay;

    ChangeTaskImpl changeTask = new ChangeTaskImpl(key, changeCtx, handler, deadline);
    changeTask.expireDeadlineTimestamp = deadline;
    return changeTask;
  }

  /**
   * 重写 AbstractChangeWorker 的任务合并方法
   * 实现大任务特有的合并策略：
   * 1. 根据任务类型（大/小推送）采用不同的合并逻辑
   * 2. 大推送任务之间只更新版本号，不调整截止时间
   *
   * @param key       任务键
   * @param task      新任务对象
   * @param handler   任务处理器
   * @param changeCtx 变更上下文
   * @param now       当前时间戳
   * @return 是否成功提交变更
   */
  @Override
  protected boolean doCommitChange(ChangeKey key, ChangeTaskImpl task, ChangeHandler handler, TriggerPushContext changeCtx, long now) {
    // 1. 检查旧任务是否存在
    final ChangeTaskImpl existTask = this.findTask(key);
    if (existTask == null) {
      // 不存在旧任务，直接入队
      this.pushTask(key, task);
      return true;
    }

    // 2. 版本号比较（新任务必须大于等于旧任务）
    if (task.changeCtx.smallerThan(existTask.changeCtx)) {
      return false;
    }

    // 3. 统一合并版本号（所有场景均需执行）
    task.changeCtx.mergeVersion(existTask.changeCtx);

    // 4. 获取新旧任务的 publisherCount
    Integer oldPublisherCount = existTask.changeCtx.getPublisherCount();
    Integer newPublisherCount = changeCtx.getPublisherCount();

    // 5. 判断新旧任务类型
    boolean isOldTaskSmall = null == oldPublisherCount || oldPublisherCount <= this.publisherThreshold;
    boolean isNewTaskSmall = null == newPublisherCount || newPublisherCount <= this.publisherThreshold;

    // 6. 根据新旧任务类型组合处理
    if (isOldTaskSmall) {
      if (isNewTaskSmall) {
        // 小推送 → 小推送：保留较晚的 deadline 或更新版本号
        mergeSmallTask(key, task, existTask);
      } else {
        // 小推送 → 大推送：直接覆盖旧任务（使用新 deadline）
        this.pushTask(key, task);
      }
    } else {
      if (isNewTaskSmall) {
        // 大推送 → 小推送：直接覆盖旧任务（使用新 deadline）
        this.pushTask(key, task);
      } else {
        // 大推送 → 大推送：仅更新版本号（保留旧 deadline）
        mergeLargeTask(task, existTask);
      }
    }

    return true;
  }

  /**
   * 合并两个小推送任务
   *
   * @param key     任务键
   * @param newTask 新任务
   * @param oldTask 旧任务
   */
  private void mergeSmallTask(ChangeKey key, ChangeTaskImpl newTask, ChangeTaskImpl oldTask) {
    if (newTask.expireTimestamp <= oldTask.expireDeadlineTimestamp) {
      newTask.expireDeadlineTimestamp = oldTask.expireDeadlineTimestamp;
      newTask.changeCtx.addTraceTime(oldTask.changeCtx.getFirstTimes());
      this.updateTask(key, newTask);
    } else {
      oldTask.changeCtx.setExpectDatumVersion(newTask.changeCtx.getExpectDatumVersion());
    }
  }

  /**
   * 合并两个大推送任务
   *
   * @param newTask 新任务
   * @param oldTask 旧任务
   */
  private void mergeLargeTask(ChangeTaskImpl newTask, ChangeTaskImpl oldTask) {
    oldTask.changeCtx.setExpectDatumVersion(newTask.changeCtx.getExpectDatumVersion());
  }

  /**
   * 清空所有任务
   * <p>
   * 通过调用 taskQueue 的 clear 方法清空所有任务数据
   */
  @Override
  public void clear() {
    this.taskQueue.clear();
  }


  /**
   * 获取任务队列实例（仅供测试使用）
   *
   * @return 内部任务队列实例
   */
  @VisibleForTesting
  public ChangeTaskQueue<ChangeKey, ChangeTaskImpl> getTaskQueue() {
    return this.taskQueue;
  }
}
