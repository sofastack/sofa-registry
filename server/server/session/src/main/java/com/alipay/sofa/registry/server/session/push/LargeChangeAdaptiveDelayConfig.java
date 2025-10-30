package com.alipay.sofa.registry.server.session.push;

/**
 * @author huicha
 * @date 2025/10/29
 */
public class LargeChangeAdaptiveDelayConfig {

  public static final boolean DEFAULT_USE_LARGE_ADAPTER_DELAY_CHANGE_WORKER = false;

  public static final long DEFAULT_PUBLISHER_THRESHOLD = 2000;

  public static final long DEFAULT_MAX_PUBLISHER_COUNT = 4900;

  public static final long DEFAULT_BASE_DELAY = 1000;

  public static final long DEFAULT_DELAY_PER_UNIT = 10;

  /**
   * 是否开启使用 LargeChangeAdaptiveDelayWorker
   */
  private boolean useLargeAdapterDelayChangeWorker = DEFAULT_USE_LARGE_ADAPTER_DELAY_CHANGE_WORKER;

  /**
   * Publisher 数量阈值，超过此值开始计算动态延迟
   */
  private long publisherThreshold = DEFAULT_PUBLISHER_THRESHOLD;

  /**
   * Publisher 数量上限，达到此值后延迟固定为最大值
   */
  private long maxPublisherCount = DEFAULT_MAX_PUBLISHER_COUNT;

  /**
   * 基础延迟时间（毫秒），用于 Publisher 数量较少的情况
   */
  private long baseDelay = DEFAULT_BASE_DELAY;

  /**
   * 每个 Publisher 单位的延迟增量（毫秒）
   */
  private long delayPerUnit = DEFAULT_DELAY_PER_UNIT;

  public boolean isUseLargeAdapterDelayChangeWorker() {
    return useLargeAdapterDelayChangeWorker;
  }

  public void setUseLargeAdapterDelayChangeWorker(boolean useLargeAdapterDelayChangeWorker) {
    this.useLargeAdapterDelayChangeWorker = useLargeAdapterDelayChangeWorker;
  }

  public long getPublisherThreshold() {
    return publisherThreshold;
  }

  public void setPublisherThreshold(long publisherThreshold) {
    this.publisherThreshold = publisherThreshold;
  }

  public long getMaxPublisherCount() {
    return maxPublisherCount;
  }

  public void setMaxPublisherCount(long maxPublisherCount) {
    this.maxPublisherCount = maxPublisherCount;
  }

  public long getBaseDelay() {
    return baseDelay;
  }

  public void setBaseDelay(long baseDelay) {
    this.baseDelay = baseDelay;
  }

  public long getDelayPerUnit() {
    return delayPerUnit;
  }

  public void setDelayPerUnit(long delayPerUnit) {
    this.delayPerUnit = delayPerUnit;
  }
}
