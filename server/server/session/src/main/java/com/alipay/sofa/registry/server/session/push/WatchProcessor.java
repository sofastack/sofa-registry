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

import com.alipay.remoting.rpc.exception.InvokeTimeoutException;
import com.alipay.sofa.registry.common.model.store.PushData;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.node.service.ClientNodeService;
import com.alipay.sofa.registry.task.MetricsableThreadPoolExecutor;
import com.alipay.sofa.registry.task.RejectedDiscardHandler;
import com.alipay.sofa.registry.util.OsUtils;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;

public class WatchProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(WatchProcessor.class);
  private static final Logger WATCH_LOGGER = LoggerFactory.getLogger("WATCH-PUSH");
  @Autowired protected SessionServerConfig sessionServerConfig;

  @Autowired protected PushSwitchService pushSwitchService;

  @Autowired protected PushDataGenerator pushDataGenerator;

  @Autowired protected ClientNodeService clientNodeService;

  final RejectedDiscardHandler discardHandler = new RejectedDiscardHandler();
  private final ThreadPoolExecutor watchCallbackExecutor =
      MetricsableThreadPoolExecutor.newExecutor(
          "WatchCallback", OsUtils.getCpuCount() * 2, 8000, discardHandler);

  boolean doExecuteOnWatch(Watcher watcher, ReceivedConfigData data, long triggerTimestamp) {
    if (!pushSwitchService.canIpPushLocal(watcher.getSourceAddress().getIpAddress())) {
      return false;
    }
    if (watcher.getPushedVersion() >= data.getVersion()) {
      return false;
    }
    PushData pushData = pushDataGenerator.createPushData(watcher, data);
    clientNodeService.pushWithCallback(
        pushData.getPayload(),
        watcher.getSourceAddress(),
        new WatchPushCallback(triggerTimestamp, watcher, data.getVersion()));
    return true;
  }

  final class WatchPushCallback implements CallbackHandler {
    final long triggerTimestamp;
    final long pushStartTimestamp = System.currentTimeMillis();
    final Watcher watcher;
    final long version;

    WatchPushCallback(long triggerTimestamp, Watcher w, long version) {
      this.watcher = w;
      this.version = version;
      this.triggerTimestamp = triggerTimestamp;
    }

    @Override
    public void onCallback(Channel channel, Object message) {
      watcher.updatePushedVersion(version);
      tracePush(PushTrace.PushStatus.OK);
    }

    @Override
    public void onException(Channel channel, Throwable exception) {
      if (exception instanceof InvokeTimeoutException) {
        tracePush(PushTrace.PushStatus.Timeout);
        LOGGER.error("[PushTimeout]watcher={}, ver={}", watcher.shortDesc(), version);
      } else {
        final boolean channelConnected = channel.isConnected();
        if (channelConnected) {
          tracePush(PushTrace.PushStatus.Fail);
          LOGGER.error("[PushFailed]watcher={}, ver={}", watcher.shortDesc(), version, exception);
        } else {
          tracePush(PushTrace.PushStatus.ChanClosed);
          LOGGER.error("[PushChanClosed]watcher={}, ver={}", watcher.shortDesc(), version);
        }
      }
    }

    void tracePush(PushTrace.PushStatus status) {
      trace(triggerTimestamp, pushStartTimestamp, status, watcher, version);
    }

    @Override
    public Executor getExecutor() {
      return watchCallbackExecutor;
    }
  }

  void trace(
      long triggerTimestamp,
      long pushStartTimestamp,
      PushTrace.PushStatus status,
      Watcher w,
      long version) {
    final long finishTs = System.currentTimeMillis();
    WATCH_LOGGER.info(
        "{},{},{},{},delay={},{},regTs={},notifyTs={},addr={}",
        status,
        w.getDataInfoId(),
        version,
        w.getAppName(),
        finishTs - triggerTimestamp,
        finishTs - pushStartTimestamp,
        w.getRegisterTimestamp(),
        triggerTimestamp,
        w.getSourceAddress().buildAddressString());
  }
}
