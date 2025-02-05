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
package com.alipay.sofa.registry.store.api.elector;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;

/**
 * @author chen.zhu
 *     <p>Mar 10, 2021
 */
public abstract class AbstractLeaderElector implements LeaderElector {
  private static final Logger LOG = LoggerFactory.getLogger("META-ELECTOR");

  private final List<LeaderAware> leaderAwares = Lists.newCopyOnWriteArrayList();

  private volatile LeaderInfo leaderInfo = LeaderInfo.HAS_NO_LEADER;

  private volatile boolean startElector = false;

  private boolean isObserver = false;

  private final LeaderElectorTrigger leaderElectorTrigger = new LeaderElectorTrigger();

  private volatile String address;

  public AbstractLeaderElector() {
    address = NetUtil.getLocalAddress().getHostAddress();
  }

  @Override
  public void registerLeaderAware(LeaderAware leaderAware) {
    leaderAwares.add(leaderAware);
  }

  @PostConstruct
  public void init() {
    ConcurrentUtils.createDaemonThread("LeaderElectorTrigger", leaderElectorTrigger).start();
  }

  @VisibleForTesting
  public void setAddress(String address) {
    this.address = address;
  }

  private class LeaderElectorTrigger extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      if (startElector) {
        try {
          elect();
          LOG.info("after elect, leader={}", leaderInfo);
        } catch (Throwable e) {
          LOG.error("failed to do elect", e);
        }
      }
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  public void elect() {
    synchronized (this) {
      if (isObserver) { // 如果是Observer，不参与选主
        leaderInfo = doQuery();
        LOG.info("meta role: Observer, leaderInfo: {}", leaderInfo);
      } else {
        leaderInfo = doElect();
      }

      if (amILeader()) {
        onIamLeader();
      } else { // 我不是leader
        onIamNotLeader();
      }
    }
  }

  @Override
  public String myself() {
    return address;
  }
  /** start compete leader */
  @Override
  public synchronized void change2Follow() {
    this.startElector = true;
    this.isObserver = false;
  }

  /** stop compete leader */
  @Override
  public synchronized void change2Observer() {
    this.isObserver = true;
  }
  /**
   * query leader
   *
   * @return LeaderInfo
   */
  protected abstract LeaderInfo doQuery();

  /**
   * elector leader
   *
   * @return LeaderInfo
   */
  protected abstract LeaderInfo doElect();

  /**
   * Am i elector boolean.
   *
   * @return the boolean
   */
  @Override
  public boolean amILeader() {
    return amILeader(leaderInfo.leader);
  }

  protected boolean amILeader(String leader) {
    return StringUtils.equals(myself(), leader) && leaderNotExpired();
  }

  private boolean leaderNotExpired() {
    long current = System.currentTimeMillis();
    return current < leaderInfo.expireTimestamp;
  }
  /**
   * Gets get elector.
   *
   * @return the get elector
   */
  @Override
  public LeaderInfo getLeaderInfo() {

    if (leaderNotExpired()) {
      return leaderInfo;
    }
    return LeaderInfo.HAS_NO_LEADER;
  }

  /** notify when change to elector */
  private void onIamNotLeader() {
    for (LeaderAware leaderAware : leaderAwares) {
      leaderAware.followNotify();
    }
  }

  /** notify when change to follow */
  private void onIamLeader() {
    for (LeaderAware leaderAware : leaderAwares) {
      leaderAware.leaderNotify();
    }
  }

  protected static LeaderInfo calcLeaderInfo(
      String leader, long epoch, long lastHeartbeat, long duration) {
    final long expireTimestamp = lastHeartbeat + duration / 2;
    return new LeaderInfo(epoch, leader, expireTimestamp);
  }

  public static class LeaderInfo {

    private static final long INIT_EPOCH = -1L;
    public static final LeaderInfo HAS_NO_LEADER = new LeaderInfo(INIT_EPOCH, null, 0);

    private final long epoch;

    private final String leader;

    private final long expireTimestamp;

    public LeaderInfo(long epoch, String leader, long expireTimestamp) {
      this.leader = leader;
      this.epoch = epoch;
      this.expireTimestamp = expireTimestamp;
    }

    /**
     * Getter method for property <tt>epoch</tt>.
     *
     * @return property value of epoch
     */
    public long getEpoch() {
      return epoch;
    }

    /**
     * Getter method for property <tt>leader</tt>.
     *
     * @return property value of leader
     */
    public String getLeader() {
      return leader;
    }

    /**
     * Getter method for property <tt>expireTimestamp</tt>.
     *
     * @return property value of expireTimestamp
     */
    public long getExpireTimestamp() {
      return expireTimestamp;
    }

    @Override
    public String toString() {
      return "LeaderInfo{"
          + "epoch="
          + epoch
          + ", leader='"
          + leader
          + '\''
          + ", expireTimestamp="
          + expireTimestamp
          + '}';
    }
  }
}
