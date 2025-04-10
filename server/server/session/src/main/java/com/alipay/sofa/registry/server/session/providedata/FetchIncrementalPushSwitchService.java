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
package com.alipay.sofa.registry.server.session.providedata;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.shared.providedata.AbstractFetchSystemPropertyService;
import com.alipay.sofa.registry.server.shared.providedata.SystemDataStorage;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huicha
 * @date 2025/2/25
 */
public class FetchIncrementalPushSwitchService
    extends AbstractFetchSystemPropertyService<
        FetchIncrementalPushSwitchService.IncrementalPushSwitchStorage> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(
          FetchIncrementalPushSwitchService.class, "[FetchIncrementalPushSwitch]");

  // 默认是否开启增量推送
  private static final boolean DEFAULT_USE_INCREMENTAL_PUSH = false;

  @Autowired private SessionServerConfig sessionServerConfig;

  public FetchIncrementalPushSwitchService() {
    super(
        ValueConstants.SESSION_INCREMENTAL_PUSH_SWITCH_DATA_ID,
        new IncrementalPushSwitchStorage(INIT_VERSION, IncrementalPushSwitch.createDefault()));
  }

  @Override
  protected int getSystemPropertyIntervalMillis() {
    return this.sessionServerConfig.getSystemPropertyIntervalMillis();
  }

  @Override
  protected boolean doProcess(IncrementalPushSwitchStorage oldStorage, ProvideData provideData) {
    try {
      // 收到新数据，开始处理
      // 1. 首先解析出新的增量推送数据开关的值
      String data = ProvideData.toString(provideData);
      IncrementalPushSwitch incrementalPushSwitch =
          JsonUtils.read(data, new TypeReference<IncrementalPushSwitch>() {});

      // 这里重新处理下 Zone 信息，全部调整为大写，方便后续做匹配的时候使用
      Set<String> useIncPushZones = incrementalPushSwitch.getUseIncPushZones();
      if (!CollectionUtils.isEmpty(useIncPushZones)) {
        Set<String> upperCaseZones =
            useIncPushZones.stream().map(StringUtils::upperCase).collect(Collectors.toSet());
        incrementalPushSwitch.setUseIncPushZones(upperCaseZones);
      }

      // 2. 更新数据
      if (!this.getStorage()
          .compareAndSet(
              oldStorage,
              new IncrementalPushSwitchStorage(provideData.getVersion(), incrementalPushSwitch))) {
        // 覆盖失败了那么可能是有并发冲突，跳过处理
        return false;
      }

      // TODO(xidong.rxd): 这里需要触发开启增量推送的 Subscriber 的全量推送任务
      return true;
    } catch (Throwable throwable) {
      LOGGER.error("Process new incremental push switch exception", throwable);
      return false;
    }
  }

  /**
   * 这个方法用来判断 Session 是否开启了增量推送
   *
   * @return 是否开启了增量推送
   */
  public boolean useIncrementalPush() {
    // 1. 先检查配置是否存在
    IncrementalPushSwitchStorage storage = this.storage.get();
    if (null == storage) {
      // 没有配置则按照默认配置来
      return DEFAULT_USE_INCREMENTAL_PUSH;
    }

    IncrementalPushSwitch incrementalPushSwitch = storage.getIncrementalPushSwitch();
    if (null == incrementalPushSwitch) {
      // 没有配置则按照默认配置来
      return DEFAULT_USE_INCREMENTAL_PUSH;
    }

    // 2. 检查全局开关是否打开
    if (incrementalPushSwitch.isAllSessionUseIncPush()) {
      // 对所有的 Session 都开启了增量推送
      return true;
    }

    // 3. 检查 Zone 白名单
    String currentZone = this.sessionServerConfig.getSessionServerRegion();
    if (StringUtils.isNotBlank(currentZone)) {
      currentZone = StringUtils.upperCase(currentZone);
      Set<String> useIncPushZones = incrementalPushSwitch.getUseIncPushZones();
      if (useIncPushZones.contains(currentZone)) {
        // 当前 Zone 开启了增量推送
        return true;
      }
    }

    // 4. 检查 IP 白名单
    InetAddress localAddr = NetUtil.getLocalAddress();
    if (null != localAddr) {
      String localHostAddr = localAddr.getHostAddress();
      Set<String> useIncPushSessionIps = incrementalPushSwitch.getUseIncPushSessionIps();
      if (useIncPushSessionIps.contains(localHostAddr)) {
        // 当前 IP 开启了增量推送
        return true;
      }
    }

    // 没有命中任何白名单，那么不开启增量推送
    // todo(xidong.rxd): 别忘了改回来
    return true;
  }

  protected static class IncrementalPushSwitchStorage extends SystemDataStorage {

    private IncrementalPushSwitch incrementalPushSwitch;

    public IncrementalPushSwitchStorage(long version, IncrementalPushSwitch incrementalPushSwitch) {
      super(version);
      this.incrementalPushSwitch = incrementalPushSwitch;
    }

    public IncrementalPushSwitch getIncrementalPushSwitch() {
      return incrementalPushSwitch;
    }
  }

  private static class IncrementalPushSwitch {
    private boolean allSessionUseIncPush;

    private Set<String> useIncPushZones;

    private Set<String> useIncPushSessionIps;

    public static IncrementalPushSwitch createDefault() {
      return new IncrementalPushSwitch();
    }

    public IncrementalPushSwitch() {
      this(false, Collections.emptySet(), Collections.emptySet());
    }

    public IncrementalPushSwitch(
        boolean allSessionUseIncPush,
        Set<String> useIncPushZones,
        Set<String> useIncPushSessionIps) {
      this.allSessionUseIncPush = allSessionUseIncPush;
      this.useIncPushZones = useIncPushZones;
      this.useIncPushSessionIps = useIncPushSessionIps;
    }

    public boolean isAllSessionUseIncPush() {
      return allSessionUseIncPush;
    }

    public void setAllSessionUseIncPush(boolean allSessionUseIncPush) {
      this.allSessionUseIncPush = allSessionUseIncPush;
    }

    public Set<String> getUseIncPushZones() {
      return useIncPushZones;
    }

    public void setUseIncPushZones(Set<String> useIncPushZones) {
      this.useIncPushZones = useIncPushZones;
    }

    public Set<String> getUseIncPushSessionIps() {
      return useIncPushSessionIps;
    }

    public void setUseIncPushSessionIps(Set<String> useIncPushSessionIps) {
      this.useIncPushSessionIps = useIncPushSessionIps;
    }
  }
}
