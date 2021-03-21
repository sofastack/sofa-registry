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

import com.alipay.sofa.common.profile.StringUtil;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.LoopRunnable;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

/**
 * @author chen.zhu
 *     <p>Mar 10, 2021
 */
public abstract class AbstractLeaderElector implements LeaderElector {
  private static final Logger LOG =
      LoggerFactory.getLogger("META-ELECTOR", "[MetaJdbcLeaderElector]");

  private final List<LeaderAware> leaderAwares = Lists.newCopyOnWriteArrayList();

  private volatile LeaderInfo leaderInfo = LeaderInfo.hasNoLeader;

  private volatile boolean isObserver = false;

  @Override
  public void registerLeaderAware(LeaderAware leaderAware) {
    leaderAwares.add(leaderAware);
  }

  @PostConstruct
  public void init() {
    ConcurrentUtils.createDaemonThread("LeaderElectorTrigger", new LeaderElectorTrigger()).start();
  }

  private class LeaderElectorTrigger extends LoopRunnable {

    @Override
    public void runUnthrowable() {
      elect();
    }

    @Override
    public void waitingUnthrowable() {
      ConcurrentUtils.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  public void elect() {
    synchronized (this) {
      if (isObserver) {
        leaderInfo = doQuery();
      } else {
        leaderInfo = doElect();
      }

      if (amILeader()) {
        onIamLeader();
      } else {
        onIamNotLeader();
      }
    }
  }

  @Override
  public String myself() {
    return NetUtil.getLocalAddress().getHostAddress();
  }
  /**
   * start compete leader
   *
   * @return
   */
  @Override
  public synchronized void change2Follow() {
    this.isObserver = false;
  }

  /**
   * stop compete leader
   *
   * @return
   */
  @Override
  public synchronized void change2Observer() {
    this.isObserver = true;
  }
  /**
   * query leader
   *
   * @return
   */
  protected abstract LeaderInfo doQuery();

  /**
   * elector leader
   *
   * @return
   */
  protected abstract LeaderInfo doElect();

  /**
   * Am i elector boolean.
   *
   * @return the boolean
   */
  @Override
  public boolean amILeader() {
    return leaderInfo != null
        && StringUtil.equals(myself(), leaderInfo.leader)
        && System.currentTimeMillis() < leaderInfo.expireTimestamp;
  }

  protected boolean amILeader(String leader) {
    long current = System.currentTimeMillis();
    if (LOG.isInfoEnabled()) {
      LOG.info(
          "myself is: {}, current timestamp: {}, leader is: {}, leader expireTimestamp: {}",
          myself(),
          current,
          leader,
          leaderInfo.expireTimestamp);
    }
    return StringUtil.equals(myself(), leader) && current < leaderInfo.expireTimestamp;
  }

  /**
   * Gets get elector.
   *
   * @return the get elector
   */
  @Override
  public String getLeader() {
    if (leaderInfo == null) {
      return LeaderInfo.hasNoLeader.leader;
    }
    return leaderInfo.leader;
  }

  /**
   * Gets get elector epoch.
   *
   * @return the get elector epoch
   */
  @Override
  public long getLeaderEpoch() {
    if (leaderInfo == null) {
      return LeaderInfo.hasNoLeader.epoch;
    }
    return leaderInfo.epoch;
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

  public static class LeaderInfo {

    public static final long initEpoch = -1L;
    public static final LeaderInfo hasNoLeader = new LeaderInfo(initEpoch);

    private long epoch;

    private String leader;

    private long expireTimestamp;

    public LeaderInfo(long epoch) {
      this.epoch = epoch;
    }

    public LeaderInfo(long epoch, String leader, Date lastHeartbeat, long duration) {
      this.leader = leader;
      this.epoch = epoch;
      this.expireTimestamp = lastHeartbeat.getTime() + duration / 2;
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
