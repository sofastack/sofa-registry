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
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

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
    List<URL> others = Lists.newLinkedList();
    for (String server : metaNodeService.getSessionServerList(zone)) {
      if (!ServerEnv.isLocalServer(server)) {
        others.add(new URL(server, sessionServerConfig.getConsolePort()));
      }
    }
    return others;
  }

  public static Map<URL, CommonResponse> concurrentSdkSend(
      ExecutorService pool, List<URL> servers, SdkExecutor executor, int timeoutMs) {
    Map<URL, CommonResponse> responses =
        Collections.synchronizedMap(Maps.newHashMapWithExpectedSize(servers.size() + 1));

    final CountDownLatch latch = new CountDownLatch(servers.size());
    for (URL url : servers) {
      pool.submit(
          () -> {
            try {
              CommonResponse resp = exec(executor, url);
              responses.put(url, resp);
            } finally {
              latch.countDown();
            }
          });
    }
    try {
      boolean success = latch.await(timeoutMs, TimeUnit.MILLISECONDS);
      if (!success) {
        LOGGER.error("[ConcurrentSendError]concurrent send timeout.");
      }

    } catch (InterruptedException e) {
      LOGGER.error("[ConcurrentSendError]concurrent send error.", e);
    } finally {
      SetView<URL> difference = Sets.difference(Sets.newHashSet(servers), responses.keySet());
      for (URL url : difference) {
        responses.put(url, CommonResponse.buildFailedResponse("execute fail"));
      }
    }
    return responses;
  }

  public static CommonResponse getFailedResponseIfAbsent(Collection<CommonResponse> responses) {
    if (CollectionUtils.isEmpty(responses)) {
      return CommonResponse.buildFailedResponse("response is empty");
    }

    Optional<CommonResponse> failedResponses =
        responses.stream().filter(r -> !r.isSuccess()).findFirst();
    return failedResponses.orElseGet(CommonResponse::buildSuccessResponse);
  }

  static CommonResponse exec(SdkExecutor executor, URL url) {
    try {
      return executor.execute(url);
    } catch (Throwable e) {
      if (e instanceof RequestException) {
        final Throwable cause = e.getCause();
        // failed on connect
        if (cause.getMessage().contains("connect RemotingException")) {
          LOGGER.warn("connect failed {}", url, e);
          return new CommonResponse(true, "ignored error: connection failed");
        }
      }
      LOGGER.error("send request other session error!url={}", url, e);
      return new CommonResponse(false, e.getMessage());
    }
  }

  public interface SdkExecutor {
    CommonResponse execute(URL url) throws Exception;
  }
}
