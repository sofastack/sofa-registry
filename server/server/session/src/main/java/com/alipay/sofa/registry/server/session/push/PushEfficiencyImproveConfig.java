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

import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author jiangcun.hlc@antfin.com
 * @since 2023/3/16
 */
public class PushEfficiencyImproveConfig {

  public static final int DEFAULT_CHANGE_TASK_WAITING_MILLIS = 100;
  public static final int DEFAULT_PUSH_TASK_WAITING_MILLIS = 200;
  public static final int DEFAULT_REG_WORK_WAITING_MILLIS = 200;
  public static final String CURRENT_DATA_CENTER_ALL_ZONE = "ALL_ZONE";
  public static final String ALL_APP = "ALL_APP";

  /** 三板斧 配置 当前IP */
  private static String CURRENT_IP = NetUtil.getLocalAddress().getHostAddress();
  /** 三板斧配置 当前Zone */
  private String CURRENT_ZONE;

  public int DEFAULT_CHANGE_DEBOUNCING_MILLIS = 1000;
  public int DEFAULT_CHANGE_DEBOUNCING_MAX_MILLIS = 3000;
  public int DEFAULT_PUSH_TASK_DEBOUNCING_MILLIS = 500;

  /**
   * session 收到 data 数据变更推送，创建 changeTask, delay changeDebouncingMillis 时间处理，避免数据连续变化触发大量推送, 默认
   * 1000ms
   */
  private int changeDebouncingMillis = DEFAULT_CHANGE_DEBOUNCING_MILLIS;
  /**
   * session 收到 data 数据变更推送，创建 changeTask, delay changeDebouncingMillis 时间处理时，设置
   * changeDebouncingMaxMillis，避免连续相同的任务一直刷新，使得 changeTask 饥饿得不到处理, 默认 3000ms
   */
  private int changeDebouncingMaxMillis = DEFAULT_CHANGE_DEBOUNCING_MAX_MILLIS;
  /** session 异步处理 changeTask 的 Looper 间隔时间, 默认 100ms */
  private int changeTaskWaitingMillis = DEFAULT_CHANGE_TASK_WAITING_MILLIS;
  /** session 异步处理 pushTask 的 Looper 间隔时间, 默认 200ms */
  private int pushTaskWaitingMillis = DEFAULT_PUSH_TASK_WAITING_MILLIS;
  /** session 处理 pushTask delay pushTaskDebouncingMillis 时间处理，可以合并相同的推送任务，避免数据连续变化触发大量推送, 默认500ms */
  private int pushTaskDebouncingMillis = DEFAULT_PUSH_TASK_DEBOUNCING_MILLIS;

  /** Sub 请求的 BufferWorker Loop wait 时间, 默认 200ms */
  private int regWorkWaitingMillis = DEFAULT_REG_WORK_WAITING_MILLIS;

  /** 三板斧配置 配置在 Ip 集合 的 Session 机器生效 */
  private Set<String> ipSet = new HashSet<>();

  /** 三板斧配置 配置在 Zone 集合 的 Session 机器生效, 要求大写 */
  private Set<String> zoneSet = new HashSet<>();

  /** 三板斧配置 支持 按订阅方应用 设置 时间 */
  private Set<String> subAppSet = new HashSet<>();
  /** 三板斧配置 支持 按订阅方应用 设置 pushTask wake 默认false */
  private boolean pushTaskWake = false;
  /** 三板斧配置 支持 按订阅方应用 设置 regWorkTask wake 默认 reg wakeup true */
  private boolean regWorkWake = true;

  /** session 处理 pushTask delay pushTaskDebouncingMillis 时间处理，可以合并相同的推送任务，避免数据连续变化触发大量推送, 默认500ms */
  private int sbfAppPushTaskDebouncingMillis = DEFAULT_PUSH_TASK_DEBOUNCING_MILLIS;

  /**
   * 判断是否满足 三板斧灰度条件
   *
   * @return boolean
   */
  public boolean inIpZoneSBF() {
    if (CollectionUtils.isNotEmpty(zoneSet) && zoneSet.contains(CURRENT_DATA_CENTER_ALL_ZONE)) {
      return true;
    }
    if (CollectionUtils.isNotEmpty(zoneSet) && zoneSet.contains(CURRENT_ZONE)) {
      return true;
    }
    if (CollectionUtils.isNotEmpty(ipSet) && ipSet.contains(CURRENT_IP)) {
      return true;
    }
    return false;
  }

  /**
   * 判断是否满足 三板斧灰度条件
   *
   * @param appName appName
   * @return boolean
   */
  public boolean inAppSBF(String appName) {
    if (CollectionUtils.isNotEmpty(subAppSet) && subAppSet.contains(ALL_APP)) {
      return true;
    }
    if (CollectionUtils.isNotEmpty(subAppSet) && subAppSet.contains(appName)) {
      return true;
    }
    return false;
  }

  public PushEfficiencyImproveConfig() {}

  public int getChangeDebouncingMillis() {
    if (inIpZoneSBF()) {
      return changeDebouncingMillis;
    }
    return DEFAULT_CHANGE_DEBOUNCING_MILLIS;
  }

  public void setChangeDebouncingMillis(int changeDebouncingMillis) {
    this.changeDebouncingMillis = changeDebouncingMillis;
  }

  public int getChangeDebouncingMaxMillis() {
    if (inIpZoneSBF()) {
      return changeDebouncingMaxMillis;
    }
    return DEFAULT_CHANGE_DEBOUNCING_MAX_MILLIS;
  }

  public void setChangeDebouncingMaxMillis(int changeDebouncingMaxMillis) {
    this.changeDebouncingMaxMillis = changeDebouncingMaxMillis;
  }

  public int getChangeTaskWaitingMillis() {
    if (inIpZoneSBF()) {
      return changeTaskWaitingMillis;
    }
    return DEFAULT_CHANGE_TASK_WAITING_MILLIS;
  }

  public void setChangeTaskWaitingMillis(int changeTaskWaitingMillis) {
    this.changeTaskWaitingMillis = changeTaskWaitingMillis;
  }

  public int getPushTaskWaitingMillis() {
    if (inIpZoneSBF()) {
      return pushTaskWaitingMillis;
    }
    return DEFAULT_PUSH_TASK_WAITING_MILLIS;
  }

  public void setPushTaskWaitingMillis(int pushTaskWaitingMillis) {
    this.pushTaskWaitingMillis = pushTaskWaitingMillis;
  }

  public int getPushTaskDebouncingMillis() {
    if (inIpZoneSBF()) {
      return pushTaskDebouncingMillis;
    }
    return DEFAULT_PUSH_TASK_DEBOUNCING_MILLIS;
  }

  public void setPushTaskDebouncingMillis(int pushTaskDebouncingMillis) {
    this.pushTaskDebouncingMillis = pushTaskDebouncingMillis;
  }

  public int getRegWorkWaitingMillis() {
    if (inIpZoneSBF()) {
      return regWorkWaitingMillis;
    }
    return DEFAULT_REG_WORK_WAITING_MILLIS;
  }

  public void setRegWorkWaitingMillis(int regWorkWaitingMillis) {
    this.regWorkWaitingMillis = regWorkWaitingMillis;
  }

  public Set<String> getIpSet() {
    return ipSet;
  }

  public void setIpSet(Set<String> ipSet) {
    this.ipSet = ipSet;
  }

  public Set<String> getZoneSet() {
    return zoneSet;
  }

  public void setZoneSet(Set<String> zoneSet) {
    this.zoneSet = zoneSet;
  }

  public Set<String> getSubAppSet() {
    return subAppSet;
  }

  public void setSubAppSet(Set<String> subAppSet) {
    this.subAppSet = subAppSet;
  }

  public int fetchSbfAppPushTaskDebouncingMillis(String appName) {
    if (inAppSBF(appName)) {
      return sbfAppPushTaskDebouncingMillis;
    }
    return getPushTaskDebouncingMillis();
  }

  public boolean fetchPushTaskWake(String appName) {
    if (inAppSBF(appName)) {
      return pushTaskWake;
    }
    return false;
  }

  public boolean fetchRegWorkWake(String appName) {
    if (inAppSBF(appName)) {
      return regWorkWake;
    }
    return true;
  }

  public int getSbfAppPushTaskDebouncingMillis() {
    return sbfAppPushTaskDebouncingMillis;
  }

  public void setSbfAppPushTaskDebouncingMillis(int sbfAppPushTaskDebouncingMillis) {
    this.sbfAppPushTaskDebouncingMillis = sbfAppPushTaskDebouncingMillis;
  }

  public boolean isPushTaskWake() {
    return pushTaskWake;
  }

  public void setPushTaskWake(boolean pushTaskWake) {
    this.pushTaskWake = pushTaskWake;
  }

  public boolean isRegWorkWake() {
    return regWorkWake;
  }

  public void setRegWorkWake(boolean regWorkWake) {
    this.regWorkWake = regWorkWake;
  }

  public void setSessionServerConfig(SessionServerConfig sessionServerConfig) {
    if (null != sessionServerConfig
        && StringUtils.isNotBlank(sessionServerConfig.getSessionServerRegion())) {
      this.CURRENT_ZONE = sessionServerConfig.getSessionServerRegion().toUpperCase();
      this.DEFAULT_CHANGE_DEBOUNCING_MILLIS = sessionServerConfig.getDataChangeDebouncingMillis();
      this.DEFAULT_CHANGE_DEBOUNCING_MAX_MILLIS =
          sessionServerConfig.getDataChangeMaxDebouncingMillis();
      this.DEFAULT_PUSH_TASK_DEBOUNCING_MILLIS =
          sessionServerConfig.getPushDataTaskDebouncingMillis();
    }
  }

  public boolean validate() {
    if (pushTaskWaitingMillis <= 0 || changeTaskWaitingMillis <= 0) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "PushEfficiencyImproveConfig{"
        + "CURRENT_ZONE='"
        + CURRENT_ZONE
        + '\''
        + ", CURRENT_IP="
        + CURRENT_IP
        + ", inIpZoneSBF="
        + inIpZoneSBF()
        + ", DEFAULT_CHANGE_DEBOUNCING_MILLIS="
        + DEFAULT_CHANGE_DEBOUNCING_MILLIS
        + ", DEFAULT_CHANGE_DEBOUNCING_MAX_MILLIS="
        + DEFAULT_CHANGE_DEBOUNCING_MAX_MILLIS
        + ", DEFAULT_PUSH_TASK_DEBOUNCING_MILLIS="
        + DEFAULT_PUSH_TASK_DEBOUNCING_MILLIS
        + ", changeDebouncingMillis="
        + changeDebouncingMillis
        + ", changeDebouncingMaxMillis="
        + changeDebouncingMaxMillis
        + ", changeTaskWaitingMillis="
        + changeTaskWaitingMillis
        + ", pushTaskWaitingMillis="
        + pushTaskWaitingMillis
        + ", pushTaskDebouncingMillis="
        + pushTaskDebouncingMillis
        + ", regWorkWaitingMillis="
        + regWorkWaitingMillis
        + ", ipSet="
        + ipSet
        + ", zoneSet="
        + zoneSet
        + ", subAppSet="
        + subAppSet
        + ", sbfAppPushTaskDebouncingMillis="
        + sbfAppPushTaskDebouncingMillis
        + ", pushTaskWake="
        + pushTaskWake
        + ", regWorkWake="
        + regWorkWake
        + '}';
  }
}
