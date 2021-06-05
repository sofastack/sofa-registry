package com.alipay.sofa.registry.server.data.change;

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

public class DataChangeGroup {
  private final Set<String> dataInfoIds = Sets.newConcurrentHashSet();
  private long firstTime;
  private long lastTime;

  public void addAll(Collection<String> dataInfoIds) {
    long currentTs = System.currentTimeMillis();
    if (firstTime == 0) {
      firstTime = currentTs;
    }
    this.lastTime = Math.max(this.lastTime, currentTs);
    this.dataInfoIds.addAll(dataInfoIds);
  }

  public void clear() {
    dataInfoIds.clear();
    firstTime = 0;
    lastTime = 0;
  }

  public Set<String> getDataInfoIds() {
    return dataInfoIds;
  }
}
