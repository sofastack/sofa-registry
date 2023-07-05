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
package com.alipay.sofa.registry.server.session.client.manager;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.sessionserver.CheckClientManagerRequest;
import com.alipay.sofa.registry.common.model.sessionserver.CheckClientManagerResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.message.SimpleRequest;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.providedata.FetchClientOffAddressService;
import com.alipay.sofa.registry.server.session.resource.Sdks;
import com.alipay.sofa.registry.server.shared.meta.MetaServerService;
import com.alipay.sofa.registry.store.api.meta.ClientManagerAddressRepository;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : CheckClientManagerService.java, v 0.1 2022年01月04日 19:54 xiaojian.xj Exp $
 */
public class CheckClientManagerService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckClientManagerService.class);

  private static final long NO_NEED_CHECK = 0L;
  @Autowired private ExecutorManager executorManager;

  @Resource private FetchClientOffAddressService fetchClientOffAddressService;

  @Autowired private ClientManagerAddressRepository clientManagerAddressRepository;

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private MetaServerService metaServerService;

  @Autowired private NodeExchanger sessionConsoleExchanger;

  private final Retryer<Boolean> retryer =
      RetryerBuilder.<Boolean>newBuilder()
          .retryIfException()
          .retryIfResult(input -> !input)
          .withWaitStrategy(WaitStrategies.fixedWait(200, TimeUnit.MILLISECONDS))
          .withStopStrategy(StopStrategies.stopAfterAttempt(15))
          .build();

  /**
   * check local version and other session version
   *
   * @param expectedVersion expectedVersion
   * @return boolean
   */
  public boolean check(long expectedVersion) {

    if (expectedVersion == NO_NEED_CHECK) {
      return true;
    }
    Future<Tuple<Boolean, Long>> submit =
        executorManager
            .getClientManagerCheckExecutor()
            .submit(() -> checkLocalCache(expectedVersion));

    List<URL> servers = getOtherConsoleServersCurrentZone();
    LOGGER.info("checkInZone expectedVersion: {}, others={}", expectedVersion, servers);
    if (servers.size() == 0) {
      return checkLocalResp(expectedVersion, submit);
    }
    Map<URL, CommonResponse> map =
        Sdks.concurrentSdkSend(
            executorManager.getClientManagerCheckExecutor(),
            servers,
            (URL url) -> {
              final CheckClientManagerRequest req = new CheckClientManagerRequest(expectedVersion);
              return (CommonResponse)
                  sessionConsoleExchanger.request(new SimpleRequest(req, url)).getResult();
            },
            3000);

    return checkLocalResp(expectedVersion, submit) && checkOtherServersResp(map, expectedVersion);
  }

  /**
   * check other session version
   *
   * @param map map
   * @param expectedVersion expectedVersion
   * @return boolean
   */
  private boolean checkOtherServersResp(Map<URL, CommonResponse> map, long expectedVersion) {
    for (Entry<URL, CommonResponse> entry : map.entrySet()) {
      if (!(entry.getValue() instanceof GenericResponse)) {
        LOGGER.error(
            "url={} client manager check fail, expectedVersion: {}, resp: {}",
            entry.getKey().getIpAddress(),
            expectedVersion,
            entry.getValue());
        return false;
      }
      GenericResponse<CheckClientManagerResponse> response = (GenericResponse) entry.getValue();
      CheckClientManagerResponse data = response.getData();
      if (!response.isSuccess() || !data.isPaasCheck()) {
        LOGGER.error(
            "url={} client manager check fail, expectedVersion: {}, actual: {}",
            entry.getKey().getIpAddress(),
            expectedVersion,
            data.getActualVersion());
        return false;
      }
    }
    return true;
  }

  /**
   * check local version
   *
   * @param expectedVersion expectedVersion
   * @return Tuple
   */
  public Tuple<Boolean, Long> checkLocalCache(long expectedVersion) {

    final AtomicLong version = new AtomicLong(0);
    try {
      retryer.call(
          () -> {
            clientManagerAddressRepository.wakeup();
            fetchClientOffAddressService.wakeup();
            version.set(fetchClientOffAddressService.lastLoadVersion());
            return version.get() >= expectedVersion;
          });
      return new Tuple(true, version.get());
    } catch (Throwable t) {
      LOGGER.error(
          "[checkLocalCache]client manager check version, excepted: {}, actual: {} error.",
          expectedVersion,
          version.get(),
          t);
      return new Tuple(false, version.get());
    }
  }

  /**
   * check local resp
   *
   * @param expectedVersion
   * @param submit
   * @return
   */
  private boolean checkLocalResp(long expectedVersion, Future<Tuple<Boolean, Long>> submit) {
    try {
      Tuple<Boolean, Long> localResp = submit.get(3100, TimeUnit.MILLISECONDS);
      if (!paasCheck(localResp.o1, false)) {
        LOGGER.error(
            "client manager local check fail, expectedVersion: {}, actual: {}",
            expectedVersion,
            localResp.o2);
        return false;
      }
      return true;
    } catch (Throwable t) {
      LOGGER.error("client manager local check error. expectedVersion: {}", expectedVersion, t);
      return false;
    }
  }

  private List<URL> getOtherConsoleServersCurrentZone() {
    return Sdks.getOtherConsoleServers(null, sessionServerConfig, metaServerService);
  }

  private boolean paasCheck(Boolean bool, boolean defaultValue) {
    return bool == null ? defaultValue : bool.booleanValue();
  }
}
