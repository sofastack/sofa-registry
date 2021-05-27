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
package com.alipay.sofa.registry.server.session.resource;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

public final class Sdks {
  private static final Logger LOGGER = LoggerFactory.getLogger(Sdks.class);

  private Sdks() {}

  public static List<URL> getOtherConsoleServers(
      String zone, SessionServerConfig sessionServerConfig, MetaServerService metaNodeService) {
    if (StringUtils.isBlank(zone)) {
      zone = sessionServerConfig.getSessionServerRegion();
    }
    if (StringUtils.isNotBlank(zone)) {
      zone = zone.toUpperCase();
    }
    List<URL> servers =
        metaNodeService.getSessionServerList(zone).stream()
            .filter(server -> !server.equals(NetUtil.getLocalAddress().getHostAddress()))
            .map(server -> new URL(server, sessionServerConfig.getConsolePort()))
            .collect(Collectors.toList());
    return servers;
  }

  public static List<CommonResponse> concurrentSdkSend(
      ThreadPoolExecutor pool, List<URL> servers, SdkExecutor executor, int timeoutMs) {
    List<CommonResponse> responses = new ArrayList<>(servers.size());
    final CountDownLatch latch = new CountDownLatch(servers.size());
    for (URL url : servers) {
      pool.submit(
          () -> {
            try {
              CommonResponse resp = exec(executor, url);
              responses.add(resp);
            } finally {
              latch.countDown();
            }
          });
    }
    try {
      latch.await(timeoutMs, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      responses.add(CommonResponse.buildFailedResponse("execute timeout"));
    }
    return responses;
  }

  public static CommonResponse getFailedResponseIfAbsent(List<CommonResponse> responses) {
    Optional<CommonResponse> failedResponses =
        responses.stream().filter(r -> !r.isSuccess()).findFirst();
    return failedResponses.orElseGet(CommonResponse::buildSuccessResponse);
  }

  static CommonResponse exec(SdkExecutor executor, URL url) {
    try {
      return executor.execute(url);
    } catch (Throwable e) {
      if (e.getCause() instanceof ConnectException) {
        LOGGER.warn("connect {} refused", url, e);
        return new CommonResponse(true, "ignored error: connection refused");
      }
      if (e.getCause() instanceof SocketTimeoutException) {
        LOGGER.warn("connect {} timeout", url, e);
        return new CommonResponse(true, "ignored error: connect timeout");
      }
      LOGGER.error("send request other session error!url={}", url, e);
      return new CommonResponse(false, e.getMessage());
    }
  }

  interface SdkExecutor {
    CommonResponse execute(URL url) throws Exception;
  }
}
